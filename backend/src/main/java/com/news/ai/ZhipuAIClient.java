package com.news.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ZhipuAI API客户端
 * 用于调用智谱AI的GLM-4模型生成新闻摘要
 */
@Component
@Slf4j
public class ZhipuAIClient {

    @Value("${zhipuai.api-key:}")
    private String apiKey;

    @Value("${zhipuai.api-url:https://open.bigmodel.cn/api/paas/v4/chat/completions}")
    private String apiUrl;

    @Value("${zhipuai.model:glm-4}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ZhipuAIClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 生成新闻摘要
     * 
     * @param newsTitle 新闻标题
     * @param newsContent 新闻内容
     * @return 生成的摘要文本
     */
    public String generateSummary(String newsTitle, String newsContent) {
        log.info("Generating summary for news: {}", newsTitle);

        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your-api-key-here")) {
            log.warn("ZhipuAI API key not configured, returning mock summary");
            return generateMockSummary(newsTitle, newsContent);
        }

        try {
            // 构建请求体
            String prompt = buildSummaryPrompt(newsTitle, newsContent);
            Map<String, Object> requestBody = buildRequestBody(prompt);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // 调用API
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            // 解析响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseSummaryFromResponse(response.getBody());
            } else {
                log.error("ZhipuAI API returned non-OK status: {}", response.getStatusCode());
                return generateMockSummary(newsTitle, newsContent);
            }

        } catch (Exception e) {
            log.error("Failed to call ZhipuAI API: {}", e.getMessage(), e);
            return generateMockSummary(newsTitle, newsContent);
        }
    }

    /**
     * 构建摘要提示词
     */
    private String buildSummaryPrompt(String title, String content) {
        // 限制内容长度，避免超过token限制
        String truncatedContent = content.length() > 2000 
                ? content.substring(0, 2000) + "..." 
                : content;

        return String.format(
                "请为以下新闻生成一段简洁的摘要（100-150字）：\n\n" +
                "标题：%s\n\n" +
                "内容：%s\n\n" +
                "要求：\n" +
                "1. 提炼核心要点\n" +
                "2. 语言简洁客观\n" +
                "3. 保持中文表达\n" +
                "4. 控制在100-150字",
                title,
                truncatedContent
        );
    }

    /**
     * 构建API请求体
     */
    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        
        // 构建messages
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        
        body.put("messages", List.of(userMessage));
        body.put("temperature", 0.7);
        body.put("max_tokens", 300);
        
        return body;
    }

    /**
     * 从响应中解析摘要
     */
    private String parseSummaryFromResponse(Map<String, Object> responseBody) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                if (message != null) {
                    String content = (String) message.get("content");
                    if (content != null && !content.isEmpty()) {
                        log.info("Successfully generated summary from ZhipuAI");
                        return content.trim();
                    }
                }
            }
            
            log.warn("Unable to parse summary from response, using mock");
            return "摘要生成失败";
            
        } catch (Exception e) {
            log.error("Error parsing ZhipuAI response: {}", e.getMessage());
            return "摘要解析失败";
        }
    }

    /**
     * 生成模拟摘要（当API不可用时）
     */
    private String generateMockSummary(String title, String content) {
        log.info("Generating mock summary (API key not configured)");
        
        // 简单的摘要生成逻辑：取标题+内容前100字
        String contentPreview = content.length() > 100 
                ? content.substring(0, 100) 
                : content;
        
        return String.format(
                "【自动摘要】%s。%s...",
                title,
                contentPreview
        );
    }

    /**
     * 检查API是否可用
     */
    public boolean isApiAvailable() {
        return apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your-api-key-here");
    }
}

