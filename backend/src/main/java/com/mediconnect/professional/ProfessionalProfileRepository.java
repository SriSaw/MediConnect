package com.mediconnect.professional;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfessionalProfileRepository extends JpaRepository<ProfessionalProfile, Long>, JpaSpecificationExecutor<ProfessionalProfile> {

    Optional<ProfessionalProfile> findByUserId(Long userId);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProfessionalProfile p WHERE p.id = :id")
    Optional<ProfessionalProfile> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT p FROM ProfessionalProfile p " +
           "JOIN p.user u " +
           "WHERE (:verified IS NULL OR p.verified = :verified) " +
           "AND (:specialization IS NULL OR LOWER(p.specialization) = LOWER(:specialization)) " +
           "AND (:query IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.bio) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<ProfessionalProfile> searchProfessionals(
            @Param("specialization") String specialization,
            @Param("query") String query,
            @Param("verified") Boolean verified,
            Pageable pageable
    );
}
