package br.psi.giganet.api.purchase.common.utils.statuses;

import br.psi.giganet.api.purchase.common.utils.model.enums.ProcessStatus;

public interface StatusesItem {
    ProcessStatus getStatus();
}
