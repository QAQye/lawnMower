package org.example.lawnmpwer.robot.service;

import org.example.lawnmpwer.robot.dto.RobotDetectionStatsMessage;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RobotDetectionStatsService {

    private final Map<String, RobotDetectionStatsMessage> latestStatsMap = new ConcurrentHashMap<>();

    public void saveLatest(RobotDetectionStatsMessage msg) {
        if (msg == null || msg.getRobotId() == null || msg.getRobotId().isBlank()) {
            return;
        }

        RobotDetectionStatsMessage copy = new RobotDetectionStatsMessage();
        copy.setRobotId(msg.getRobotId());
        copy.setCropCount(msg.getCropCount() == null ? 0 : msg.getCropCount());
        copy.setWeedCount(msg.getWeedCount() == null ? 0 : msg.getWeedCount());
        copy.setTimestamp(msg.getTimestamp() == null ? System.currentTimeMillis() : msg.getTimestamp());

        latestStatsMap.put(copy.getRobotId(), copy);
    }

    public RobotDetectionStatsMessage getLatest(String robotId) {
        return latestStatsMap.get(robotId);
    }
}