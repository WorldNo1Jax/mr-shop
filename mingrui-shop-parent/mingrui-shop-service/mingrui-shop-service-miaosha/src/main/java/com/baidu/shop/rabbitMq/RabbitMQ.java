package com.baidu.shop.rabbitMq;

import com.baidu.shop.utils.MqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @ClassName RabbitMQ
 * @Description: TODO
 * @Author jlz
 * @Date 2020-10-29 17:24
 * @Version V1.0
 **/
@Component
@Slf4j
public class RabbitMQ implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback {

    private RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitMQ(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        //这是设置回调能收到发送到响应
        rabbitTemplate.setConfirmCallback(this);
        //如果设置备份队列则不起作用
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setReturnCallback(this);
    }

    /**
     * 发送消息方法
     * @param msg
     * @param routingKey
     */
    public void send(String msg , String routingKey){
        CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
        //convertAndSend(exchange:交换机名称,routingKey:路由关键字,object:发送的消息内容,correlationData:消息ID)
        rabbitTemplate.convertAndSend(MqConstants.MIAOSHA_EXCHANGE,routingKey,msg,correlationId);
    }

    /**
     * 是否发送成功消息的方法
     * @param correlationData
     * @param b
     * @param s
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {
        if(b){
            log.info("消息发送成功:correlationData({}),ack({}),cause({})",correlationData,b,s);
        }else{
            log.error("消息发送失败:correlationData({}),ack({}),cause({})",correlationData,b,s);
        }
    }

    /**消息消费失败 会执行的方法
     * @param message
     * @param i
     * @param s
     * @param s1
     * @param s2
     */
    @Override
    public void returnedMessage(Message message, int i, String s, String s1, String s2) {
        log.warn("消息丢失:exchange({}),route({}),replyCode({}),replyText({}),message:{}",s1,s2,i,s,message);
    }
}
