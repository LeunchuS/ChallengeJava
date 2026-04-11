package com.leandrocarron.challengejava.validation.validator;

import com.leandrocarron.challengejava.exception.FileErrorType;
import com.leandrocarron.challengejava.exception.FileException;
import com.leandrocarron.challengejava.validation.annotation.MaxFileSize;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class MaxFileSizeValidator implements ConstraintValidator<MaxFileSize, MultipartFile> {

    private long maxSize;

    @Override
    //It is like a setter
    public void initialize(MaxFileSize constraintAnnotation) {
        this.maxSize = constraintAnnotation.value();
    }


    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext constraintValidatorContext) {
        if (multipartFile == null || multipartFile.isEmpty()) {//this validation is cover by another annotation
            return true;
        }
        if(multipartFile.getSize()>maxSize) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("El tamaño del archivo excede el límite. "+(maxSize/1024/1024)+"MB").addConstraintViolation();
            return false;
        }
        return true;
    }
}
