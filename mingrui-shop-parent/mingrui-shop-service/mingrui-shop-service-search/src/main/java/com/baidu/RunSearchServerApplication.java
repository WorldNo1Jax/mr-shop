package com.baidu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import javax.activation.DataSource;

/**
 * @ClassName RunSearchServerApplication
 * @Description: TODO
 * @Author ljc
 * @Date 2020/9/16
 * @Version V1.0
 **/
// exclude = {DataSourceAutoConfiguration.class} 不加载数据源配置
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableFeignClients
@EnableEurekaClient
public class RunSearchServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RunSearchServerApplication.class);
    }
}
