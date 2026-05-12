package com.macro.mall.ai.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * AI智能导购-对话响应DTO
 *
 * @author macro
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * AI回复内容
     */
    private String reply;

    /**
     * 推荐商品列表
     */
    private List<ProductInfoDTO> recommendedProducts;

    /**
     * 用户意图：question/recommend/complaint/other
     */
    private String intent;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 时间戳
     */
    private Long timestamp;
}
