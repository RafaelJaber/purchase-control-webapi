package br.psi.giganet.api.purchase.cost_center.adapter;

import br.psi.giganet.api.purchase.cost_center.controller.request.CostCenterRequest;
import br.psi.giganet.api.purchase.cost_center.controller.response.CostCenterResponse;
import br.psi.giganet.api.purchase.cost_center.model.CostCenter;
import org.springframework.stereotype.Component;

@Component
public class CostCenterAdapter {

    public CostCenter create(Long id) {
        CostCenter costCenter = new CostCenter();
        costCenter.setId(id);

        return costCenter;
    }

    public CostCenter transform(CostCenterRequest request) {
        CostCenter costCenter = new CostCenter();
        costCenter.setName(request.getName());
        costCenter.setDescription(request.getDescription());
        return costCenter;
    }

    public CostCenterResponse transform(CostCenter costCenter) {
        CostCenterResponse response = new CostCenterResponse();
        response.setId(costCenter.getId());
        response.setName(costCenter.getName());
        response.setDescription(costCenter.getDescription());

        return response;
    }

}
