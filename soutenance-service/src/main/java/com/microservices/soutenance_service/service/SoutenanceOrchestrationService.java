package com.microservices.soutenance_service.service;

import com.microservices.soutenance_service.client.JuryServiceClient;
import com.microservices.soutenance_service.client.NotesServiceClient;
import com.microservices.soutenance_service.dto.EvaluationFeignResponse;
import com.microservices.soutenance_service.dto.JuryAffectationResponse;
import com.microservices.soutenance_service.dto.ResultatFeignResponse;
import com.microservices.soutenance_service.dto.SoutenanceDetailsResponse;
import com.microservices.soutenance_service.dto.SoutenanceResponse;
import feign.FeignException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
public class SoutenanceOrchestrationService {

    private final SoutenanceService soutenanceService;
    private final JuryServiceClient juryServiceClient;
    private final NotesServiceClient notesServiceClient;

    public SoutenanceOrchestrationService(
            SoutenanceService soutenanceService,
            JuryServiceClient juryServiceClient,
            NotesServiceClient notesServiceClient
    ) {
        this.soutenanceService = soutenanceService;
        this.juryServiceClient = juryServiceClient;
        this.notesServiceClient = notesServiceClient;
    }

    public SoutenanceDetailsResponse getDetails(Long soutenanceId) {
        SoutenanceResponse soutenance = soutenanceService.getById(soutenanceId);

        List<JuryAffectationResponse> jury = getOptional(
                () -> juryServiceClient.getAffectationsBySoutenanceId(soutenanceId),
                List.of()
        );
        List<EvaluationFeignResponse> evaluations = getOptional(
                () -> notesServiceClient.getEvaluationsBySoutenanceId(soutenanceId),
                List.of()
        );
        ResultatFeignResponse resultat = getOptional(
                () -> notesServiceClient.getResultatBySoutenanceId(soutenanceId),
                null
        );

        return new SoutenanceDetailsResponse(soutenance, jury, evaluations, resultat);
    }

    private <T> T getOptional(Supplier<T> supplier, T fallback) {
        try {
            return supplier.get();
        } catch (FeignException.NotFound ignored) {
            return fallback;
        }
    }
}
