package com.ke.bella.openapi.protocol.ocr;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 旷视科技OCR属性配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MegviiOcrProperty extends OcrProperty {
    private String apiKey;                  // 旷视API Key
    private String apiSecret;               // 旷视API Secret
    private String baseUrl;                 // API基础URL
    private String returnPortrait;          // 是否返回人像（配置级别）
    private ExceptionRange exceptionRange;  // 异常范围配置
    private String encryptionType;          // 加密类型
    
    @Data
    public static class ExceptionRange {
        private Boolean legality;           // 合法性检查
        private Boolean textQuality;       // 文本质量检查
        private Boolean logic;              // 逻辑检查
        private Boolean completeness;       // 完整性检查
        private Boolean faceDetect;         // 人脸检测
    }
}