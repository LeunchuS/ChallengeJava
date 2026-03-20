package com.leandrocarron.challengejava.controller;

import com.leandrocarron.challengejava.dto.requestDTO.AccountBalanceRequestDTO;
import com.leandrocarron.challengejava.dto.requestDTO.DuplicatedRequestDTO;
import com.leandrocarron.challengejava.dto.requestDTO.ProcessRequestDTO;
import com.leandrocarron.challengejava.dto.responseDTO.AccountBalanceResponseDTO;
import com.leandrocarron.challengejava.dto.responseDTO.DuplicatedRespopnseDTO;
import com.leandrocarron.challengejava.dto.responseDTO.ProcessResponseDTO;
import com.leandrocarron.challengejava.dto.responseDTO.RankingResponseDTO;
import com.leandrocarron.challengejava.repository.TransactionRepository;
import com.leandrocarron.challengejava.service.ConsultingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consulting")
public class ConsultingController {
    private final ConsultingService consultingService;


    public ConsultingController(ConsultingService consultingService, TransactionRepository transactionRepository) {
        this.consultingService = consultingService;
    }


    @GetMapping("/processing")
    public ProcessResponseDTO GetProcessInfo(@RequestBody ProcessRequestDTO processRequestDTO){
        ProcessResponseDTO processResponseDTO = consultingService.getProcessInfo(processRequestDTO.getProcessingId());
        return processResponseDTO;
    }

    @GetMapping("/accountBalance")
    public AccountBalanceResponseDTO GetAccountBalance(@RequestBody AccountBalanceRequestDTO accountBalanceRequestDTO){
        AccountBalanceResponseDTO accountBalanceResponseDTO = consultingService.getAccountBalance(accountBalanceRequestDTO.getAccountId());
        return accountBalanceResponseDTO;
    }

    @GetMapping("/ranking")
    public RankingResponseDTO GetRankinByAccount(){
        RankingResponseDTO rankingResponseDTO = consultingService.getRanking();
        return rankingResponseDTO;
    }

    @GetMapping("/duplicated")
    public DuplicatedRespopnseDTO isDuplicated(@RequestBody DuplicatedRequestDTO duplicatedRequestDTO){
        boolean transaction = consultingService.getById(duplicatedRequestDTO.getTransactionId());

        DuplicatedRespopnseDTO duplicatedRespopnseDTO = new DuplicatedRespopnseDTO(
                duplicatedRequestDTO.getTransactionId(), !transaction,
                "transactionId is"+ (transaction?" not ":" ")+ "duplicated");
        return duplicatedRespopnseDTO;

    }
}
