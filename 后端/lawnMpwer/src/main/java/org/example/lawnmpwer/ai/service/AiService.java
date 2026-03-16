package org.example.lawnmpwer.ai.service;

import org.example.lawnmpwer.ai.dto.UserQuery;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AiService {
    SseEmitter askDeepSeekStream(UserQuery query);
}