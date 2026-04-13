package org.example.lawnmpwer.robot.dto;
//杂草检测信息
import lombok.Data;

@Data
public class RobotDetectionStatsMessage {

    private String robotId;
    private Integer cropCount;
    private Integer weedCount;
    private Long timestamp;
}