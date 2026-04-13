package org.example.lawnmpwer.robot.service;

import org.example.lawnmpwer.robot.dto.RobotDetectionStatsMessage;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RobotDetectionStatsService {

    private final Map<String, RobotDetectionStatsMessage> latestStatsMap = new ConcurrentHashMap<>();

    public void updateStats(RobotDetectionStatsMessage stats) {
        if (stats != null && stats.getRobotId() != null && !stats.getRobotId().isBlank()) {
            latestStatsMap.put(stats.getRobotId(), stats);
        }
    }
    public RobotDetectionStatsMessage getLatestStats(String robotId) {
        return latestStatsMap.get(robotId);
    }
}