package com.atwj.wubi.bzmq;

import com.atwj.wubi.common.ErrorCode;
import com.atwj.wubi.constant.BIRabbitConstant;
import com.atwj.wubi.exception.BusinessException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class BIRabbitMain {
    public static void main(String[] args) {
        try {
            //创建连接工厂
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            //创建连接
            Connection connection = factory.newConnection();
            //创建通道
            Channel channel = connection.createChannel();
            //定义交换机的名称为" code_exchange"
            String EXCHANGE_NAME = BIRabbitConstant.BI_EXCHANGE_NAME;
            //声明交换机，指定交换机类型为direct
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            // 创建队列，随机分配-一个队列名称
            String queueName = BIRabbitConstant.BI_QUEUE_NAME;
            //声明队列，设置队列持久化、非独占、非自动删除，并传入额外的参数为null
            channel.queueDeclare(queueName, true, false, false, null);
            //将队列绑定到交换机，指定路由键
            channel.queueBind(queueName, EXCHANGE_NAME, BIRabbitConstant.BI_ROUTINGKEY_NAME);
        } catch (Exception e) {
            //异常处理
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }
}
