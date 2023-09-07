package br.psi.giganet.api.purchase.common.settings.service;

import br.psi.giganet.api.purchase.common.settings.model.SettingOptions;
import br.psi.giganet.api.purchase.common.settings.repository.SettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class SettingsService {

    @Autowired
    private SettingRepository settingRepository;

    public BigDecimal getMinimalAmountToAutoApproveQuotation() {
        return settingRepository.findByKey(SettingOptions.MINIMAL_QUANTITY_TO_AUTO_APPROVE_QUOTATION.name())
                .map(setting -> new BigDecimal(setting.getValue()))
                .orElse(BigDecimal.ZERO);
    }

}
