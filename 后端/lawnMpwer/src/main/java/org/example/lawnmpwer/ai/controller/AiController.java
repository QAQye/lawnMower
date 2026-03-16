package org.example.lawnmpwer.ai.controller;

import org.example.lawnmpwer.ai.dto.UserQuery;
import org.example.lawnmpwer.ai.service.AiService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/ai")
@CrossOrigin
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping(value = "/ask", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter askDeepSeekStream(@RequestBody UserQuery query) {
//        把用户询问的内容包装在里面
        return aiService.askDeepSeekStream(query);
    }
}