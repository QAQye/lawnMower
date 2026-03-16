package org.example.lawnmpwer.ai.dto;

import java.util.List;
import java.util.Map;

public record DeepSeekRequest(String model, List<Map<String, String>> messages) {}