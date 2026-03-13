package com.DANN.SmartHome.domain.repository;

import com.DANN.SmartHome.domain.entity.AutomationConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutomationConfigRepository extends JpaRepository<AutomationConfig, Short> {
}
