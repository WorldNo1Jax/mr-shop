package com.baidu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @ClassName RunMiaoShaApplicationStarter
 * @Description: TODO
 * @Author jlz
 * @Date 2020-10-29 19:36
 * @Version V1.0
 **/
@SpringBootApplication
@EnableEurekaClient
@MapperScan("com.baidu.shop.mapper")
public class RunMiaoShaApplicationStarter {

    public static void main(String[] args) {
        SpringApplication.run(RunMiaoShaApplicationStarter.class,args);
    }
}
