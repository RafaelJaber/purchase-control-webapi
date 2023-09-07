package br.psi.giganet.api.purchase.purchase_requests.controller.request;

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
public class UpdatePurchaseRequest {

    @NotNull(message = "Responsável nao pode ser nulo")
    private Long responsible;
    @NotEmpty(message = "Motivo nao pode ser vazio/nulo")
    private String reason;
    @NotNull(message = "Centro de custo nao pode ser nulo")
    private Long costCenter;
    @NotNull(message = "Filial nao pode ser nula")
    private Long branchOffice;

    private String dateOfNeed;
    private String description;
    private String note;

    @NotNull(message = "Produtos não pode ser nulo")
    @NotEmpty(message = "Deve conter ao menos 1 produto")
    @Valid
    private List<UpdatePurchaseRequestItem> products;

}
