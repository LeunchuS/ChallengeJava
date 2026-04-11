package com.leandrocarron.challengejava.validation.annotation;

import com.leandrocarron.challengejava.validation.validator.CSVFileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.PARAMETER, ElementType.FIELD }) //where can be used this anotation?
@Retention(RetentionPolicy.RUNTIME) //Required to be seen by spring for validation
@Constraint(validatedBy = CSVFileValidator.class)//It determines the logic is in MaxFileSizeValidator.class
public @interface CSVFile {
    String message() default "El archivo es imcompatible. Se espera un archivo csv";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
