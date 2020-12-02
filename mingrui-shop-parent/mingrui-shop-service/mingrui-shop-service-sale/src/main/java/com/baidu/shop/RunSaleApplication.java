package com.baidu.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @ClassName RunSaleApplication
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/27
 * @Version V1.0
 **/
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@MapperScan("com.baidu.shop.mapper")
public class RunSaleApplication {

    public static void main(String[] args) {
        SpringApplication.run(RunSaleApplication.class);
    }
}
