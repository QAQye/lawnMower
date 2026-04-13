package org.example.lawnmpwer.robot.controller;

import lombok.RequiredArgsConstructor;
import org.example.lawnmpwer.robot.dto.RobotDetectionStatsMessage;
import org.example.lawnmpwer.robot.service.RobotDetectionStatsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/robot/detection-stats")
@RequiredArgsConstructor
public class RobotDetectionStatsController {

    private final RobotDetectionStatsService statsService;

    @PostMapping("/report")
    public Map<String, Object> report(@RequestBody RobotDetectionStatsMessage stats) {
        statsService.updateStats(stats);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "统计上报成功");
        return result;
    }

    @GetMapping("/latest")
    public Map<String, Object> latest(@RequestParam(defaultValue = "robot1") String robotId) {
        RobotDetectionStatsMessage stats = statsService.getLatestStats(robotId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);

        if (stats == null) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("robotId", robotId);
            empty.put("cropCount", 0);
            empty.put("weedCount", 0);
            empty.put("timestamp", System.currentTimeMillis());
            result.put("data", empty);
        } else {
            result.put("data", stats);
        }

        return result;
    }
}