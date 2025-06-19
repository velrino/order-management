package com.b2b.ordermanagement.shared.mappers;

import com.b2b.ordermanagement.application.dto.PartnerResponseDTO;
import com.b2b.ordermanagement.domain.entities.Partner;
import org.springframework.stereotype.Component;

@Component
public class PartnerMapper {

    public PartnerResponseDTO toResponseDTO(Partner partner) {
        if (partner == null) {
            return null;
        }

        return new PartnerResponseDTO(
                partner.getId(),
                partner.getName(),
                partner.getCreditLimit(),
                partner.getAvailableCredit(),
                partner.getCreatedAt(),
                partner.getUpdatedAt()
        );
    }
}