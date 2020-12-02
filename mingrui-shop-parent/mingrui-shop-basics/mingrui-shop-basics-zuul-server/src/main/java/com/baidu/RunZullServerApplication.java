package com.baidu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableZuulProxy     //开启网关
@EnableEurekaClient
public class RunZullServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RunZullServerApplication.class);
    }

}
