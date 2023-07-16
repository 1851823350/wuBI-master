package com.atwj.wubi.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class AIManagerTest {
    @Resource
    private AIManager aiManager;

    @Test
    void doChart() {
        String result = aiManager.doChart(1651468516836098050L, "你好，世界");
        System.out.println(result);
    }
}