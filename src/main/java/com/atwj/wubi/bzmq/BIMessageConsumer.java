package com.atwj.wubi.bzmq;

import com.atwj.wubi.common.ErrorCode;
import com.atwj.wubi.constant.BIRabbitConstant;
import com.atwj.wubi.constant.CommonConstant;
import com.atwj.wubi.exception.BusinessException;
import com.atwj.wubi.manager.AIManager;
import com.atwj.wubi.model.entity.Chart;
import com.atwj.wubi.service.ChartService;
import com.atwj.wubi.utils.ExcelUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Command;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class BIMessageConsumer {
    @Resource
    private AIManager aiManager;

    @Resource
    private ChartService chartService;


    /**
     * 接收消息
     * @param message 消息内容
     * @param channel 消息所在的通道，可以和RabbitMq进行交互，例如手动确认消息，拒绝消息等;
     * @param deliveryTag 消息投递的标签，用于唯一标识某一条消息
     */
    //简化异常处理
    @SneakyThrows
    //@Header(AmqpHeaders.DELIVERY_TAG)，用于获取消息头中获取投递标签（DELIVERY_TAG），并赋值给deliveryTag
    @RabbitListener(queues = {BIRabbitConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {

        log.info("执行消费：" + message);
        if (StringUtils.isBlank(message)) {
            //如果接收消息失败，拒绝当前消息
            channel.basicNack(deliveryTag, false, false);
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if (chart == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图表为空");
        }
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setStatus("running");
        boolean updateResult = chartService.updateById(updateChart);
        if (!updateResult) {
            channel.basicNack(deliveryTag,false, false);
            handleChartUpdateError(chart.getId(), "更新图表失败");
        }

        /**
         * 调用AI
         * 返回数据格式
         * "【【【【【\n" +
         * "{前端 Echarts V5 的 option 配置对象js代码，合理地将数据进行可视化，不要生成任何多余的内容，比如注释}\n" +
         * "【【【【【\n" +
         * "{明确的数据分析结论、越详细越好，不要生成多余的注释}";
         */
        String result = aiManager.doChart(CommonConstant.BI_MODLE_ID, createUserInput(chart));
        String[] split = result.split("【【【【【");
        if (split.length < 3) {
            handleChartUpdateError(chart.getId(), "AI生成失败");
        }
        String genChart = split[1].trim();
        String genResult = split[2].trim();

        Chart chartResult = new Chart();
        chartResult.setId(chart.getId());
        chartResult.setStatus("success");
        chartResult.setGenChart(genChart);
        chartResult.setGenResult(genResult);
        boolean insertChartResult = chartService.updateById(chartResult);
        if (!insertChartResult) {
            channel.basicNack(deliveryTag,false, false);
            handleChartUpdateError(chart.getId(), "更新图表失败");
        }
        log.info("receive message = {}", message);
        channel.basicAck(deliveryTag,false);
    }

    //更新异常工具类
    public void handleChartUpdateError(Long chartId, String message) {
        Chart chart = new Chart();
        chart.setId(chartId);
        chart.setExecMessage(message);
        chart.setStatus("failed");
        boolean result = chartService.updateById(chart);
        if (!result) {
            log.info("更新图表失败" + chartId + message);
        }
    }

    //拼接用户输入
    private String createUserInput(Chart chart) {
        String chartType = chart.getChartType();
        String goal = chart.getGoal();
        String chartData = chart.getChartData();

        //构造请求输入格式 ↓↓↓↓↓↓↓↓↓↓↓↓
        // 1.分析需求：
        // 分析网站用户的增长情况
        // 2.原始数据：
        // 日期,用户数
        // 1号,10
        // 2号,20
        // 3号,30
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        //1.拼接分析图表类型和分析目标
        if (StringUtils.isNotBlank(chartType)) {
            goal += ".请使用" + chartType;
        }
        userInput.append(goal).append("\n");

        //2.拼接压缩后的数据
        userInput.append("原始数据：").append("\n");
        userInput.append(chartData).append("\n");
        return userInput.toString();
    }
}
