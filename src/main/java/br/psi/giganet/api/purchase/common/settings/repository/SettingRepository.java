package br.psi.giganet.api.purchase.common.settings.repository;

import br.psi.giganet.api.purchase.common.settings.model.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {

    Optional<Setting> findByKey(String key);

}
