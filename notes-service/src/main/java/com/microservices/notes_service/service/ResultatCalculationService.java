package com.microservices.notes_service.service;

import com.microservices.notes_service.enums.MentionFinale;
import com.microservices.notes_service.model.Evaluation;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ResultatCalculationService {

    public double calculerMoyenne(List<Evaluation> evaluations) {
        double moyenne = evaluations.stream()
                .mapToDouble(Evaluation::getNote)
                .average()
                .orElse(0.0);

        return BigDecimal.valueOf(moyenne)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public MentionFinale determinerMention(double moyenne) {
        if (moyenne < 10.0) {
            return MentionFinale.AJOURNE;
        }
        if (moyenne < 12.0) {
            return MentionFinale.PASSABLE;
        }
        if (moyenne < 14.0) {
            return MentionFinale.ASSEZ_BIEN;
        }
        if (moyenne < 16.0) {
            return MentionFinale.BIEN;
        }
        return MentionFinale.TRES_BIEN;
    }
}
