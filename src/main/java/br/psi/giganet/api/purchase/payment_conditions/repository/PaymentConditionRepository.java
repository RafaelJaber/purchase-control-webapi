package br.psi.giganet.api.purchase.payment_conditions.repository;

import br.psi.giganet.api.purchase.payment_conditions.model.PaymentCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentConditionRepository extends JpaRepository<PaymentCondition, Long> {
}
