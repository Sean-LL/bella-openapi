package com.ke.bella.openapi.protocol.ocr;

import com.ke.bella.file.api.FileApiClient;
import com.ke.bella.file.api.config.FileApiProperties;
import com.ke.bella.openapi.EndpointContext;
import com.ke.bella.openapi.common.exception.BizParamCheckException;
import com.ke.bella.openapi.protocol.IProtocolAdaptor;
import org.springframework.util.StringUtils;

import java.util.Base64;

/**
 * OCR身份证识别适配器接口
 */
public interface OcrIdcardAdaptor<T extends OcrProperty> extends IProtocolAdaptor {

    /**
     * 身份证识别
     * @param request 识别请求
     * @param url API地址
     * @param property 属性配置
     * @return 识别结果
     */
    default OcrIdcardResponse idcard(OcrIdcardRequest request, String url, T property) {
        try {
            ImageDataType dataType = processImageData(request, property);
            return doIdcard(request, url, property, dataType);
        } catch (Exception e) {
            throw new BizParamCheckException("OCR识别请求处理失败: " + e.getMessage());
        }
    }

    /**
     * 执行身份证识别请求
     */
    OcrIdcardResponse doIdcard(OcrIdcardRequest request, String url, T property, ImageDataType dataType) throws Exception;

    /**
     * 处理图片数据，确定使用的数据类型并补充请求信息
     */
    default ImageDataType processImageData(OcrIdcardRequest request, T property) throws Exception {
        // 优先级：Base64 > URL > FileID

        // 1. 检查Base64输入
        if (property.isSupportBase64() && StringUtils.hasText(request.getImage_base64())) {
            return ImageDataType.BASE64;
        }

        // 2. 检查URL输入
        if (property.isSupportUrl() && StringUtils.hasText(request.getImage_url())) {
            return ImageDataType.URL;
        }

        // 3. 检查文件ID输入
        if (property.isSupportFileId() && StringUtils.hasText(request.getFile_id())) {
            // 如果适配器支持Base64，则转换为Base64
            if (property.isSupportBase64()) {
                convertFileIdToBase64(request);
                return ImageDataType.BASE64;
            }
            return ImageDataType.FILE_ID;
        }

        // 构建错误信息
        StringBuilder errorMessage = new StringBuilder("请求参数格式错误，请使用以下支持的图像输入方式：");
        if (property.isSupportBase64()) {
            errorMessage.append("image_base64 ");
        }
        if (property.isSupportUrl()) {
            errorMessage.append("image_url ");
        }
        if (property.isSupportFileId()) {
            errorMessage.append("file_id");
        }

        throw new BizParamCheckException(errorMessage.toString());
    }

    /**
     * 将文件ID转换为Base64格式
     */
    default void convertFileIdToBase64(OcrIdcardRequest request) throws Exception {
        FileApiProperties fileApiProperties = getFileApiProperties();
        if (fileApiProperties == null || !StringUtils.hasText(fileApiProperties.getUrl())) {
            throw new BizParamCheckException("文件服务未配置");
        }

        try {
            // 获取FileApiClient实例并调用文件服务
            FileApiClient fileApiClient = FileApiClient.getInstance(fileApiProperties.getUrl());
            String apikey = EndpointContext.getApikey().getCode();
            byte[] imageBytes = fileApiClient.getContent(request.getFile_id(), apikey);

            // 转换为Base64
            String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);
            request.setImage_base64(imageBase64);
            request.setFile_id(null); // 清除file_id，避免重复处理
        } catch (Exception e) {
            throw new BizParamCheckException("文件ID无效或文件服务不可用: " + request.getFile_id());
        }
    }

    /**
     * 获取文件API配置，子类需要实现此方法
     */
    default FileApiProperties getFileApiProperties() {
        return null; // 默认返回null，具体实现类需要注入FileApiProperties
    }

    @Override
    default String endpoint() {
        return "/v1/ocr/idcard";
    }
}
