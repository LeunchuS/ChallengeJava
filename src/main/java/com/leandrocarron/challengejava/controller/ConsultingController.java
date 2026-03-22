package com.leandrocarron.challengejava.controller;

import com.leandrocarron.challengejava.dto.responseDTO.AccountBalanceResponseDTO;
import com.leandrocarron.challengejava.dto.responseDTO.DuplicatedRespopnseDTO;
import com.leandrocarron.challengejava.dto.responseDTO.ProcessResponseDTO;
import com.leandrocarron.challengejava.dto.responseDTO.RankingResponseDTO;
import com.leandrocarron.challengejava.exception.FileErrorType;
import com.leandrocarron.challengejava.exception.FileException;
import com.leandrocarron.challengejava.repository.TransactionRepository;
import com.leandrocarron.challengejava.service.ConsultingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/consulting")
public class ConsultingController {
    private final ConsultingService consultingService;


    public ConsultingController(ConsultingService consultingService, TransactionRepository transactionRepository) {
        this.consultingService = consultingService;
    }


    @GetMapping("/processing/{processingId}")
    public ProcessResponseDTO GetProcessInfo(@PathVariable Long processingId){
        ProcessResponseDTO processResponseDTO = consultingService.getProcessInfo(processingId);
        if(processResponseDTO == null)
            throw new FileException(FileErrorType.INVALID_ID,"Invalid processingId " + processingId);
        return processResponseDTO;
    }

    @GetMapping("/accountBalance/{accountId}")
    public AccountBalanceResponseDTO GetAccountBalance(@PathVariable Long accountId){
        AccountBalanceResponseDTO accountBalanceResponseDTO = consultingService.getAccountBalance(accountId);
        return accountBalanceResponseDTO;
    }

    @GetMapping("/ranking")
    public RankingResponseDTO GetRankinByAccount(){
        RankingResponseDTO rankingResponseDTO = consultingService.getRanking();
        return rankingResponseDTO;
    }

    @GetMapping("/duplicated/{transactionId}")
    public DuplicatedRespopnseDTO isDuplicated(@PathVariable Long transactionId){
        boolean transaction = consultingService.getById(transactionId);

        DuplicatedRespopnseDTO duplicatedRespopnseDTO = new DuplicatedRespopnseDTO(
                transactionId, !transaction,
                "transactionId is"+ (transaction?" not ":" ")+ "duplicated");
        return duplicatedRespopnseDTO;

    }
}
