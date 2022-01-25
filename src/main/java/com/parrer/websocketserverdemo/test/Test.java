package com.parrer.websocketserverdemo.test;

import com.parrer.util.LogUtil;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class Test implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        LogUtil.info("new scripts!");
    }
}
