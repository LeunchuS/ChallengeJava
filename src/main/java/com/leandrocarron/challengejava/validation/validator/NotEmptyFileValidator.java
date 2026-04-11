package com.leandrocarron.challengejava.validation.validator;


import com.leandrocarron.challengejava.validation.annotation.NotEmptyFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class NotEmptyFileValidator implements ConstraintValidator<NotEmptyFile, MultipartFile> {


    @Override
    //return false only if it validates that file exists and it is empty.
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext constraintValidatorContext) {
        /*si el archivo es null devuelve true porque no pudo validar si es vacio
            si el archivo es vacio es true pero lo convierte a lo opuesto con el ! entonces la validacion devuelve false.
         */
        return multipartFile == null || !multipartFile.isEmpty();
    }
}
