package com.baidu.shop.config;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @ClassName AlipayConfig
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/22
 * @Version V1.0
 **/

public class AlipayConfig {

    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    public static String app_id = "2016102600766869";

    // 商户私钥，您的PKCS8格式RSA2私钥
    public static String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC76UtnqJeIdGTActugP9O1ADgHqiBU7+dlYIGspUHp15/J/FWwjqtE1iWrsOCBMAosX12qusvEXgpqqir2jSMNQAhQCQgCDDGg5QKPEp4Q04536l33tyx1OWc5r1u6oLr4VGf9SSrKYYAI2l9uUT0x5n4n7dL0sr7af22Zj8HoFv+uEXs8bvz7uDF0UsmUVqBTNpEW5Qrl0RzJLRmiQX6PhkpP97bPIiGeKCtfZ6Vr3JeP/tGYXkSB0JM5B9UKmO4VNvZdo++gqFeP+8IVlmS/JsWa0JLNaqJwWYg5eBQa6J+gfBqnosKuUslDy+xJfeIsNVT0oyjYaWanpxOGTZcFAgMBAAECggEBAKfqQfKAKeDKuZlrSay6PHE6n9bhhIDB3BGwGlBvYCYZA3WYIe5intMlVYZFyWhHyF98XJQK1blEm6RTKY8l8ZOOi58RYvGJefOfkRjOdybsFRw89vk72uNphz953ss3g7wSmNYyQqWUWLgQg0RuHfPwVv1RPLhi3b8cLZEScCD2m8fLD4QsRgFFFJuzrqcOa6r1uFcceOwzXe6WReB3z8IYyqGwHHrc69j1h4Q80Ik3yVSI6iIVyEfsU88vCIKY1XeGwPNeOa1YGM8gcKKSd3m+RHbTnvvEY0wuWp0RQtRwQJEJs8fM0Q6QU9iUmUItwDWbozGPMYi603rof8HYX0UCgYEA5r3+6wXg+PBGytjBM61Biv40iZC01Lh9Y3aUuPygWNzF0mcqcL1QKXDJc7Rx0EZCe6/WlndE6KvIhK9Uv+DlgRvsz8s0FN1lpmU5AqpyL6zKsssMBxX3GL3tHHmY/HMfENbChHMX8V0hnmWxqub72DkCoGE3ASxe7n7cKNcn4YcCgYEA0HsQo3oHEjSdz1wMFAoXnrNOI0mUieB+p4tlUodUHTj7Fc0DpNqbr+kQ2ko+Ex+3e2bMYaL4+V46MFArM0QE84bt9OL4QQCDVKjJNasLwPMxZ1pxxl6tFQpUKAfpeq1Cih48PAEfqt+t46rR94gY5a8BaRBqXL/azZpmaFHn1hMCgYEA0B/l1EVcNA4+PG8gbSVEbkN2TfLpOMojx1hJM0Yit6SDBpIpdME9kfN6sdO7qPoGs5vaOaPg0uFHn26qSdxoBnmSs6b9FijQrDK3JpFpJ1l7B6Qf+DA2zelZ+wovFN4d0klz8JYDraCsVzww8g8gcFqT5Odq/rLv+b0RhgDzpvcCgYBCZW8/ITXvTmKUD7SpDS3IlUJlaqGr55tusK5sQj0/sU4F+20Vq6m5tHhxjQOIA6nj2zFsRwMUKDZP/LhFc3xAMtDGFSMLUKaC595tATSVFt69E8j4bF6jPM7gkOSC9Xsjkc2NLq5vNR2kTFrigq2rOLkyRNAbCd8da/cBclRrkQKBgCR+t57b2IJRgJJIdGwpsZG9XlEKyvo8XY/dX0aEUZ4bdivFed8U5E+ad1gXg24mOZe7TI5JVwJHzEKXrbFtMoSdnGJ0D6jPpZbUv0cfMWo7aG04EzF2qqiUk1ggdRgnrCmGK5cPBGmHbdPPnWJumosbsrGYkx4bcticMejgIf0C";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjwekDzAkKp+KHLdJkNPyTIyyYhjmlQNMZRhoRdnt1Vv5p0xoK0rzDLOqcvinyrTAJglDxGzRSkCQTk6RqORY26s3QvSd68pru3wgjpnMURTs7BP1LcDEBW5l4Sj/bD6QPETCHX0zPHyujpRfIbxBa7Onfi4lflFSV1Ig/brPQDwZ0CuGMWJU+RH2M9wDTtl1mMMAy2cX2KxNSHFSmIdpxA8iLG9agWFfzrFMJeItbDE2/KUL4gqgMnIjt1sON4BjBYPRo/HQuf0o5qoa/1MDS8b4b0e7DwSS/pGVcziNTjvAedBd7dizaIU3Hkmw0dGK3dL3ygc/AkLvfC9x7IyT5wIDAQAB";

    // 服务器异步通知页面路径  需http://格式的完整路径，不能加?id支付宝开放平台开发助手
    //
    //密钥工具
    //生成密钥
    //签名
    //同步验签
    //异步验签
    //格式转换
    //密钥匹配
    //常用工具
    //我的API查询
    //API在线调试
    //一键搬家工具 NEW
    //智能反馈
    //开放社区
    //设置=123这类自定义参数，必须外网可以正常访问
    public static String notify_url = "http://localhost:8900/pay/returnNotify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，内网可以正常访问
    public static String return_url = "http://localhost:8900/pay/returnURL";

    // 签名方式
    public static String sign_type = "RSA2";

    // 字符编码格式
    public static String charset = "utf-8";

    // 支付宝网关
    public static String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    // 支付宝网关
    public static String log_path = "C:\\";




    /**
     * 写日志，方便测试（看网站需求，也可以改成把记录存入数据库）
     * @param sWord 要写入日志里的文本内容
     */
    public static void logResult(String sWord) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(log_path + "alipay_log_" + System.currentTimeMillis()+".txt");
            writer.write(sWord);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
