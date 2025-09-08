package com.ke.bella.openapi.protocol.ocr;

import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * OCR Mock适配器
 */
@Component("mockOcr")
public class MockOcrAdaptor implements OcrIdcardAdaptor<OcrProperty> {

    @Override
    public String getDescription() {
        return "OCR Mock协议";
    }

    @Override
    public Class<OcrProperty> getPropertyClass() {
        return OcrProperty.class;
    }

    @Override
    public OcrIdcardResponse doIdcard(OcrIdcardRequest request, String url, OcrProperty property, ImageDataType dataType) {
        // 模拟延迟
        try {
            Thread.sleep(500 + (int)(Math.random() * 1000)); // 0.5-1.5秒随机延迟
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        OcrIdcardResponse response = new OcrIdcardResponse();
        response.setRequest_id(generateRequestId());

        // 随机返回人像面或国徽面数据
        if (Math.random() > 0.5) {
            // 返回人像面数据
            response.setSide("portrait");
            OcrIdcardResponse.PortraitData data = new OcrIdcardResponse.PortraitData();
            data.setName("张三");
            data.setSex("男");
            data.setNationality("汉");
            data.setBirth_date("19900101");
            data.setAddress("北京市海淀区中关村大街1号");
            data.setIdcard_number("110101199001010001");
            response.setData(data);
        } else {
            // 返回国徽面数据
            response.setSide("national_emblem");
            OcrIdcardResponse.NationalEmblemData data = new OcrIdcardResponse.NationalEmblemData();
            data.setIssue_authority("北京市海淀区公安局");
            data.setValid_date_start("2020.01.01");
            data.setValid_date_end("2030.01.01");
            response.setData(data);
        }

        return response;
    }

    private String generateRequestId() {
        return "mock_" + System.currentTimeMillis() + "," + UUID.randomUUID().toString().substring(0, 8);
    }
}
