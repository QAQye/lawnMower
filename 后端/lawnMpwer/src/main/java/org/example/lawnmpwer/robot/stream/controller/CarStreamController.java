package org.example.lawnmpwer.robot.stream.controller;

import org.example.lawnmpwer.robot.stream.service.CarStreamService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/car")
@CrossOrigin
public class CarStreamController {

    private final CarStreamService carStreamService;

    public CarStreamController(CarStreamService carStreamService) {
        this.carStreamService = carStreamService;
    }

    @GetMapping("/stream")
    public String getStream() {
        // 前端每 100 毫秒调用一次这里，直接返回内存中的最新 JSON 数据
        return carStreamService.getLatestPayload();
    }
}