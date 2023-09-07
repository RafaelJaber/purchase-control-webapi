package br.psi.giganet.api.purchase.quotations.controller.request;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class SendEmailWithQuotationRequest {

    @NotEmpty(message = "Assunto não pode ser vazio")
    private String subject;
    @NotNull(message = "Mensagem não pode ser nula")
    private String message;

    @Valid
    private List<SupplierEmailDestinyRequest> suppliers;
}
