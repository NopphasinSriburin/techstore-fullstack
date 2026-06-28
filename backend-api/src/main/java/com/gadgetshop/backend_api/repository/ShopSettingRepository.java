package com.gadgetshop.backend_api.repository;

import com.gadgetshop.backend_api.model.ShopSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopSettingRepository extends JpaRepository<ShopSetting, Long> {
}