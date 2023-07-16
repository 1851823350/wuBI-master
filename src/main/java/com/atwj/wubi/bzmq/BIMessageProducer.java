package com.atwj.wubi.bzmq;

import com.atwj.wubi.constant.BIRabbitConstant;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class BIMessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     *
     * @param message 发送的消息
     */
    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(BIRabbitConstant.BI_EXCHANGE_NAME, BIRabbitConstant.BI_ROUTINGKEY_NAME, message);
    }
}
