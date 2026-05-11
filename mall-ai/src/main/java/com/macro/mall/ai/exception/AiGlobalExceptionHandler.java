package com.macro.mall.ai.exception;

import com.macro.mall.common.api.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * AI模块全局异常处理
 *
 * @author macro
 */
@Slf4j
@RestControllerAdvice("com.macro.mall.ai")
public class AiGlobalExceptionHandler {

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public CommonResult<?> handleException(Exception e) {
        log.error("AI服务异常", e);
        return CommonResult.failed("AI服务暂时不可用，请稍后重试");
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public CommonResult<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数错误: {}", e.getMessage());
        return CommonResult.validateFailed(e.getMessage());
    }
}
