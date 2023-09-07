package br.psi.giganet.api.purchase.approvals.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApprovalItemResponseWithTrace extends ApprovalItemResponse {

    private Long approval;
    private ItemTraceResponse approvedTrace;
    private ItemTraceResponse pendingTrace;

}