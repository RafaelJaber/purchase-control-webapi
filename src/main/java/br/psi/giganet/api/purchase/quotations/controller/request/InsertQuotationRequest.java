package br.psi.giganet.api.purchase.quotations.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InsertQuotationRequest {

    private String note;
    @NotEmpty(message = "Deve ser informado pelo menos um produto")
    @Valid
    private List<InsertQuotedItemRequest> products;

    @NotNull(message = "Centro de custo nao pode ser nulo")
    private Long costCenter;

    @NotNull(message = "Filial nao pode ser nula")
    private Long branchOffice;

    private String description;

    private String externalLink;

    private String dateOfNeed;

    private Long project;

    private Long location;

    @Valid
    @NotNull(message = "Frete não pode ser nulo")
    private QuotationFreightRequest freight;

    @Valid
    @NotNull(message = "Condição de pagamento não pode ser nulo")
    private InsertPaymentConditionRequest paymentCondition;

}
