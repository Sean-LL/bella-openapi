package com.ke.bella.openapi.protocol.ocr;

import com.ke.bella.file.api.config.FileApiProperties;
import com.ke.bella.openapi.utils.HttpUtils;
import com.ke.bella.openapi.utils.JacksonUtils;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

/**
 * 旷视科技OCR适配器
 */
@Component("megviiOcr")
public class MegviiOcrAdaptor implements OcrIdcardAdaptor<MegviiOcrProperty> {
    
    @Autowired
    private FileApiProperties fileApiProperties;
    
    @Override
    public String getDescription() {
        return "旷视科技OCR协议";
    }
    
    @Override
    public Class<MegviiOcrProperty> getPropertyClass() {
        return MegviiOcrProperty.class;
    }
    
    @Override
    public FileApiProperties getFileApiProperties() {
        return fileApiProperties;
    }
    
    @Override
    public OcrIdcardResponse doIdcard(OcrIdcardRequest request, String url, MegviiOcrProperty property, ImageDataType dataType) throws Exception {
        try {
            // 1. 参数校验和预处理
            validateImageInput(request, property);
            
            // 2. 构建multipart/form-data请求
            MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("api_key", property.getApiKey())
                    .addFormDataPart("api_secret", property.getApiSecret());
            
            // 3. 根据数据类型处理图片数据
            addImageToRequest(request, multipartBuilder, dataType);
            
            // 4. 添加可选参数
            addOptionalParameters(request, property, multipartBuilder);
            
            // 5. 构建HTTP请求
            RequestBody requestBody = multipartBuilder.build();
            Request httpRequest = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            
            // 6. 执行请求并处理响应
            return doRequest(httpRequest);
            
        } catch (Exception e) {
            return buildErrorResponse("INTERNAL_ERROR", "请求处理失败: " + e.getMessage());
        }
    }
    
    private void validateImageInput(OcrIdcardRequest request, MegviiOcrProperty property) {
        // 图片大小校验
        if (StringUtils.hasText(request.getImage_base64())) {
            byte[] imageBytes = Base64.getDecoder().decode(request.getImage_base64());
            long imageSizeBytes = imageBytes.length;
            long maxSizeBytes = 10 * 1024 * 1024; // 10MB
            
            Assert.isTrue(imageSizeBytes <= maxSizeBytes, "图片大小不能超过10MB");
            Assert.isTrue(imageSizeBytes >= 1024, "图片大小不能小于1KB"); // 最小尺寸检查
        }
    }
    
    private void addImageToRequest(OcrIdcardRequest request, MultipartBody.Builder builder, ImageDataType dataType) throws IOException {
        switch (dataType) {
            case BASE64:
                // Base64图片数据转换为文件
                byte[] imageBytes = Base64.getDecoder().decode(request.getImage_base64());
                RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), imageBytes);
                builder.addFormDataPart("image", "image.jpg", imageBody);
                break;
            case URL:
                // 从URL下载图片
                byte[] urlImageBytes = downloadImage(request.getImage_url());
                RequestBody urlImageBody = RequestBody.create(MediaType.parse("image/*"), urlImageBytes);
                builder.addFormDataPart("image", "image.jpg", urlImageBody);
                break;
            case FILE_ID:
                // FILE_ID类型应该已经在接口层转换为BASE64，这里不应该到达
                throw new IllegalStateException("FILE_ID类型应该已经在接口层转换为BASE64");
        }
    }
    
    private void addOptionalParameters(OcrIdcardRequest request, MegviiOcrProperty property, 
                                     MultipartBody.Builder builder) {
        // 添加return_portrait参数（从property配置中获取）
        if (StringUtils.hasText(property.getReturnPortrait())) {
            builder.addFormDataPart("return_portrait", property.getReturnPortrait());
        }
        
        // 添加exception_range参数
        if (property.getExceptionRange() != null) {
            String exceptionRangeJson = JacksonUtils.serialize(property.getExceptionRange());
            builder.addFormDataPart("exception_range", exceptionRangeJson);
        }
        
        // 添加encryption_type参数
        if (StringUtils.hasText(property.getEncryptionType())) {
            builder.addFormDataPart("encryption_type", property.getEncryptionType());
        }
    }
    
    private byte[] downloadImage(String imageUrl) throws IOException {
        Request request = new Request.Builder().url(imageUrl).build();
        try (Response response = HttpUtils.httpRequest(request)) {
            if (!response.isSuccessful()) {
                throw new IOException("图片下载失败: " + response.code());
            }
            return response.body().bytes();
        }
    }
    
    private OcrIdcardResponse doRequest(Request httpRequest) {
        try {
            // 调用旷视API
            MegviiOcrResponse megviiResponse = HttpUtils.httpRequest(httpRequest, MegviiOcrResponse.class);
            
            // 转换为标准响应格式
            return convertMegviiResponse(megviiResponse);
            
        } catch (Exception e) {
            return buildErrorResponse("HTTP_REQUEST_FAILED", "API调用失败: " + e.getMessage());
        }
    }
    
    private OcrIdcardResponse convertMegviiResponse(MegviiOcrResponse megviiResponse) {
        OcrIdcardResponse response = new OcrIdcardResponse();
        response.setRequest_id(megviiResponse.getRequestId());
        
        // 检查是否有错误
        if (StringUtils.hasText(megviiResponse.getError())) {
            response.setError_code("400");
            response.setError_msg(megviiResponse.getError());
            response.setData(null);
            return response;
        }
        
        // 根据side字段判断正反面
        if (megviiResponse.getSide() == 0) {
            // 人像面
            response.setSide("portrait");
            response.setData(buildPortraitData(megviiResponse));
        } else if (megviiResponse.getSide() == 1) {
            // 国徽面
            response.setSide("national_emblem");
            response.setData(buildNationalEmblemData(megviiResponse));
        }
        
        return response;
    }
    
    private OcrIdcardResponse.PortraitData buildPortraitData(MegviiOcrResponse megviiResponse) {
        OcrIdcardResponse.PortraitData data = new OcrIdcardResponse.PortraitData();
        
        if (megviiResponse.getName() != null) {
            data.setName(megviiResponse.getName().getResult());
        }
        if (megviiResponse.getGender() != null) {
            data.setSex(megviiResponse.getGender().getResult());
        }
        if (megviiResponse.getNationality() != null) {
            data.setNationality(megviiResponse.getNationality().getResult());
        }
        if (megviiResponse.getIdcardNumber() != null) {
            data.setIdcard_number(megviiResponse.getIdcardNumber().getResult());
        }
        if (megviiResponse.getAddress() != null) {
            data.setAddress(megviiResponse.getAddress().getResult());
        }
        
        // 组合出生日期
        String birthDate = combineBirthDate(megviiResponse);
        data.setBirth_date(birthDate);
        
        return data;
    }
    
    private OcrIdcardResponse.NationalEmblemData buildNationalEmblemData(MegviiOcrResponse megviiResponse) {
        OcrIdcardResponse.NationalEmblemData data = new OcrIdcardResponse.NationalEmblemData();
        
        if (megviiResponse.getIssuedBy() != null) {
            data.setIssue_authority(megviiResponse.getIssuedBy().getResult());
        }
        if (megviiResponse.getValidDateStart() != null) {
            data.setValid_date_start(formatDate(megviiResponse.getValidDateStart().getResult()));
        }
        if (megviiResponse.getValidDateEnd() != null) {
            data.setValid_date_end(formatDate(megviiResponse.getValidDateEnd().getResult()));
        }
        
        return data;
    }
    
    private String combineBirthDate(MegviiOcrResponse megviiResponse) {
        String year = megviiResponse.getBirthYear() != null ? megviiResponse.getBirthYear().getResult() : "";
        String month = megviiResponse.getBirthMonth() != null ? megviiResponse.getBirthMonth().getResult() : "";
        String day = megviiResponse.getBirthDay() != null ? megviiResponse.getBirthDay().getResult() : "";
        
        if (StringUtils.hasText(year) && StringUtils.hasText(month) && StringUtils.hasText(day)) {
            return year + String.format("%02d", Integer.parseInt(month)) + String.format("%02d", Integer.parseInt(day));
        }
        return "";
    }
    
    private String formatDate(String dateStr) {
        // 将YYYYMMDD格式转换为YYYY.MM.DD格式
        if (StringUtils.hasText(dateStr) && dateStr.length() == 8) {
            return dateStr.substring(0, 4) + "." + dateStr.substring(4, 6) + "." + dateStr.substring(6, 8);
        }
        return dateStr;
    }
    
    private OcrIdcardResponse buildErrorResponse(String errorCode, String errorMsg) {
        OcrIdcardResponse response = new OcrIdcardResponse();
        response.setRequest_id(generateRequestId());
        response.setError_code(errorCode);
        response.setError_msg(errorMsg);
        response.setData(null);
        return response;
    }
    
    private String generateRequestId() {
        return System.currentTimeMillis() + "," + UUID.randomUUID().toString();
    }
}