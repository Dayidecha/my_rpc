package com.haha.serviceImpl;

import com.haha.Hello;
import com.haha.HelloService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloImpl implements HelloService {
    static {
        System.out.println("HelloServiceImpl被创建");
    }
    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl收到: {}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl返回: {}.", result);
        return result;
    }
}
