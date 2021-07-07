package com.hikvision.redis.controller;

import com.hikvision.redis.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author renpeiqian
 * @date 2021/6/25 15:15
 */
@RestController
@RequestMapping("/hik")
@Slf4j
public class RedisController {

    @Autowired
    RedisTemplate redisTemplate;

    @RequestMapping("/get")
    public User get(){
        User user = (User) redisTemplate.opsForValue().get("username");
        return user;
    }

    @RequestMapping("/set")
    public String set(@RequestBody User user){
        log.info("user: {}",user);
        redisTemplate.opsForValue().set("username",user);
        return  "插入成功";
    }

}
