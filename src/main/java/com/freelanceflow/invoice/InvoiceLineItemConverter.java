package com.freelanceflow.invoice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Converter(autoApply = false)
public class InvoiceLineItemConverter implements AttributeConverter<List<InvoiceLineItem>, String> {

    private static final Logger log = LoggerFactory.getLogger(InvoiceLineItemConverter.class);

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public String convertToDatabaseColumn(List<InvoiceLineItem> items) {
        if (items == null || items.isEmpty()) {
            return "[]";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(items);
        } catch (Exception e) {
            log.error("Failed to serialize line items to JSON", e);
            return "[]";
        }
    }

    @Override
    public List<InvoiceLineItem> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<List<InvoiceLineItem>>() {});
        } catch (Exception e) {
            log.error("Failed to deserialize line items from JSON: {}", json, e);
            return new ArrayList<>();
        }
    }
}
