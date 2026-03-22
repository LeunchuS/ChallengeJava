package com.leandrocarron.challengejava.controller;

import com.leandrocarron.challengejava.config.DefaultApiResponses;
import com.leandrocarron.challengejava.dto.ErrorDTO.ErrorResponseDTO;
import com.leandrocarron.challengejava.dto.responseDTO.*;
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
            summary = "Obtiene información de un proceso de carga",
            description = "Devuelve el estado actual del proceso de carga de un archivo CSV. Los valores estadísticos se actualizan una vez que el proceso finaliza.",
            responses = {@ApiResponse(responseCode = "200",description = "Proceso encontrado",
                            content = @Content(schema = @Schema(implementation = ProcessResponseDTO.class)))
            }
    )
    @DefaultApiResponses
    @GetMapping("/processing/{processingId}")
    public ProcessResponseDTO GetProcessInfo(@PathVariable Long processingId){
        ProcessResponseDTO processResponseDTO = consultingService.getProcessInfo(processingId);
        if(processResponseDTO == null)
            throw new FileException(FileErrorType.INVALID_ID,"Invalid processingId " + processingId);
        return processResponseDTO;
    }

    @Operation(
            summary = "Suma los montos de las transacciones de una cuenta",
            description = "Si la cuenta existe devuelve el monto, si no existe devuelve un ErrorResponseDTO",
            responses = {
                    @ApiResponse(responseCode = "200",description = "Balance obtenido correctamente",
                                content = @Content(schema = @Schema(implementation = AccountBalanceResponseDTO.class)))
            }
    )
    @DefaultApiResponses
    @GetMapping("/accountBalance/{accountId}")
    public AccountBalanceResponseDTO GetAccountBalance(@PathVariable Long accountId){
        AccountBalanceResponseDTO accountBalanceResponseDTO = consultingService.getAccountBalance(accountId);
        if(accountBalanceResponseDTO==null)
            throw new FileException(FileErrorType.INVALID_ID,"No transactions were found");
        return accountBalanceResponseDTO;
    }

    @Operation(
            summary = "Ranking de cuentas por cantidad de transacciones",
            description = "Devuelve las 10 cuentas con mayor cantidad de transacciones registradas en el sistema.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ranking obtenido correctamente",
                            content = @Content(schema = @Schema(implementation = RankingResponseDTO.class)))
            }
    )
    @DefaultApiResponses
    @GetMapping("/ranking")
    public RankingResponseDTO GetRankinByAccount(){
        RankingResponseDTO rankingResponseDTO = consultingService.getRanking();
        return rankingResponseDTO;
    }

    @Operation(
            summary = "Verifica si una transacción está duplicada",
            description = "Indica si una transacción ya existe en el sistema. Retorna true si no existe y false si ya existe.",
            responses = {
                    @ApiResponse(responseCode = "200",description = "Consulta exitosa",
                            content = @Content(schema = @Schema(implementation = DuplicatedResponseDTO.class)))
            }
    )
    @DefaultApiResponses
    @GetMapping("/duplicated/{transactionId}")
    public DuplicatedResponseDTO isDuplicated(@PathVariable Long transactionId){
        boolean transaction = consultingService.isDuplicated(transactionId);

        DuplicatedResponseDTO duplicatedResponseDTO = new DuplicatedResponseDTO(
                transactionId, transaction,
                "transactionId is"+ (transaction?" ":" not ")+ "duplicated");
        return duplicatedResponseDTO;
    }
}
