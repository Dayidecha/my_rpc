package com.haha.serviceImpl;

import com.haha.Hello;
import com.haha.HelloService;

import java.io.Serializable;

public class Hello2Impl implements HelloService, Serializable {


    @Override
    public String hello(Hello hello) {
        return "hahahahahahaha,wo shi hello2 "+hello.toString();
    }
}
