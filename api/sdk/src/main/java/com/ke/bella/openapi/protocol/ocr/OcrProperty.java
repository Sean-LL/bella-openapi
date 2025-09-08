package com.ke.bella.openapi.protocol.ocr;

import lombok.Data;

import java.io.Serializable;

/**
 * OCR协议适配器属性基类
 */
@Data
public abstract class OcrProperty implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String maxImageSize;            // 最大图片大小限制
    private String[] supportedFormats;      // 支持的图片格式
    private String minImageSize;            // 最小图片尺寸
    
    // 支持的图片输入方式配置
    private boolean supportBase64 = true;   // 是否支持Base64输入
    private boolean supportUrl = false;     // 是否支持URL输入
    private boolean supportFileId = true;   // 是否支持文件ID输入
    
    // 编码类型配置
    private String encodingType;            // 编码类型
}