package com.fisch_tradehub.tradehub_core.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BillStatusConverter implements AttributeConverter<BillStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(BillStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public BillStatus convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return BillStatus.fromValue(dbData);
    }
}
