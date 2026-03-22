package com.leandrocarron.challengejava.controller;

import com.leandrocarron.challengejava.dto.ErrorDTO.ErrorResponseDTO;
import com.leandrocarron.challengejava.dto.responseDTO.AccountBalanceResponseDTO;
import com.leandrocarron.challengejava.dto.responseDTO.DuplicatedResponseDTO;
import com.leandrocarron.challengejava.dto.responseDTO.ProcessResponseDTO;
import com.leandrocarron.challengejava.dto.responseDTO.RankingResponseDTO;
import com.leandrocarron.challengejava.exception.FileErrorType;
import com.leandrocarron.challengejava.exception.FileException;
import com.leandrocarron.challengejava.repository.TransactionRepository;
import com.leandrocarron.challengejava.service.ConsultingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Consulting", description = "Operaciones de consulta")

@RestController
@RequestMapping("/consulting")
public class ConsultingController {
    private final ConsultingService consultingService;


    public ConsultingController(ConsultingService consultingService, TransactionRepository transactionRepository) {
        this.consultingService = consultingService;
    }

    @Operation(
            summary = "Informa sobre un proceso de carga de csv",
            description = "Devuelve el estado del proceso y las estadisticas. Los datos numéricos se actualizan al finalizar"
    )
    @GetMapping("/processing/{processingId}")
    public ProcessResponseDTO GetProcessInfo(@PathVariable Long processingId){
        ProcessResponseDTO processResponseDTO = consultingService.getProcessInfo(processingId);
        if(processResponseDTO == null)
            throw new FileException(FileErrorType.INVALID_ID,"Invalid processingId " + processingId);
        return processResponseDTO;
    }

    @Operation(
            summary = "Suma los montos de las transacciones de una cuenta",
            description = "Si la cuenta existe devuelve el monto, si no existe devuelve una respuesta de error"
    )
    @GetMapping("/accountBalance/{accountId}")
    public AccountBalanceResponseDTO GetAccountBalance(@PathVariable Long accountId){
        AccountBalanceResponseDTO accountBalanceResponseDTO = consultingService.getAccountBalance(accountId);
        if(accountBalanceResponseDTO==null)
            throw new FileException(FileErrorType.INVALID_ID,"No transactions were found");
        return accountBalanceResponseDTO;
    }

    @Operation(
            summary = "Cuentas con más transacciones",
            description = "Devuelve las primeras diez cuentas con más transacciones"
    )
    @GetMapping("/ranking")
    public RankingResponseDTO GetRankinByAccount(){
        RankingResponseDTO rankingResponseDTO = consultingService.getRanking();
        return rankingResponseDTO;
    }

    @Operation(
            summary = "Valida si ya existe una transacción",
            description = "Si el id de la transacción existe devuelve true, si no existe devuelve false"
    )
    @GetMapping("/duplicated/{transactionId}")
    public DuplicatedResponseDTO isDuplicated(@PathVariable Long transactionId){
        boolean transaction = consultingService.getById(transactionId);

        DuplicatedResponseDTO duplicatedResponseDTO = new DuplicatedResponseDTO(
                transactionId, !transaction,
                "transactionId is"+ (transaction?" not ":" ")+ "duplicated");
        return duplicatedResponseDTO;

    }
}
