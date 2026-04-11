package com.leandrocarron.challengejava.validation.validator;

import com.leandrocarron.challengejava.validation.annotation.CSVFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ValidatorContext;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.reader.StreamReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CSVFileValidator implements ConstraintValidator<CSVFile, MultipartFile> {

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext constraintValidatorContext) {
        if(multipartFile==null || multipartFile.isEmpty())
            return true;//another annotations validate this

        String originalFileName = multipartFile.getOriginalFilename();
        if(originalFileName!=null && originalFileName.toUpperCase().endsWith(".CSV")){
            //try-catch-resource //wherever happends reader autocloseses
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream())))
            {
                String firstLine = reader.readLine();
                if(firstLine.contains(","))
                    return true;
                else
                    return false;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }
}
