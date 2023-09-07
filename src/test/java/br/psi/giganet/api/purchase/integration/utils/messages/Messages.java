package br.psi.giganet.api.purchase.integration.utils.messages;

import lombok.Getter;

public enum Messages {

    NOT_EMPTY("Não pode ser vazio"),
    NOT_NULL("Não pode ser nulo"),
    EMAIL("Deve ser um email válido"),
    POSITIVE_NUMBER("O valor deve ser maior do que 0"),

    ;

    @Getter
    private String message;

    Messages(String message) {
        this.message = message;
    }
}
