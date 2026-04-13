package org.example.lawnmpwer.robot.dto;
//前端控制信息接收
import lombok.Data;

@Data
public class FrontendControlMessage {

    private Long userId;

    private String robotId;

    private String targetIp;

    /**
     * move / camera / weed
     */
    private String type;

    /**
     * w / a / s / d / x / 1 / 2 / 3 / 5 / c / v
     */
    private String command;

    private Long timestamp;
}