package br.psi.giganet.api.purchase.branch_offices.adapter;

import br.psi.giganet.api.purchase.branch_offices.controller.request.InsertBranchOfficeRequest;
import br.psi.giganet.api.purchase.branch_offices.controller.request.UpdateBranchOfficeRequest;
import br.psi.giganet.api.purchase.branch_offices.controller.response.BranchOfficeProjection;
import br.psi.giganet.api.purchase.branch_offices.controller.response.BranchOfficeResponse;
import br.psi.giganet.api.purchase.branch_offices.model.BranchOffice;
import br.psi.giganet.api.purchase.common.address.model.Address;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class BranchOfficeAdapter {

    public BranchOffice create(Long id) {
        BranchOffice branchOffice = new BranchOffice();
        branchOffice.setId(id);

        return branchOffice;
    }

    public BranchOffice transform(InsertBranchOfficeRequest request) {
        BranchOffice branchOffice = new BranchOffice();
        branchOffice.setName(request.getName());
        branchOffice.setShortName(request.getShortName());
        branchOffice.setCnpj(request.getCnpj());
        branchOffice.setStateRegistration(request.getStateRegistration());
        branchOffice.setTelephone(request.getTelephone());

        branchOffice.setAddress(new Address());
        BeanUtils.copyProperties(request.getAddress(), branchOffice.getAddress(), Address.class);

        return branchOffice;
    }

    public BranchOffice transform(UpdateBranchOfficeRequest request) {
        BranchOffice branchOffice = new BranchOffice();
        branchOffice.setId(request.getId());
        branchOffice.setName(request.getName());
        branchOffice.setShortName(request.getShortName());
        branchOffice.setCnpj(request.getCnpj());
        branchOffice.setStateRegistration(request.getStateRegistration());
        branchOffice.setTelephone(request.getTelephone());

        branchOffice.setAddress(new Address());
        BeanUtils.copyProperties(request.getAddress(), branchOffice.getAddress(), Address.class);

        return branchOffice;
    }

    public BranchOfficeResponse transformToResponse(BranchOffice branchOffice) {
        BranchOfficeResponse response = new BranchOfficeResponse();
        response.setName(branchOffice.getName());
        response.setShortName(branchOffice.getShortName());
        response.setCnpj(branchOffice.getCnpj());
        response.setAddress(branchOffice.getAddress());
        response.setId(branchOffice.getId());
        response.setStateRegistration(branchOffice.getStateRegistration());
        response.setTelephone(branchOffice.getTelephone());

        if (branchOffice.getAddress() != null) {
            BeanUtils.copyProperties(branchOffice.getAddress(), response.getAddress());
        }

        return response;
    }

    public BranchOfficeProjection transform(BranchOffice branchOffice) {
        BranchOfficeProjection response = new BranchOfficeProjection();
        response.setId(branchOffice.getId());
        response.setName(branchOffice.getName());
        response.setShortName(branchOffice.getShortName());

        return response;
    }

    public BranchOfficeProjection transform(Long id, String name, String shortName) {
        BranchOfficeProjection response = new BranchOfficeProjection();
        response.setId(id);
        response.setName(name);
        response.setShortName(shortName);

        return response;
    }

    public BranchOfficeProjection transform(String shortName) {
        BranchOfficeProjection response = new BranchOfficeProjection();
        response.setShortName(shortName);

        return response;
    }

}
