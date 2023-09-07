package br.psi.giganet.api.purchase.locations.controller.response;

import br.psi.giganet.api.purchase.branch_offices.controller.response.BranchOfficeProjection;
import lombok.Data;

@Data
public class LocationResponse {

    private Long id;
    private String name;
    private String description;
    private BranchOfficeProjection branchOffice;
}
