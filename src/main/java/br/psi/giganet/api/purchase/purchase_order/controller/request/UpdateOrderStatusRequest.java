package br.psi.giganet.api.purchase.purchase_order.controller.request;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class UpdateOrderStatusRequest {

    @NotNull(message = "O status não pode ser nulo")
    private ProcessStatus status;

}
