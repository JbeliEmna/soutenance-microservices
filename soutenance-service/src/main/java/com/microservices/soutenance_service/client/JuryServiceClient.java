package com.microservices.soutenance_service.client;

import com.microservices.soutenance_service.dto.JuryAffectationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "jury-service")
public interface JuryServiceClient {
    @GetMapping("/api/affectations-jury/soutenance/{soutenanceId}")
    List<JuryAffectationResponse> getAffectationsBySoutenanceId(@PathVariable("soutenanceId") Long soutenanceId);
}
