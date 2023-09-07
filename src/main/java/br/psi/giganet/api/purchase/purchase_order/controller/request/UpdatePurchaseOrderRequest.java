package br.psi.giganet.api.purchase.purchase_order.controller.request;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class UpdatePurchaseOrderRequest {

    @NotNull(message = "Id não pode ser nulo")
    private Long id;

    private String note;

    @NotNull(message = "Situação não pode ser nula")
    private ProcessStatus status;

    @NotNull(message = "Filial não pode ser nula")
    private Long branchOffice;

    @Valid
    @NotNull(message = "Frete não pode ser nulo")
    private PurchaseOrderFreightRequest freight;

    @Valid
    @NotNull(message = "Condição de pagamento não pode ser nulo")
    private UpdatePaymentConditionRequest paymentCondition;

    @Valid
    @NotEmpty(message = "Competências não podem ser nulas")
    private List<UpdatePurchaseOrderCompetence> competencies;
}
