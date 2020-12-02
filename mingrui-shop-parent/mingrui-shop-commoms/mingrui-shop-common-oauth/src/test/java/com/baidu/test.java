package com.baidu;

import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.utils.JwtUtils;
import com.baidu.shop.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @ClassName test
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/15
 * @Version V1.0
 **/

public class test {



    //公钥位置
    private static final String pubKeyPath = "D:\\Users\\rea.pub";
    //私钥位置
    private static final String priKeyPath = "D:\\Users\\rea.pri";
    //公钥对象
    private PublicKey publicKey;
    //私钥对象
    private PrivateKey privateKey;


    /**
     * 生成公钥私钥 根据密文
     * @throws Exception
     */
    @Test
    public void genRsaKey() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "mingrui");
    }


    /**
     * 从文件中读取公钥私钥
     * @throws Exception
     */
    @Before
    public void getKeyByRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    /**
     * 根据用户信息结合私钥生成token
     * @throws Exception
     */
    @Test
    public void genToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(1, "luojingchen"), privateKey, 2);
        System.out.println("user-token = " + token);
    }


    /**
     * 结合公钥解析token
     * @throws Exception
     */
    @Test
    public void parseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MSwidXNlcm5hbWUiOiJsdW9qaW5nY2hlbiIsImV4cCI6MTYwMjc0MTgwNn0.LVw_9vLMxk3US16sO4ilZManwMzTh21-4mGZFpoVyql6JXbVEGEy_xJMHpAyxYYPPofyJZCzCb-wIEiNsq7WvcltTEsALWVwrVNsHKbbtaLL_EMX9rr9E3B3zZboEupeH7KMqY9vPCVHaRriBFGNIaWTvNEF8pilPuPiyOIb1nc";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}
