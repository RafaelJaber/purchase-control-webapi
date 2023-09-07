package br.psi.giganet.api.purchase.suppliers.taxes.adapter;

import br.psi.giganet.api.purchase.suppliers.taxes.controller.request.TaxRequest;
import br.psi.giganet.api.purchase.suppliers.taxes.controller.response.TaxResponse;
import br.psi.giganet.api.purchase.suppliers.taxes.model.Tax;
import org.springframework.stereotype.Component;

@Component
public class TaxAdapter {

    public Tax transform(TaxRequest request){
        Tax tax = new Tax();
        tax.setStateTo("MG");
        tax.setStateFrom(request.getFrom());
        tax.setIcms(request.getIcms());

        return tax;
    }

    public TaxResponse transform(Tax tax){
        TaxResponse response = new TaxResponse();
        response.setFrom(tax.getStateFrom());
        response.setTo(tax.getStateTo());
        response.setIcms(tax.getIcms());
        response.setId(tax.getId());

        return response;
    }

}
