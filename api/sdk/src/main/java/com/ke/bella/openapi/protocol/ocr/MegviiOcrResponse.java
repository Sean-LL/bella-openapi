package com.ke.bella.openapi.protocol.ocr;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 旷视科技OCR API响应模型
 */
@Data
public class MegviiOcrResponse {
    @JsonProperty("request_id")
    private String requestId;
    private Integer result;
    private Integer side;
    private String error;
    private Integer completeness;

    // 人像面字段
    private FieldInfo name;
    private FieldInfo gender;
    private FieldInfo nationality;
    @JsonProperty("birth_year")
    private FieldInfo birthYear;
    @JsonProperty("birth_month")
    private FieldInfo birthMonth;
    @JsonProperty("birth_day")
    private FieldInfo birthDay;
    @JsonProperty("idcard_number")
    private FieldInfo idcardNumber;
    private FieldInfo address;
    private FieldInfo portrait;

    // 国徽面字段
    @JsonProperty("issued_by")
    private FieldInfo issuedBy;
    @JsonProperty("valid_date_start")
    private FieldInfo validDateStart;
    @JsonProperty("valid_date_end")
    private FieldInfo validDateEnd;

    // 通用字段
    private Object legality;
    @JsonProperty("card_rect")
    private Object cardRect;
    @JsonProperty("time_used")
    private Integer timeUsed;

    @Data
    public static class FieldInfo {
        private String result;
        private Float quality;
        private Object rect;
        private Integer logic;
    }
}
