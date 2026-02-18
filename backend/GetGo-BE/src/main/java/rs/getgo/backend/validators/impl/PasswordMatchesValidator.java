package rs.getgo.backend.validators.impl;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import rs.getgo.backend.validators.annotations.PasswordMatches;

import java.lang.reflect.Field;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {
    private String passwordField;
    private String confirmPasswordField;

    @Override
    public void initialize(PasswordMatches annotation) {
        this.passwordField = annotation.passwordField();
        this.confirmPasswordField = annotation.confirmPasswordField();
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        try {
            Field password = obj.getClass().getDeclaredField(passwordField);
            Field confirm = obj.getClass().getDeclaredField(confirmPasswordField);
            password.setAccessible(true);
            confirm.setAccessible(true);

            Object p = password.get(obj);
            Object c = confirm.get(obj);

            boolean valid = p != null && p.equals(c);
            if (!valid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode(confirmPasswordField)
                        .addConstraintViolation();
            }
            return valid;
        } catch (Exception e) {
            return false;
        }
    }
}