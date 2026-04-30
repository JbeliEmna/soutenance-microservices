package com.microservices.soutenance_service.client;

import com.microservices.soutenance_service.dto.EvaluationFeignResponse;
import com.microservices.soutenance_service.dto.ResultatFeignResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "notes-service")
public interface NotesServiceClient {
    @GetMapping("/api/evaluations/soutenance/{soutenanceId}")
    List<EvaluationFeignResponse> getEvaluationsBySoutenanceId(@PathVariable("soutenanceId") Long soutenanceId);

    @GetMapping("/api/resultats/soutenances/{soutenanceId}")
    ResultatFeignResponse getResultatBySoutenanceId(@PathVariable("soutenanceId") Long soutenanceId);
}
