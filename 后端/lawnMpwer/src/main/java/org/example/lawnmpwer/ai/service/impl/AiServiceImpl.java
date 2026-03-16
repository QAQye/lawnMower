//询问ai的实现类主要是做核心流式对话服务主要作用是充当一个“中转站（Proxy）”：
// 接收前端的提问，去请求 DeepSeek 的大模型接口，然后把 DeepSeek 吐出来的数据，
// 像流水一样一点点实时转发给前端。
package org.example.lawnmpwer.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.lawnmpwer.ai.dto.UserQuery;
import org.example.lawnmpwer.ai.service.AiService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AiServiceImpl implements AiService {

    // 先沿用你现在的写法，后面再改配置文件
    private static final String API_KEY = "sk-6edcf8f03aa7468881a54cb6e58db38b";
    private static final String API_URL = "https://api.deepseek.com/chat/completions";

    // Jackson 的 JSON 处理工具，用来把 Java Map 变成 JSON 字符串
    private final ObjectMapper objectMapper = new ObjectMapper();
    // 创建一个缓存线程池。因为等待 AI 回复是一个耗时操作，如果直接在主线程做，会卡死 Tomcat 的请求处理线程。
    // 所以我们需要把这个任务扔到子线程里去异步执行。
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public SseEmitter askDeepSeekStream(UserQuery query) {
        // 0L 代表永不超时。这是后端用来向前端源源不断推送数据的核心对象
        SseEmitter emitter = new SseEmitter(0L);
// 将具体的网络请求扔进线程池中异步执行
        executor.execute(() -> {
            try {
//                这里调用的 query.message()，就是 Java 编译器自动为 UserQuery 这个 record 生成的获取 message 字段的方法。
                String userMessage = query.message();
                // 【4. 构建给 DeepSeek 的请求参数】
                Map<String, Object> bodyMap = Map.of(
                        "model", "deepseek-chat",
//                        开启流式输出
                        "stream", true,
                        "messages", List.of(
                                Map.of("role", "system", "content", "你是一个智能割草机器人助手。"),
                                Map.of("role", "user", "content", userMessage)
                        )
                );
            // 把 Map 转换成 JSON 格式的字符串
                String requestBody = objectMapper.writeValueAsString(bodyMap);
                // 【5. 发起 HTTP 请求】
                // 建立与 DeepSeek 服务器的连接
                HttpURLConnection conn = (HttpURLConnection) URI.create(API_URL).toURL().openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    // 把刚刚拼好的 requestBody 发送给 DeepSeek 服务器
                    os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }
                // 获取 HTTP 响应状态码（200代表成功）
                int status = conn.getResponseCode();
                // 【6. 异常情况处理（状态码不是200）】
                if (status != 200) {
                    try (BufferedReader errorReader = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                        // 读出完整的错误信息
                        StringBuilder errorText = new StringBuilder();
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            errorText.append(line);
                        }

                        emitter.send(SseEmitter.event().data("{\"choices\":[{\"delta\":{\"content\":\"调用失败："
                                + escapeJson(errorText.toString()) + "\"}}]}"));
                        emitter.send(SseEmitter.event().data("[DONE]"));
                    }
                    emitter.complete();
                    return;
                }
                // 【7. 成功情况处理：核心流式读取】
                // 读取 DeepSeek 返回的数据流
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    // line = reader.readLine() 会阻塞等待，直到 DeepSeek 吐出新的一行数据
                    String line;
                    // 标准的 SSE 数据格式都是以 "data: " 开头的，忽略空行或其他格式
                    while ((line = reader.readLine()) != null) {
                        if (!line.startsWith("data: ")) {
                            continue;
                        }
                        // 截取掉 "data: " 前缀，拿到真正的数据体（一个 JSON 字符串，或者 [DONE]）
                        String data = line.substring(6).trim();
                        // DeepSeek 官方约定，当返回 [DONE] 时，说明 AI 这句话已经完全说完了
                        if ("[DONE]".equals(data)) {
                            emitter.send(SseEmitter.event().data("[DONE]"));
                            break;
                        }

                        // 关键改动：
                        // 直接把 DeepSeek 返回的 JSON 片段原样转发给前端
                        // 这里直接把 DeepSeek 返回的 JSON 字符串（例如 {"choices":[{"delta":{"content":"好"}}]} ）
                        // 原封不动地通过 SSE 转发给我们的前端。
                        emitter.send(SseEmitter.event().data(data));
                    }
                }
// 所有数据读取并转发完毕，正常关闭发射器
                emitter.complete();

            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().data(
                            "{\"choices\":[{\"delta\":{\"content\":\"服务器异常：" + escapeJson(e.getMessage()) + "\"}}]}"
                    ));
                    emitter.send(SseEmitter.event().data("[DONE]"));
                } catch (Exception ignored) {
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
    /**
     * 将字符串进行 JSON 转义
     * 因为在处理异常情况时，后端手动拼装了 JSON 字符串 ("{\"choices...}")。
     * 如果报错信息里含有双引号 (") 或换行符 (\n)，会导致拼接出来的 JSON 格式损坏。
     * 所以需要这个方法把它们转义掉。
     */
    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}