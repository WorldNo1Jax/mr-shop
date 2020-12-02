package com.baidu.shop.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 * @ClassName RabbitMqConfig
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/28
 * @Version V1.0
 **/
import org.springframework.amqp.core.Queue;


@Configuration
public class RabbitMqConfig {

    @Bean
    public Queue delCacheQueue() {
        return new Queue("delCache");
    }

    @Bean
    public Queue orderQueue() {
        return new Queue("orderQueue");
    }

}
