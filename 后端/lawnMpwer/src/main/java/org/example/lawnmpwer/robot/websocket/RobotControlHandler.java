package org.example.lawnmpwer.robot.websocket;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.DataInputStream;
import java.io.OutputStream;
import java.net.Socket;

public class RobotControlHandler extends TextWebSocketHandler {

    // 树莓派的 IP 和端口
    private static final String ROBOT_IP = "192.168.1.11";
    private static final int CMD_PORT = 65432;
    private static final int VIDEO_PORT = 65433;

    // 保存当前的 WebSocket 会话
    private static WebSocketSession currentSession;

    // 连接树莓派的 Socket
    private Socket cmdSocket;
    private Socket videoSocket;
    private OutputStream cmdOutputStream;
    private Thread videoThread;
    private boolean isRunning = false;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("前端已连接: " + session.getId());
        currentSession = session;
        connectToRobot();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 收到前端 Vue 发来的按键指令（W/A/S/D）
        String payload = message.getPayload();
        if (cmdOutputStream != null) {
            try {
                cmdOutputStream.write(payload.getBytes());
                System.out.println("Java 收到前端指令: " + payload);
                cmdOutputStream.flush();
            } catch (Exception e) {
                System.err.println("发送指令失败: " + e.getMessage());
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("前端断开连接");
        disconnectRobot();
    }

    private void connectToRobot() {
        try {
            // 连接控制端口
            cmdSocket = new Socket(ROBOT_IP, CMD_PORT);
            cmdOutputStream = cmdSocket.getOutputStream();
            System.out.println("成功连接到小车控制端口");

            // 连接视频端口并启动接收线程
            videoSocket = new Socket(ROBOT_IP, VIDEO_PORT);
            isRunning = true;
            videoThread = new Thread(this::receiveVideoLoop);
            videoThread.start();
            System.out.println("成功连接到小车视频端口");

        } catch (Exception e) {
            System.err.println("连接树莓派失败: " + e.getMessage());
        }
    }

    private void disconnectRobot() {
        isRunning = false;
        try {
            if (cmdSocket != null) {
                cmdSocket.close();
            }
            if (videoSocket != null) {
                videoSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 循环读取树莓派发来的视频流，并转发给前端
    private void receiveVideoLoop() {
        try (DataInputStream dis = new DataInputStream(videoSocket.getInputStream())) {
            while (isRunning && currentSession != null && currentSession.isOpen()) {
                // Python 端使用 struct.pack(">Q", len)
                // 这里按 8 字节大端序读取长度

                long length = dis.readLong();

                byte[] imageBytes = new byte[(int) length];
                dis.readFully(imageBytes);

                currentSession.sendMessage(new BinaryMessage(imageBytes));
            }
        } catch (Exception e) {
            System.err.println("视频传输中断: " + e.getMessage());
        }
    }
}