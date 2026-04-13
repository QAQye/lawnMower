package org.example.lawnmpwer.robot.dto;

import lombok.Data;

@Data
public class RobotWsAckMessage {
//    后端和车通信的时候车给后端返回的信息
    /**
     * ack / status
     */
    private String type;

    private String robotId;

    private String command;

    private Boolean success;

    private String message;

    private Long timestamp;
}