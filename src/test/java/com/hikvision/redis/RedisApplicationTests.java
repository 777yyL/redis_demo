package com.hikvision.redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class RedisApplicationTests {

    @Autowired
    RedisTemplate redisTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    public void test(){
        redisTemplate.opsForValue().set("username","hello,hikvision");
        System.out.println(redisTemplate.opsForValue().get("username").toString());
    }
}
