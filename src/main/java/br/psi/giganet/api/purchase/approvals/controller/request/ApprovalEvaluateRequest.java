package br.psi.giganet.api.purchase.approvals.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ApprovalEvaluateRequest {

    @NotEmpty(message = "É necessário informar pelo menos 1 item")
    @Valid
    private List<ApprovalItemEvaluate> items;
    private String note;
}
