package br.psi.giganet.api.purchase.approvals.controller.response;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApprovalProjection {

    private Long id;
    private Long request;
    private String requester;
    private String description;
    private ProcessStatus status;
    private ZonedDateTime approvalDate;

}
