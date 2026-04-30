package com.microservices.soutenance_service.dto;

import java.util.List;

public record ResultatFeignResponse(
        Long soutenanceId,
        Double noteFinale,
        String mention,
        List<Long> etudiantIds
) {
}
