package com.leandrocarron.challengejava.service;

import com.leandrocarron.challengejava.dto.responseDTO.AccountBalanceResponseDTO;
import com.leandrocarron.challengejava.dto.responseDTO.ProcessResponseDTO;
import com.leandrocarron.challengejava.dto.responseDTO.Ranked;
import com.leandrocarron.challengejava.dto.responseDTO.RankingResponseDTO;
import com.leandrocarron.challengejava.model.FileProcess;
import com.leandrocarron.challengejava.repository.ProcessRepository;
import com.leandrocarron.challengejava.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service
@AllArgsConstructor
public class ConsultingService {

    private final ProcessRepository processRepository;
    private final TransactionRepository transactionRepository;

    public ProcessResponseDTO getProcessInfo(long processingId){
        FileProcess fileProcess = processRepository.getReferenceById(processingId);
        ProcessResponseDTO processResponseDTO = new ProcessResponseDTO();
        processResponseDTO.PrepareDTO(fileProcess);
        return processResponseDTO;
    }

    public AccountBalanceResponseDTO getAccountBalance(long accountId){
         BigDecimal amount = transactionRepository.sumAmountByAccountId(accountId);
         return (new AccountBalanceResponseDTO(accountId, amount));
    }

    public RankingResponseDTO getRanking(){
       List<Ranked> ranking = transactionRepository.getRanking(PageRequest.of(0,10));
       RankingResponseDTO rankingResponseDTO = new RankingResponseDTO(ranking);
       return rankingResponseDTO;
    }

    public boolean getById(Long transactionId){
        return transactionRepository.findById(transactionId).isEmpty();
    }
}
