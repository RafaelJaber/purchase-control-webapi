package br.psi.giganet.api.purchase.purchase_order.controller.response;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ItemResponseWithDetails extends PurchaseOrderItemResponse {

    private Long purchaseOrder;
    private ZonedDateTime date;

}
