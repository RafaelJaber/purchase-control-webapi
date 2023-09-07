package br.psi.giganet.api.purchase.payment_conditions.controller;

import br.psi.giganet.api.purchase.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.api.purchase.payment_conditions.adapter.PaymentConditionAdapter;
import br.psi.giganet.api.purchase.payment_conditions.controller.request.PaymentConditionRequest;
import br.psi.giganet.api.purchase.payment_conditions.controller.response.PaymentConditionResponse;
import br.psi.giganet.api.purchase.payment_conditions.controller.security.RolePaymentConditionRead;
import br.psi.giganet.api.purchase.payment_conditions.controller.security.RolePaymentConditionWrite;
import br.psi.giganet.api.purchase.payment_conditions.service.PaymentConditionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("payment-conditions")
public class PaymentConditionController {

    @Autowired
    private PaymentConditionService paymentConditionService;

    @Autowired
    private PaymentConditionAdapter paymentConditionAdapter;

    @GetMapping
    @RolePaymentConditionRead
    public List<PaymentConditionResponse> findAll() {
        return paymentConditionService.findAll()
                .stream()
                .map(paymentConditionAdapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @RolePaymentConditionRead
    public PaymentConditionResponse findById(@PathVariable Long id) {
        return paymentConditionService.findById(id)
                .map(paymentConditionAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Condição de pagamento não encontrada"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RolePaymentConditionWrite
    public PaymentConditionResponse insert(@Valid @RequestBody PaymentConditionRequest request) {
        return paymentConditionService.insert(paymentConditionAdapter.transform(request))
                .map(paymentConditionAdapter::transform)
                .orElseThrow(() -> new RuntimeException("Não foi possível cadastrar esta condição de pagamento"));
    }

    @PutMapping("/{id}")
    @RolePaymentConditionWrite
    public PaymentConditionResponse update(
            @PathVariable Long id,
            @Valid @RequestBody PaymentConditionRequest request) {
        return paymentConditionService.update(id, paymentConditionAdapter.transform(request))
                .map(paymentConditionAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Condição de pagamento não encontrada"));
    }
}
