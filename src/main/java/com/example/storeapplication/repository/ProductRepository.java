package com.example.storeapplication.repository;

import com.example.storeapplication.domain.Product;
import com.example.storeapplication.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    public Product findByName(String name);

    @Query("select p from Product p where p.category = :category and p.price between" +
            " :minPrice and :maxPrice and p.available = :available")
    Page<Product> findAllWithFilter(@Param("category") Category category,
                                    @Param("minPrice") BigDecimal minPrice,
                                    @Param("maxPrice") BigDecimal maxPrice,
                                    @Param("available") Boolean available,
                                    Pageable pageable);

    @Query("select p from Product p where p.available = :available and p.stock > 0")
    Page<Product> findAvailableProducts(Pageable pageable);
}
