package com.ke.bella.openapi.protocol.ocr;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * OCR错误处理工具类
 */
@Component
public class OcrErrorHandler {

    public static final Map<String, String> MEGVII_ERROR_MAPPING = new HashMap<>();

    static {
        // 旷视API错误码映射
        MEGVII_ERROR_MAPPING.put("ID_CARD_NOT_FOUND", "图片中没有找到身份证");
        MEGVII_ERROR_MAPPING.put("INVALID_IMAGE_SIZE", "图片尺寸不符合要求");
        MEGVII_ERROR_MAPPING.put("IMAGE_ERROR_UNSUPPORTED_FORMAT", "不支持的图片格式");
        MEGVII_ERROR_MAPPING.put("BAD_ARGUMENTS", "参数错误");
        MEGVII_ERROR_MAPPING.put("MISSING_ARGUMENTS", "缺少必要参数");
        MEGVII_ERROR_MAPPING.put("AUTHENTICATION_ERROR", "API密钥验证失败");
        MEGVII_ERROR_MAPPING.put("AUTHORIZATION_ERROR", "API权限不足");
        MEGVII_ERROR_MAPPING.put("CONCURRENCY_LIMIT_EXCEEDED", "并发请求超限");
        MEGVII_ERROR_MAPPING.put("API_NOT_FOUND", "API不存在");
        MEGVII_ERROR_MAPPING.put("INTERNAL_ERROR", "服务内部错误");
    }

    public static String mapMegviiError(String megviiErrorCode) {
        return MEGVII_ERROR_MAPPING.getOrDefault(megviiErrorCode, "未知错误: " + megviiErrorCode);
    }
}
