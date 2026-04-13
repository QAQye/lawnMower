package org.example.lawnmpwer.robot.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RobotWsControlMessage {
//后端websocket给车传递的命令信息
    /**
     * 固定为 control
     */
    private String type;

    private String robotId;

    /**
     * w / a / s / d / x / 1 / 2 / 3 / 5 / c / v
     */
    private String command;

    private Long timestamp;
}
