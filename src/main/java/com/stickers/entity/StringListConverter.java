package com.stickers.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        // Convert to PostgreSQL array format: {"value1","value2"}
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < attribute.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(attribute.get(i).replace("\"", "\\\"")).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty() || dbData.equals("{}")) {
            return new ArrayList<>();
        }
        
        // Remove curly braces and split by comma
        String cleaned = dbData.trim();
        if (cleaned.startsWith("{") && cleaned.endsWith("}")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        
        if (cleaned.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Split by comma but handle quoted strings
        List<String> result = new ArrayList<>();
        String[] parts = cleaned.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("\"") && part.endsWith("\"")) {
                part = part.substring(1, part.length() - 1);
            }
            part = part.replace("\\\"", "\"");
            if (!part.isEmpty()) {
                result.add(part);
            }
        }
        
        return result;
    }
}

