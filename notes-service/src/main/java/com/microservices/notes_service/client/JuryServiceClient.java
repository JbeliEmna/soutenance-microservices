package com.microservices.notes_service.client;

import com.microservices.notes_service.dto.JuryAffectationFeignResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "jury-service")
public interface JuryServiceClient {
    @GetMapping("/api/affectations-jury/soutenance/{soutenanceId}")
    List<JuryAffectationFeignResponse> getAffectationsBySoutenanceId(@PathVariable("soutenanceId") Long soutenanceId);
}
