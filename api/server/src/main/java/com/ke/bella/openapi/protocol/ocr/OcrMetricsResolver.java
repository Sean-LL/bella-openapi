package com.ke.bella.openapi.protocol.ocr;

import java.util.List;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.protocol.OpenapiResponse;
import com.ke.bella.openapi.protocol.metrics.MetricsResolver;
import lombok.extern.slf4j.Slf4j;

/**
 * OCR指标解析器
 */
@Component
@Slf4j
public class OcrMetricsResolver implements MetricsResolver {
    
    private boolean canResolve(OpenapiResponse response) {
        return response != null && response.getError() != null && response.getError().getHttpCode() >= 500;
    }

    @Override
    public Integer resolveUnavailableSeconds(EndpointProcessData processData) {
        OpenapiResponse response = processData.getResponse();
        int seconds = 60; // OCR服务默认重试间隔：60秒
        
        if (canResolve(response)) {
            // 根据错误类型调整重试间隔
            int httpCode = response.getError().getHttpCode();
            if (httpCode == 503) {
                seconds = 120; // 服务不可用，延长重试间隔
            } else if (httpCode == 502 || httpCode == 504) {
                seconds = 30;  // 网关错误，缩短重试间隔
            }
            log.warn("OCR service unavailable, HTTP code: {}, retry after {} seconds", httpCode, seconds);
        }
        
        return seconds;
    }

    @Override
    public List<String> metricsName() {
        return Lists.newArrayList("ttlt", "request_count", "success", "side");
    }

    @Override
    public String support() {
        return "/v*/ocr/idcard";
    }
}
