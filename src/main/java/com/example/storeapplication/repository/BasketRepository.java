package com.example.storeapplication.repository;

import com.example.storeapplication.domain.Basket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BasketRepository extends JpaRepository<Basket, Long> {
    Optional<Basket> findBySessionId(String sessionId);
}
