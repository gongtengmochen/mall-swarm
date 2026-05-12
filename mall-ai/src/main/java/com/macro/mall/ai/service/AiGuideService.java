package com.macro.mall.ai.service;

import com.macro.mall.ai.domain.ChatRequestDTO;
import com.macro.mall.ai.domain.ChatResponseDTO;
import reactor.core.publisher.Flux;

/**
 * AI智能导购服务接口
 *
 * @author macro
 */
public interface AiGuideService {

    /**
     * 普通对话（非流式）
     * @param request 对话请求
     * @return 对话响应
     */
    ChatResponseDTO chat(ChatRequestDTO request);

    /**
     * 流式对话
     * @param request 对话请求
     * @return 流式响应
     */
    Flux<String> streamChat(ChatRequestDTO request);

    /**
     * 识别用户意图
     * @param message 用户消息
     * @return 意图类型：question/recommend/complaint/other
     */
    String detectIntent(String message);

    /**
     * 生成商品推荐理由
     * @param productInfo 商品信息
     * @param userPreference 用户偏好
     * @return 推荐理由
     */
    String generateRecommendReason(String productInfo, String userPreference);
}
