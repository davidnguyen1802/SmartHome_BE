package com.DANN.SmartHome.service.Imp;

import com.DANN.SmartHome.DTO.Response.AutomationConfigResponse;
import com.DANN.SmartHome.domain.entity.AutomationConfig;
import com.DANN.SmartHome.domain.repository.AutomationConfigRepository;
import com.DANN.SmartHome.service.AutomationConfigService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class AutomationConfigServiceImp implements AutomationConfigService {

    private final AutomationConfigRepository automationConfigRepository;

    @Override
    public AutomationConfigResponse getConfig() {
        return toResponse(getConfigEntity());
    }

    @Override
    public AutomationConfigResponse updateFanThreshold(BigDecimal lowTemp, BigDecimal highTemp) {
        if (lowTemp == null || highTemp == null) {
            throw new IllegalArgumentException("lowTemp and highTemp must not be null");
        }

        if (highTemp.compareTo(lowTemp) < 0) {
            throw new IllegalArgumentException("highTemp must be greater than or equal to lowTemp");
        }

        AutomationConfig config = getConfigEntity();
        config.setFanLowTemp(lowTemp);
        config.setFanHighTemp(highTemp);

        automationConfigRepository.save(config);
        return toResponse(config);
    }

    private AutomationConfig getConfigEntity() {
        return automationConfigRepository.findById((short) 1)
                .orElseThrow(() -> new IllegalStateException("Automation config not found"));
    }

    private AutomationConfigResponse toResponse(AutomationConfig config) {
        return new AutomationConfigResponse(
                config.getFanLowTemp(),
                config.getFanHighTemp(),
                config.getLedOnThreshold(),
                config.getLedOffThreshold(),
                config.getPirAlertCooldownSeconds()
        );
    }
}
