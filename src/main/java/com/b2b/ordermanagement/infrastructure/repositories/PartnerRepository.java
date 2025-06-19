package com.b2b.ordermanagement.infrastructure.repositories;

import com.b2b.ordermanagement.domain.entities.Order;
import com.b2b.ordermanagement.domain.entities.Partner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Partner p WHERE p.id = :id")
    Optional<Partner> findByIdWithLock(@Param("id") String id);

    boolean existsByName(String name);

    @Query("SELECT o FROM Partner o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    Page<Partner> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,@Param("endDate") LocalDateTime endDate, Pageable pageable);
}