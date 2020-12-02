package com.baidu;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableEurekaClient
@MapperScan("com.baidu.shop.mapper")
@EnableFeignClients
@EnableAspectJAutoProxy(exposeProxy = true)
public class XXXServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(XXXServiceApplication.class);
    }
}
