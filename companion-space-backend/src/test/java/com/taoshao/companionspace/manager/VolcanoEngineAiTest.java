package com.taoshao.companionspace.manager;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @Author taoshao
 * @Date 2024/9/21
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VolcanoEngineAiTest {

    @Autowired
    private VolcanoEngineAi volcanoEngineAi;

    @Test
    void volcanoEngine() {
        String message = volcanoEngineAi.volcanoEngine("请用java实现一个二分查找");
        System.out.println(message);
    }
}