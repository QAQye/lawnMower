package org.example.lawnmpwer.robot.controller;

import org.example.lawnmpwer.robot.dto.RobotDetectionStatsMessage;
import org.example.lawnmpwer.robot.service.RobotDetectionStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/robot/detection-stats")
public class RobotDetectionStatsController {

    private final RobotDetectionStatsService robotDetectionStatsService;

    public RobotDetectionStatsController(RobotDetectionStatsService robotDetectionStatsService) {
        this.robotDetectionStatsService = robotDetectionStatsService;
    }

    @GetMapping("/latest")
    public Map<String, Object> getLatestStats(
            @RequestParam(defaultValue = "robot1") String robotId) {

        RobotDetectionStatsMessage latest = robotDetectionStatsService.getLatest(robotId);

        Map<String, Object> result = new HashMap<>();
        result.put("robotId", robotId);

        if (latest == null) {
            result.put("cropCount", 0);
            result.put("weedCount", 0);
            result.put("timestamp", System.currentTimeMillis());
            result.put("hasData", false);
        } else {
            result.put("cropCount", latest.getCropCount());
            result.put("weedCount", latest.getWeedCount());
            result.put("timestamp", latest.getTimestamp());
            result.put("hasData", true);
        }

        return result;
    }
}