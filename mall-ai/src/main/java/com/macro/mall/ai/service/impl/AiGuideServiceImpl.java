package com.macro.mall.ai.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.macro.mall.ai.config.AiGuideConfig;
import com.macro.mall.ai.domain.*;
import com.macro.mall.ai.service.AiGuideService;
import com.macro.mall.ai.service.ProductKnowledgeService;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * AI智能导购服务实现类
 *
 * @author macro
 */
@Slf4j
@Service
public class AiGuideServiceImpl implements AiGuideService {

    @Autowired
    private AiGuideConfig aiGuideConfig;

    @Autowired
    private ProductKnowledgeService productKnowledgeService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RateLimiterRegistry rateLimiterRegistry;

    @Autowired
    private ChatModel chatModel;

    private static final String SESSION_HISTORY_KEY = "ai:session:";
    private static final String SYSTEM_PROMPT = """
            你是一个专业的电商智能购物助手，名为"mall-ai助手"。你的任务是帮助用户找到合适的商品并解答相关问题。
            
            请遵循以下原则：
            1. 基于提供的商品信息回答问题，不要编造信息
            2. 回答要专业、友好、简洁
            3. 如果用户询问推荐，要根据用户需求推荐合适的商品并说明理由
            4. 如果商品信息不足，可以引导用户提供更多需求信息
            5. 对于价格、库存等敏感信息，要以实际数据为准
            6. 保持对话的连贯性，记住上下文
            
            当前时间：{current_time}
            """;

    @Override
    public ChatResponseDTO chat(ChatRequestDTO request) {
        try {
            // 限流保护
            RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("aiGuideRateLimiter");
            if (!rateLimiter.acquirePermission()) {
                return buildErrorResponse(request.getSessionId(), "请求过于频繁，请稍后再试");
            }

            // 检索相关商品（RAG）
            List<ProductInfoDTO> relevantProducts = productKnowledgeService.retrieveRelevantProducts(
                    request.getMessage(), 
                    aiGuideConfig.getRagTopK()
            );

            // 构建系统提示词
            String systemPrompt = buildSystemPrompt(relevantProducts);

            // 获取对话历史
            List<Message> history = getConversationHistory(request.getSessionId());

            // 构建消息列表
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(systemPrompt));
            messages.addAll(history);
            messages.add(new UserMessage(request.getMessage()));

            // 调用大模型
            Prompt prompt = new Prompt(messages);

            ChatResponse response = chatModel.call(prompt);
            String reply = response.getResult().getOutput().getContent();

            // 识别用户意图
            String intent = detectIntent(request.getMessage());

            // 保存对话历史
            saveConversationHistory(request.getSessionId(), request.getMessage(), reply);

            // 构建响应
            return ChatResponseDTO.builder()
                    .sessionId(request.getSessionId())
                    .reply(reply)
                    .recommendedProducts(relevantProducts)
                    .intent(intent)
                    .success(true)
                    .timestamp(System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            log.error("AI对话失败", e);
            return buildErrorResponse(request.getSessionId(), "抱歉，服务暂时不可用，请稍后重试");
        }
    }

    @Override
    public Flux<String> streamChat(ChatRequestDTO request) {
        try {
            // 限流保护
            RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("aiGuideRateLimiter");
            if (!rateLimiter.acquirePermission()) {
                return Flux.just("{\"error\":\"请求过于频繁，请稍后再试\"}");
            }

            // 检索相关商品（RAG）
            List<ProductInfoDTO> relevantProducts = productKnowledgeService.retrieveRelevantProducts(
                    request.getMessage(), 
                    aiGuideConfig.getRagTopK()
            );

            // 构建系统提示词
            String systemPrompt = buildSystemPrompt(relevantProducts);

            // 获取对话历史
            List<Message> history = getConversationHistory(request.getSessionId());

            // 构建消息列表
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(systemPrompt));
            messages.addAll(history);
            messages.add(new UserMessage(request.getMessage()));

            // 调用大模型（流式）
            Prompt prompt = new Prompt(messages);

            // 流式输出
            return chatModel.stream(prompt)
                    .map(chatResponse -> {
                        String content = chatResponse.getResult().getOutput().getContent();
                        return content != null ? content : "";
                    })
                    .doOnComplete(() -> {
                        // 流完成后保存对话历史（简化处理）
                        log.info("流式对话完成");
                    })
                    .onErrorResume(error -> {
                        log.error("流式对话出错", error);
                        return Flux.just("抱歉，服务暂时不可用");
                    });

        } catch (Exception e) {
            log.error("流式对话初始化失败", e);
            return Flux.just("抱歉，服务暂时不可用");
        }
    }

    @Override
    public String detectIntent(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "other";
        }

        String lowerMessage = message.toLowerCase();

        // 推荐意图
        if (lowerMessage.matches(".*推荐|有什么|想买|想要|看看|介绍.*")) {
            return "recommend";
        }

        // 问题意图
        if (lowerMessage.matches(".*什么|怎么|如何|哪里|是否|能不能|可以吗.*")) {
            return "question";
        }

        // 投诉意图
        if (lowerMessage.matches(".*投诉|差评|不满|生气|退款|退货.*")) {
            return "complaint";
        }

        return "other";
    }

    @Override
    public String generateRecommendReason(String productInfo, String userPreference) {
        try {
            String prompt = String.format("""
                    请根据以下商品信息和用户偏好，生成一段简短的推荐理由（50字以内）：
                    
                    商品信息：%s
                    用户偏好：%s
                    
                    推荐理由：
                    """, productInfo, userPreference);

            Prompt chatPrompt = new Prompt(new UserMessage(prompt));

            ChatResponse response = chatModel.call(chatPrompt);
            return response.getResult().getOutput().getContent();

        } catch (Exception e) {
            log.error("生成推荐理由失败", e);
            return "这款商品很适合您的需求";
        }
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(List<ProductInfoDTO> products) {
        String productContext = productKnowledgeService.formatProductContext(products);
        
        return SYSTEM_PROMPT.replace("{current_time}", 
                java.time.LocalDateTime.now().toString()) + "\n\n" + productContext;
    }

    /**
     * 获取对话历史
     */
    private List<Message> getConversationHistory(String sessionId) {
        List<Message> history = new ArrayList<>();
        
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return history;
        }

        String key = SESSION_HISTORY_KEY + sessionId;
        List<String> historyJson = redisTemplate.opsForList().range(key, 0, -1);

        if (historyJson != null && !historyJson.isEmpty()) {
            for (String json : historyJson) {
                try {
                    ChatMessageDTO msg = JSONUtil.toBean(json, ChatMessageDTO.class);
                    if ("user".equals(msg.getRole())) {
                        history.add(new UserMessage(msg.getContent()));
                    } else if ("assistant".equals(msg.getRole())) {
                        history.add(new org.springframework.ai.chat.messages.AssistantMessage(msg.getContent()));
                    }
                } catch (Exception e) {
                    log.warn("解析对话历史失败", e);
                }
            }
        }

        // 只保留最近的N条历史
        int maxSize = aiGuideConfig.getMaxHistorySize() * 2; // user + assistant
        if (history.size() > maxSize) {
            history = history.subList(history.size() - maxSize, history.size());
        }

        return history;
    }

    /**
     * 保存对话历史
     */
    private void saveConversationHistory(String sessionId, String userMessage, String assistantMessage) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return;
        }

        String key = SESSION_HISTORY_KEY + sessionId;

        // 保存用户消息
        ChatMessageDTO userMsg = new ChatMessageDTO("user", userMessage, System.currentTimeMillis());
        redisTemplate.opsForList().rightPush(key, JSONUtil.toJsonStr(userMsg));

        // 保存AI回复
        ChatMessageDTO assistantMsg = new ChatMessageDTO("assistant", assistantMessage, System.currentTimeMillis());
        redisTemplate.opsForList().rightPush(key, JSONUtil.toJsonStr(assistantMsg));

        // 限制历史记录长度
        Long size = redisTemplate.opsForList().size(key);
        if (size != null && size > aiGuideConfig.getMaxHistorySize() * 2) {
            redisTemplate.opsForList().trim(key, size - aiGuideConfig.getMaxHistorySize() * 2, -1);
        }

        // 设置过期时间（24小时）
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
    }

    /**
     * 构建错误响应
     */
    private ChatResponseDTO buildErrorResponse(String sessionId, String errorMessage) {
        return ChatResponseDTO.builder()
                .sessionId(sessionId)
                .success(false)
                .errorMessage(errorMessage)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
