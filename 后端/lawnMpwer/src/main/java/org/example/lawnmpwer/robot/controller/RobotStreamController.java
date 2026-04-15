package org.example.lawnmpwer.robot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/robot")
public class RobotStreamController {
    // 这里改成你自己的 SRS 服务器地址
    private static final String SRS_HOST = "localhost";
    // RTMP 推流端口
    private static final int RTMP_PORT = 1935;
    // HTTP-FLV / HLS / WHEP 访问端口
    private static final int HTTP_PORT = 1985;
    // SRS 应用名
    private static final String APP_NAME = "live";
    @GetMapping("/stream-url")
    public Map<String, String> getStreamUrl(
            @RequestParam(defaultValue = "robot1") String robotId,
            @RequestParam(defaultValue = "cam") String type) {
        Map<String, String> result = new HashMap<>();
        // 动态生成流名称
        String streamName = robotId + "_" + type;
        result.put("robotId", robotId);
        result.put("type", type);
        result.put("streamName", streamName);
        // RTMP 推流地址
        result.put("rtmpPushUrl",
                "rtmp://" + SRS_HOST + ":" + RTMP_PORT + "/" + APP_NAME + "/" + streamName);
        // HTTP-FLV 播放地址
        result.put("flvUrl",
                "http://" + SRS_HOST + ":" + HTTP_PORT + "/" + APP_NAME + "/" + streamName + ".flv");
        // HLS 播放地址
        result.put("hlsUrl",
                "http://" + SRS_HOST + ":" + HTTP_PORT + "/" + APP_NAME + "/" + streamName + ".m3u8");
        // WHEP / WebRTC 播放地址
        result.put("whepUrl",
                "http://" + SRS_HOST + ":" + HTTP_PORT + "/rtc/v1/whep/?app=" + APP_NAME + "&stream=" + streamName);
        return result;
    }
}