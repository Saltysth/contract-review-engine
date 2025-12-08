package com.contractreview.reviewengine.interfaces.rest.dto;

import com.contract.common.enums.ReviewTypeDetail;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ReviewTypeDetail列表反序列化器
 * 处理多种输入格式：
 * 1. 空字符串或null：返回空列表
 * 2. 逗号分隔的字符串："RISK_ASSESSMENT,CLAUSE_ANALYSIS"
 * 3. 数组格式：["RISK_ASSESSMENT", "CLAUSE_ANALYSIS"]
 */
public class ReviewTypeDetailListDeserializer extends JsonDeserializer<List<ReviewTypeDetail>> {

    @Override
    public List<ReviewTypeDetail> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            String value = p.getValueAsString();
            if (value == null || value.trim().isEmpty()) {
                return Collections.emptyList();
            }
            // 处理逗号分隔的字符串
            String[] types = value.split(",");
            return Arrays.stream(types)
                    .map(String::trim)
                    .filter(type -> !type.isEmpty())
                    .map(ReviewTypeDetail::fromString)
                    .collect(Collectors.toList());
        } else if (p.currentToken() == JsonToken.VALUE_NULL) {
            return Collections.emptyList();
        } else if (p.currentToken() == JsonToken.START_ARRAY) {
            // 处理数组格式
            List<String> stringList = ctxt.readValue(p, ctxt.constructType(List.class));
            if (stringList == null || stringList.isEmpty()) {
                return Collections.emptyList();
            }
            return stringList.stream()
                    .filter(type -> type != null && !type.trim().isEmpty())
                    .map(ReviewTypeDetail::fromString)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }
}