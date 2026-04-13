package org.example.lawnmpwer.robot.dto;
//小车控制信息
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RobotControlMessage {

    private String robotId;

    /**
     * w / a / s / d / x / 1 / 2 / 3 / 5 / c / v
     */
    private String command;
}