package org.example.lawnmpwer.robot.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class MqttSender {

    private static final Logger log = LoggerFactory.getLogger(MqttSender.class);

    @Value("${mqtt.broker-url}")
    private String brokerUrl;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Value("${mqtt.qos:1}")
    private int qos;

    private MqttClient client;
    private MqttConnectOptions options;

    @PostConstruct
    public void init() {
        try {
            String realClientId = clientId + "-pub-" + UUID.randomUUID().toString().substring(0, 8);
            client = new MqttClient(brokerUrl, realClientId, new MemoryPersistence());

            options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);

            if (username != null && !username.isBlank()) {
                options.setUserName(username);
            }
            if (password != null && !password.isBlank()) {
                options.setPassword(password.toCharArray());
            }

            client.connect(options);
            log.info("MQTT 发布端连接成功: broker={}, clientId={}", brokerUrl, realClientId);
        } catch (Exception e) {
            log.error("MQTT 发布端初始化失败", e);
            throw new RuntimeException("MQTT 发布端初始化失败", e);
        }
    }

    public synchronized void publish(String topic, String payload) {
        try {
            ensureConnected();

            MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            message.setQos(qos);

            client.publish(topic, message);
            log.info("MQTT 发布成功: topic={}, payload={}", topic, payload);
        } catch (Exception e) {
            log.error("MQTT 发布失败: topic={}", topic, e);
            throw new RuntimeException("MQTT 发布失败", e);
        }
    }

    private void ensureConnected() throws Exception {
        if (client == null) {
            throw new IllegalStateException("MQTT client 未初始化");
        }
        if (!client.isConnected()) {
            client.connect(options);
            log.info("MQTT 重新连接成功: broker={}", brokerUrl);
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            if (client != null) {
                if (client.isConnected()) {
                    client.disconnect();
                }
                client.close();
            }
            log.info("MQTT 发布端已关闭");
        } catch (Exception e) {
            log.error("关闭 MQTT 发布端失败", e);
        }
    }
}