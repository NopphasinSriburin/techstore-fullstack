package com.gadgetshop.backend_api.repository;

import com.gadgetshop.backend_api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    
}