package com.ke.bella.openapi.protocol.ocr;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.protocol.log.EndpointLogHandler;
import com.ke.bella.openapi.utils.DateTimeUtils;

import lombok.Data;

/**
 * OCR日志处理器
 */
@Component
public class OcrLogHandler implements EndpointLogHandler {

    @Override
    public void process(EndpointProcessData processData) {
        OcrIdcardRequest request = (OcrIdcardRequest) processData.getRequest();
        OcrIdcardResponse response = null;

        if (processData.getResponse() instanceof OcrIdcardResponse) {
            response = (OcrIdcardResponse) processData.getResponse();
        }

        // 计算处理时间
        long startTime = processData.getRequestTime();
        int ttlt = (int) (DateTimeUtils.getCurrentSeconds() - startTime);

        // 构建指标数据
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("ttlt", ttlt);
        metrics.put("request_count", 1); // OCR按次计费
        metrics.put("success", response != null && response.getError_code() == null);

        if (response != null) {
            metrics.put("side", response.getSide()); // 记录识别的身份证面
        }

        processData.setMetrics(metrics);

        // 构建使用量信息（用于计费）
        OcrUsage usage = new OcrUsage();
        usage.setRequestCount(1);
        usage.setSuccessful(response != null && response.getError_code() == null);
        processData.setUsage(usage);
    }

    @Override
    public String endpoint() {
        return "/v*/ocr/idcard";
    }

    @Data
    public static class OcrUsage {
        private int requestCount;           // 请求次数
        private boolean successful;         // 是否成功
    }
}
