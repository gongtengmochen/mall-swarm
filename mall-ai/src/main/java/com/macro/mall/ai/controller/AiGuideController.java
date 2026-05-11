package com.macro.mall.ai.controller;

import com.macro.mall.ai.domain.ChatRequestDTO;
import com.macro.mall.ai.domain.ChatResponseDTO;
import com.macro.mall.ai.service.AiGuideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * AI智能导购控制器
 *
 * @author macro
 */
@Tag(name = "AiGuideController", description = "AI智能导购接口")
@RestController
@RequestMapping("/ai/guide")
public class AiGuideController {

    @Autowired
    private AiGuideService aiGuideService;

    /**
     * 普通对话（非流式）
     */
    @Operation(summary = "AI对话（非流式）")
    @PostMapping("/chat")
    public ChatResponseDTO chat(@RequestBody ChatRequestDTO request) {
        return aiGuideService.chat(request);
    }

    /**
     * 流式对话
     */
    @Operation(summary = "AI对话（流式输出）")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody ChatRequestDTO request) {
        return aiGuideService.streamChat(request);
    }

    /**
     * 获取热门商品推荐
     */
    @Operation(summary = "获取热门商品推荐")
    @GetMapping("/hot-products")
    public ChatResponseDTO getHotProducts(@RequestParam(defaultValue = "10") Integer limit) {
        // 这里可以扩展为返回商品列表
        return ChatResponseDTO.builder()
                .success(true)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
