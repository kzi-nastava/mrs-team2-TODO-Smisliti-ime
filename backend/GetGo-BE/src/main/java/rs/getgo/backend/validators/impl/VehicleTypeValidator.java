package rs.getgo.backend.validators.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import rs.getgo.backend.model.enums.VehicleType;
import rs.getgo.backend.validators.annotations.ValidVehicleType;

public class VehicleTypeValidator implements ConstraintValidator<ValidVehicleType, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        try {
            VehicleType.valueOf(value.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}