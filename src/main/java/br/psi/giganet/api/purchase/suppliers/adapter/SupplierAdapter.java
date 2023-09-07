package br.psi.giganet.api.purchase.suppliers.adapter;

import br.psi.giganet.api.purchase.common.address.model.Address;
import br.psi.giganet.api.purchase.quotations.controller.request.SupplierEmailDestinyRequest;
import br.psi.giganet.api.purchase.suppliers.controller.request.SupplierRequest;
import br.psi.giganet.api.purchase.suppliers.controller.response.*;
import br.psi.giganet.api.purchase.suppliers.model.Supplier;
import br.psi.giganet.api.purchase.suppliers.taxes.adapter.TaxAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
public class SupplierAdapter {

    @Autowired
    private TaxAdapter taxAdapter;

    public Supplier create(final Long id) {
        final Supplier s = new Supplier();
        s.setId(id);
        return s;
    }

    public Supplier transform(SupplierEmailDestinyRequest supplier){
        Supplier s = create(supplier.getId());
        s.setEmail(supplier.getEmail());

        return s;
    }

    public Supplier transform(SupplierRequest request) {
        Supplier s = new Supplier();
        s.setName(request.getName());
        s.setCellphone(request.getCellphone());
        s.setTelephone(request.getTelephone());
        s.setEmail(request.getEmail());
        s.setDescription(request.getDescription());
        s.setCnpj(request.getCnpj());
        s.setCpf(request.getCpf());
        s.setStateRegistration(request.getStateRegistration());
        s.setMunicipalRegistration(request.getMunicipalRegistration());

        s.setAddress(new Address());
        s.getAddress().setPostalCode(request.getAddress().getPostalCode());
        s.getAddress().setComplement(request.getAddress().getComplement());
        s.getAddress().setCity(request.getAddress().getCity());
        s.getAddress().setDistrict(request.getAddress().getDistrict());
        s.getAddress().setNumber(request.getAddress().getNumber());
        s.getAddress().setState(request.getAddress().getState());
        s.getAddress().setStreet(request.getAddress().getStreet());

        return s;
    }

    public SupplierProjection transform(Supplier supplier) {
        SupplierProjection s = new SupplierProjection();
        s.setName(supplier.getName());
        s.setId(supplier.getId());

        return s;
    }

    public SupplierProjection transform(Long id, String name) {
        SupplierProjection s = new SupplierProjection();
        s.setName(name);
        s.setId(id);

        return s;
    }

    public SupplierProjectionAndEmail transformProjectionAndEmail(Supplier supplier) {
        SupplierProjectionAndEmail s = new SupplierProjectionAndEmail();
        s.setId(supplier.getId());
        s.setName(supplier.getName());
        s.setEmail(supplier.getEmail());

        return s;
    }

    public SupplierProjectionWithCPFAndCNPJ transformProjectionWithCPFAndCNPJ(Supplier supplier) {
        SupplierProjectionWithCPFAndCNPJ s = new SupplierProjectionWithCPFAndCNPJ();
        s.setId(supplier.getId());
        s.setName(supplier.getName());
        s.setCnpj(supplier.getCnpj());
        s.setCpf(supplier.getCpf());

        return s;
    }

    @Transactional
    public SupplierProjectionAndTax transformProjectionAndTax(Supplier supplier) {
        SupplierProjectionAndTax s = new SupplierProjectionAndTax();
        s.setId(supplier.getId());
        s.setName(supplier.getName());
        s.setIcms(supplier.getTax().getIcms());

        return s;
    }

    @Transactional
    public SupplierResponse transformToResponse(Supplier supplier) {
        SupplierResponse response = new SupplierResponse();
        response.setId(supplier.getId());
        response.setName(supplier.getName());
        response.setCellphone(supplier.getCellphone());
        response.setTelephone(supplier.getTelephone());
        response.setEmail(supplier.getEmail());
        response.setDescription(supplier.getDescription());
        response.setCnpj(supplier.getCnpj());
        response.setCpf(supplier.getCpf());
        response.setStateRegistration(supplier.getStateRegistration());
        response.setMunicipalRegistration(supplier.getMunicipalRegistration());

        if (supplier.getAddress() != null) {
            if (response.getAddress() == null) {
                response.setAddress(new Address());
            }

            response.getAddress().setComplement(supplier.getAddress().getComplement());
            response.getAddress().setPostalCode(supplier.getAddress().getPostalCode());
            response.getAddress().setCity(supplier.getAddress().getCity());
            response.getAddress().setDistrict(supplier.getAddress().getDistrict());
            response.getAddress().setNumber(supplier.getAddress().getNumber());
            response.getAddress().setState(supplier.getAddress().getState());
            response.getAddress().setStreet(supplier.getAddress().getStreet());
        }

        response.setTax(taxAdapter.transform(supplier.getTax()));

        return response;
    }


    public Supplier copyProperties(Supplier source, Supplier target) {
        target.setName(source.getName());
        target.setCellphone(source.getCellphone());
        target.setTelephone(source.getTelephone());
        target.setEmail(source.getEmail());
        target.setDescription(source.getDescription());
        target.setCnpj(source.getCnpj());
        target.setCpf(source.getCpf());
        target.setStateRegistration(source.getStateRegistration());
        target.setMunicipalRegistration(source.getMunicipalRegistration());

        if (source.getAddress() != null) {
            if (target.getAddress() == null) {
                target.setAddress(new Address());
            }

            target.getAddress().setComplement(source.getAddress().getComplement());
            target.getAddress().setPostalCode(source.getAddress().getPostalCode());
            target.getAddress().setCity(source.getAddress().getCity());
            target.getAddress().setDistrict(source.getAddress().getDistrict());
            target.getAddress().setNumber(source.getAddress().getNumber());
            target.getAddress().setState(source.getAddress().getState());
            target.getAddress().setStreet(source.getAddress().getStreet());
        }

        return target;
    }

}
