package com.macro.mall.ai.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI智能导购-对话请求DTO
 *
 * @author macro
 */
@Data
public class ChatRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户消息
     */
    private String message;

    /**
     * 是否启用流式输出
     */
    private Boolean stream = true;

    /**
     * 对话历史
     */
    private List<ChatMessageDTO> history;
}
