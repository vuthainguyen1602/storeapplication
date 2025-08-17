package com.example.storeapplication.repository;

import com.example.storeapplication.domain.Deal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {
    @Query("SELECT d FROM Deal d WHERE d.active = true AND " +
            "(d.expirationDate IS NULL OR d.expirationDate > :now)")
    Page<Deal> findActiveDeals(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT d FROM Deal d WHERE d.product.id = :productId AND d.active = true AND " +
            "(d.expirationDate IS NULL OR d.expirationDate > :now)")
    List<Deal> findActiveDealsForProduct(@Param("productId") Long productId, @Param("now") LocalDateTime now);
}
