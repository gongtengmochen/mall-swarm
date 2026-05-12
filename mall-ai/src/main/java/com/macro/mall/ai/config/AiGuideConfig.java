package com.macro.mall.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI智能导购配置类
 *
 * @author macro
 */
@Data
@Component
@ConfigurationProperties(prefix = "mall.ai")
public class AiGuideConfig {

    /**
     * 是否启用流式输出
     */
    private Boolean enableStream = true;

    /**
     * 最大对话历史条数
     */
    private Integer maxHistorySize = 10;

    /**
     * RAG检索Top K
     */
    private Integer ragTopK = 5;

    /**
     * API超时时间（毫秒）
     */
    private Long apiTimeout = 30000L;

    /**
     * 最大重试次数
     */
    private Integer maxRetries = 3;

    /**
     * 限流QPS
     */
    private Integer rateLimitQps = 10;
}
