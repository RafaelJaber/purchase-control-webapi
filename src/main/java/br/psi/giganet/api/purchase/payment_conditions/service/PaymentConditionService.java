package br.psi.giganet.api.purchase.payment_conditions.service;

import br.psi.giganet.api.purchase.payment_conditions.model.PaymentCondition;
import br.psi.giganet.api.purchase.payment_conditions.repository.PaymentConditionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentConditionService {

    @Autowired
    private PaymentConditionRepository paymentConditionRepository;

    public List<PaymentCondition> findAll() {
        return paymentConditionRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Optional<PaymentCondition> findById(Long id) {
        return paymentConditionRepository.findById(id);
    }

    public Optional<PaymentCondition> insert(PaymentCondition paymentCondition) {
        if (paymentCondition.getNumberOfInstallments() > 1 &&
                (paymentCondition.getDaysInterval() == null || paymentCondition.getDaysInterval() < 1)) {
            throw new IllegalArgumentException("Intervalo entre dias é inválido");
        }
        return Optional.of(paymentConditionRepository.save(paymentCondition));
    }

    @Transactional
    public Optional<PaymentCondition> update(Long id, PaymentCondition paymentCondition) {
        return paymentConditionRepository.findById(id)
                .map(saved -> {
                    saved.setName(paymentCondition.getName());
                    saved.setDaysInterval(paymentCondition.getDaysInterval());
                    saved.setNumberOfInstallments(paymentCondition.getNumberOfInstallments());
                    saved.setDescription(paymentCondition.getDescription());

                    return paymentConditionRepository.save(saved);
                });
    }

}
