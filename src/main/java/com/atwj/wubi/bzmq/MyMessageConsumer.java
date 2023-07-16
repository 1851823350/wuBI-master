package com.atwj.wubi.bzmq;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MyMessageConsumer {


    /**
     * 接收消息
     * @param message 消息内容
     * @param channel 消息所在的通道，可以和RabbitMq进行交互，例如手动确认消息，拒绝消息等;
     * @param deliveryTag 消息投递的标签，用于唯一标识某一条消息
     */
    //简化异常处理
    @SneakyThrows
    //@Header(AmqpHeaders.DELIVERY_TAG)，用于获取消息头中获取投递标签（DELIVERY_TAG），并赋值给deliveryTag
    @RabbitListener(queues = {"code_queue"}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receive message = {}", message);
        channel.basicAck(deliveryTag,false);

    }
}
