package org.example.lawnmpwer.robot.stream.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RobotStreamController {
    /**
     * 获取推流/拉流地址
     * @param robotId 小车ID，默认 robot1
     * @param type    流类型，例如 cam(原始摄像头) 或 ai(AI检测流)，默认 cam
     */
    @GetMapping("/api/robot/stream-url")
    public Map<String, String> getStreamUrl(
            @RequestParam(defaultValue = "robot1") String robotId,
            @RequestParam(defaultValue = "cam") String type
    ) {
        Map<String, String> result = new HashMap<>();

        // 动态拼接流名，例如：robot1_cam 或 robot1_ai
        String streamName = robotId + "_" + type;
        result.put("streamName", streamName);

        // 小车端推流地址
        result.put("rtmpPushUrl", "rtmp://localhost:1935/live/" + streamName);

        // 前端播放地址
        result.put("flvUrl", "http://localhost:1985/live/" + streamName + ".flv");
        result.put("hlsUrl", "http://localhost:1985/live/" + streamName + ".m3u8");
        result.put("whepUrl", "http://localhost:1985/rtc/v1/whep/?app=live&stream=" + streamName);

        return result;
    }
}