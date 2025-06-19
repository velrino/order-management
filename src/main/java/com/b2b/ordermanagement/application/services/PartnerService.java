package com.b2b.ordermanagement.application.services;

import com.b2b.ordermanagement.application.dto.OrderResponseDTO;
import com.b2b.ordermanagement.application.dto.PartnerResponseDTO;
import com.b2b.ordermanagement.application.interfaces.PartnerFilterParams;
import com.b2b.ordermanagement.domain.entities.Partner;
import com.b2b.ordermanagement.infrastructure.repositories.PartnerRepository;
import com.b2b.ordermanagement.shared.exceptions.BusinessException;
import com.b2b.ordermanagement.shared.exceptions.ResourceNotFoundException;
import com.b2b.ordermanagement.shared.mappers.OrderMapper;
import com.b2b.ordermanagement.shared.mappers.PartnerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class PartnerService {

    private final PartnerMapper partnerMapper;

    private static final Logger logger = LoggerFactory.getLogger(PartnerService.class);

    private final PartnerRepository partnerRepository;

    public PartnerService(PartnerRepository partnerRepository, PartnerMapper partnerMapper) {
        this.partnerRepository = partnerRepository;
        this.partnerMapper = partnerMapper;
    }

    @Transactional(readOnly = true)
    public Partner getPartnerEntityById(String partnerId) {
        try {
            return partnerRepository.findById(partnerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Partner not found: " + partnerId));
        } catch (ResourceNotFoundException e) {
            logger.error("Partner not found: {}", partnerId);
            throw new ResourceNotFoundException("Partner not found: " + partnerId);
        }

    }

    public void debitCredit(String partnerId, BigDecimal amount) {
        logger.info("Debiting credit for partner: {} amount: {}", partnerId, amount);

        Partner partner = partnerRepository.findByIdWithLock(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found: " + partnerId));

        if (!partner.hasAvailableCredit(amount)) {
            throw new BusinessException("Insufficient credit available for partner: " + partnerId);
        }

        partner.debitCredit(amount);
        partnerRepository.save(partner);

        logger.info("Credit debited successfully for partner: {} new available credit: {}",
                partnerId, partner.getAvailableCredit());
    }

    public void restoreCredit(String partnerId, BigDecimal amount) {
        logger.info("Restoring credit for partner: {} amount: {}", partnerId, amount);

        Partner partner = partnerRepository.findByIdWithLock(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found: " + partnerId));

        partner.creditCredit(amount);
        partnerRepository.save(partner);

        logger.info("Credit restored successfully for partner: {} new available credit: {}",
                partnerId, partner.getAvailableCredit());
    }

    public Partner createPartner(String id, String name, BigDecimal creditLimit) {
        logger.info("Creating partner: {} with credit limit: {}", name, creditLimit);

        if (partnerRepository.existsById(id)) {
            throw new BusinessException("Partner with ID already exists: " + id);
        }

        if (partnerRepository.existsByName(name)) {
            throw new BusinessException("Partner with name already exists: " + name);
        }

        Partner partner = new Partner(id, name, creditLimit);
        Partner savedPartner = partnerRepository.save(partner);

        logger.info("Partner created successfully: {}", savedPartner.getId());
        return savedPartner;
    }

    @Transactional(readOnly = true)
    public Page<PartnerResponseDTO> getFiltered(PartnerFilterParams filters, Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "created_at")
            );
        }

        Page<Partner> partners;

        if (filters.hasDateRange()) {
            partners = partnerRepository.findByCreatedAtBetween(filters.getStartDate(), filters.getEndDate(), pageable);
        } else {
            // All orders without filter
            partners = partnerRepository.findAll(pageable);
        }

        return partners.map(partnerMapper::toResponseDTO);
    }

}