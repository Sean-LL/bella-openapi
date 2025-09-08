package com.ke.bella.openapi.protocol.ocr;

/**
 * OCR图片数据类型枚举
 */
public enum ImageDataType {
    /**
     * Base64编码的图片数据
     */
    BASE64,
    
    /**
     * 图片URL地址
     */
    URL,
    
    /**
     * 文件ID（来自文件服务）
     */
    FILE_ID
}