package rs.getgo.backend.validators.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import rs.getgo.backend.validators.impl.VehicleTypeValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = VehicleTypeValidator.class)
public @interface ValidVehicleType {
    String message() default "Invalid vehicle type";
    boolean required() default true;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}