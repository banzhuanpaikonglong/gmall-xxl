package com.littlestore.shoppingmall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // 相当于beans.xml
public class RedisConfig {

    // 获取host,port,database,timeOut
    @Value("${spring.redis.host:disable}") // :disable 表如果在配置文件中没有获取到数据，则port 为默认值disable
    private String host;

    @Value("${spring.redis.port:0}")
    private int port;

    @Value("${spring.redis.database:0}")
    private int database;

    @Value("${spring.redis.timeOut:0}")
    private int timeOut;

    // 调用RedisUtil 中初始化initJedisPool 方法
    // 表示 <bean class="com.atguigu.gmall0105.config.RedisUtil"> </bean> 把RedisUtil 放入到了spring 容器中！
    @Bean
    public RedisUtil getRedisUtil(){
        if ("disable".equals(host)){
            return null;
        }

        RedisUtil redisUtil = new RedisUtil();
        // 初始化赋值
        redisUtil.initJedisPool(host,port,database,timeOut);
        return redisUtil;
    }

}
