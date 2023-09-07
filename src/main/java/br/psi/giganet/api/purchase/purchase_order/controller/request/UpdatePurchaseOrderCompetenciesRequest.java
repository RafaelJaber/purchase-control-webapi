package br.psi.giganet.api.purchase.purchase_order.controller.request;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class UpdatePurchaseOrderCompetenciesRequest {

    @NotNull(message = "Id não pode ser nulo")
    private Long id;

    @NotNull(message = "Status não pode ser nulo")
    private ProcessStatus status;

    @Valid
    @NotEmpty(message = "Competências não podem ser nulas")
    private List<UpdatePurchaseOrderCompetence> competencies;

    private String note;

}
