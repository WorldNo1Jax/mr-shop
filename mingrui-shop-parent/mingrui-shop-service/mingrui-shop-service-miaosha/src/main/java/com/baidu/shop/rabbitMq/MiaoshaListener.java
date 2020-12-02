package com.baidu.shop.rabbitMq;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.service.MiaoShaService;
import com.baidu.shop.utils.MqConstants;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @ClassName MiaoshaListener
 * @Description: TODO
 * @Author jlz
 * @Date 2020-10-29 17:28
 * @Version V1.0
 **/
@Component
@Slf4j
public class MiaoshaListener {

    @Autowired
    private MiaoShaService service;


    @RabbitListener(
            bindings = @QueueBinding(
                    //监听 队列
                    value = @Queue(
                            value = MqConstants.MIAOSHA_QUEUE_DELETE,
                            durable = "true"
                    ),
                    exchange = @Exchange(
                            value = MqConstants.MIAOSHA_EXCHANGE,
                            ignoreDeclarationExceptions = "true",
                            type = ExchangeTypes.TOPIC
                    ),
                    //监听routingKey 可以监听多个
                    key = MqConstants.MIAOSHA_ROUT_KEY_DELETE
            )
    )
    public void delete(Message message, Channel channel)throws IOException {
        log.info("接受到需要删除数据的消息: " + new String(message.getBody()));
        service.delStockCountCache(Long.parseLong(new String(message.getBody())));
        log.info("删除数据成功: " + new String(message.getBody()));
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(
                            value = MqConstants.MIAOSHA_QUEUE_ADDORDER,
                            durable = "true"
                    ),
                    exchange = @Exchange(
                            value = MqConstants.MIAOSHA_EXCHANGE,
                            ignoreDeclarationExceptions = "true",
                            type = ExchangeTypes.TOPIC
                    ),
                    key = MqConstants.MIAOSHA_ROUT_KEY_ADDORDER
            )
    )
    public void addorder(Message message, Channel channel)throws IOException {
        log.info("接受到新增订单的消息: " + new String(message.getBody()));
        JSONObject jsonObject = JSONObject.parseObject(new String(message.getBody()));
        service.createOrderByMq(jsonObject.getString("sid"),jsonObject.getInteger("userId"));
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
    }
}
