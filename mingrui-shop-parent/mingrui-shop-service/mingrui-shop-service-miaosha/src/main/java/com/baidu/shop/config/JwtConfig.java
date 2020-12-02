package com.baidu.shop.config;

import com.baidu.shop.utils.RsaUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

/**
 * @ClassName JwtConfig
 * @Description: TODO
 * @Author jlz
 * @Date 2020-10-17 11:22
 * @Version V1.0
 **/
@Data
@Configuration
public class JwtConfig {

    @Value("${shop.jwt.pubKeyPath}")
    private String pubKeyPath;// 公钥地址

    @Value("${shop.jwt.cookieName}")
    private String cookieName;

    private PublicKey publicKey; // 公钥


    private static final Logger logger = LoggerFactory.getLogger(JwtConfig.class);

    @PostConstruct
    public void init(){
        try {
            // 获取公钥和私钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            logger.error("初始化公钥失败！", e);
            throw new RuntimeException();
        }
    }
}
