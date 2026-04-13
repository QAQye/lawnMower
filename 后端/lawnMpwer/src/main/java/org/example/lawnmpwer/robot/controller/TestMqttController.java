package org.example.lawnmpwer.robot.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestMqttController {

    @GetMapping("/api/test/mqtt-backend")
    public Map<String, Object> test() {
        return Map.of(
                "success", true,
                "message", "Spring Boot 后端运行正常，MQTT 接收器已加载"
        );
    }
}