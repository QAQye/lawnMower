package org.example.lawnmpwer.robot.dto;
//向Java端注册车信息。会分配唯一的id
import lombok.Data;

@Data
public class RobotRegisterMessage {

    /**
     * 固定为 register
     */
    private String type;

    /**
     * 小车唯一标识
     */
    private String robotId;
}