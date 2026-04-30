package com.microservices.notes_service.client;

import com.microservices.notes_service.dto.SoutenanceFeignResponse;
import com.microservices.notes_service.dto.UpdateSoutenanceEtatFeignRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "soutenance-service")
public interface SoutenanceServiceClient {
    @GetMapping("/api/soutenances/{id}")
    SoutenanceFeignResponse getSoutenanceById(@PathVariable("id") Long id);

    @PutMapping("/api/soutenances/{id}/etat")
    SoutenanceFeignResponse updateEtat(
            @PathVariable("id") Long id,
            @RequestBody UpdateSoutenanceEtatFeignRequest request
    );
}
