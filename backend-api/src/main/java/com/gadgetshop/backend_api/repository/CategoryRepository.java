package com.gadgetshop.backend_api.repository;

import com.gadgetshop.backend_api.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}