package br.psi.giganet.api.purchase.locations.controller.response;

import lombok.Data;

@Data
public class LocationProjection {

    private Long id;
    private String name;
    private String description;
}
