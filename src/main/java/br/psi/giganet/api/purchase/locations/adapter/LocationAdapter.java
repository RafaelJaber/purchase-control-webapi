package br.psi.giganet.api.purchase.locations.adapter;

import br.psi.giganet.api.purchase.branch_offices.adapter.BranchOfficeAdapter;
import br.psi.giganet.api.purchase.locations.controller.request.InsertLocationRequest;
import br.psi.giganet.api.purchase.locations.controller.request.UpdateLocationRequest;
import br.psi.giganet.api.purchase.locations.controller.response.LocationProjection;
import br.psi.giganet.api.purchase.locations.controller.response.LocationResponse;
import br.psi.giganet.api.purchase.locations.model.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LocationAdapter {

    @Autowired
    private BranchOfficeAdapter branchOfficeAdapter;

    public Location create(Long id) {
        Location location = new Location();
        location.setId(id);

        return location;
    }

    public Location transform(InsertLocationRequest request) {
        Location location = new Location();
        location.setName(request.getName());
        location.setDescription(request.getDescription());
        location.setBranchOffice(branchOfficeAdapter.create(request.getBranchOffice()));
        return location;
    }

    public Location transform(UpdateLocationRequest request) {
        Location location = new Location();
        location.setName(request.getName());
        location.setDescription(request.getDescription());
        location.setId(request.getId());
        location.setBranchOffice(branchOfficeAdapter.create(request.getBranchOffice()));
        return location;
    }

    public LocationProjection transform(Location location) {
        LocationProjection response = new LocationProjection();
        response.setId(location.getId());
        response.setName(location.getName());
        response.setDescription(location.getDescription());
        return response;
    }

    public LocationResponse transformToResponse(Location location) {
        LocationResponse response = new LocationResponse();
        response.setId(location.getId());
        response.setName(location.getName());
        response.setDescription(location.getDescription());
        response.setBranchOffice(branchOfficeAdapter.transform(location.getBranchOffice()));
        return response;
    }

}
