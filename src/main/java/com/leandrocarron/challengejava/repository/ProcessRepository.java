package com.leandrocarron.challengejava.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.leandrocarron.challengejava.model.FileProcess;
import java.util.Optional;

public interface ProcessRepository extends JpaRepository<FileProcess, Long> {
        Optional<FileProcess> findByfileProcessId(Long fileProcessId);
}

