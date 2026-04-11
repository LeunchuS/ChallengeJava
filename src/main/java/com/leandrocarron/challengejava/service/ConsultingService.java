package com.leandrocarron.challengejava.service;

import com.leandrocarron.challengejava.dto.responseDTO.AccountBalanceResponseDTO;
import com.leandrocarron.challengejava.dto.responseDTO.ProcessResponseDTO;
import com.leandrocarron.challengejava.dto.responseDTO.Ranked;
import com.leandrocarron.challengejava.dto.responseDTO.RankingResponseDTO;
import com.leandrocarron.challengejava.exception.FileErrorType;
import com.leandrocarron.challengejava.exception.FileException;
import com.leandrocarron.challengejava.model.FileProcess;
import com.leandrocarron.challengejava.repository.ProcessRepository;
import com.leandrocarron.challengejava.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service
@AllArgsConstructor
public class ConsultingService {

    private final ProcessRepository processRepository;
    private final TransactionRepository transactionRepository;
    private static final Logger log = LoggerFactory.getLogger(FileProcessService.class);

    public ProcessResponseDTO getProcessInfo(long processingId){
        //If procesId doesn't exist it returns an ErrorResponseDTO
        FileProcess fileProcess = processRepository.findById(processingId).orElse(null);
        if (fileProcess == null) {
            log.info("processingId " +processingId+ " not exist or something else went wrong");
            return null;
        }
        ProcessResponseDTO processResponseDTO = new ProcessResponseDTO();
        processResponseDTO.prepareDTO(fileProcess);
        return processResponseDTO;
    }

    public AccountBalanceResponseDTO getAccountBalance(long accountId){
        if (!transactionRepository.existsByAccountId(accountId))
            throw new FileException(FileErrorType.NOT_FOUND,"No transactions were found");
        BigDecimal amount = transactionRepository.sumAmountByAccountId(accountId);
        return (new AccountBalanceResponseDTO(accountId, amount));
    }

    public RankingResponseDTO getRanking(){
       List<Ranked> ranking = transactionRepository.getRanking(PageRequest.of(0,10));
       RankingResponseDTO rankingResponseDTO = new RankingResponseDTO(ranking);
       return rankingResponseDTO;
    }

    public boolean isDuplicated(Long transactionId){
        return !transactionRepository.findById(transactionId).isEmpty();
    }
}
