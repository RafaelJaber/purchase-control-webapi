package br.psi.giganet.api.purchase.products.adapter;

import br.psi.giganet.api.purchase.products.categories.adapter.CategoryAdapter;
import br.psi.giganet.api.purchase.products.controller.request.ProductRequest;
import br.psi.giganet.api.purchase.products.controller.response.ProductProjection;
import br.psi.giganet.api.purchase.products.controller.response.ProductProjectionNameAndCodeOnly;
import br.psi.giganet.api.purchase.products.controller.response.ProductResponse;
import br.psi.giganet.api.purchase.products.controller.response.ProductWithAvailableUnitsResponse;
import br.psi.giganet.api.purchase.products.model.Product;
import br.psi.giganet.api.purchase.units.adapter.UnitAdapter;
import br.psi.giganet.api.purchase.units.controller.response.UnitProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProductAdapter {

    @Autowired
    private UnitAdapter unitAdapter;

    @Autowired
    private CategoryAdapter categoryAdapter;

    public Product create(final Long id) {
        final Product p = new Product();
        p.setId(id);
        return p;
    }

    public Product create(final String code) {
        final Product p = new Product();
        p.setCode(code);
        return p;
    }

    public Product transform(ProductRequest request) {
        Product p = new Product();
        p.setName(request.getName());
        p.setCode(request.getCode());
        p.setCategory(categoryAdapter.create(request.getCategory()));
        p.setDescription(request.getDescription());
        p.setManufacturer(request.getManufacturer());
        p.setUnit(unitAdapter.create(request.getUnit()));

        return p;
    }

    public ProductProjection transform(Product product) {
        ProductProjection p = new ProductProjection();
        p.setName(product.getName());
        p.setCode(product.getCode());
        p.setManufacturer(product.getManufacturer());
        p.setId(product.getId());
        p.setUnit(unitAdapter.transform(product.getUnit()));

        return p;
    }

    public ProductProjectionNameAndCodeOnly transformToNameAndCodeOnly(String code, String name) {
        ProductProjectionNameAndCodeOnly p = new ProductProjectionNameAndCodeOnly();
        p.setCode(code);
        p.setName(name);

        return p;
    }


    public ProductResponse transformToFullResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setCode(product.getCode());
        response.setCategory(categoryAdapter.transform(product.getCategory()));
        response.setDescription(product.getDescription());
        response.setManufacturer(product.getManufacturer());
        response.setUnit(unitAdapter.transformToFullResponse(product.getUnit()));

        return response;
    }

    @Transactional
    public ProductWithAvailableUnitsResponse transformToProductWithAvailableUnitsResponse(Product product) {
        ProductProjection p = new ProductProjection();
        p.setName(product.getName());
        p.setCode(product.getCode());
        p.setManufacturer(product.getManufacturer());
        p.setId(product.getId());
        p.setUnit(unitAdapter.transform(product.getUnit()));

        Set<UnitProjection> units = new HashSet<>();
        units.add(p.getUnit());
        if (product.getUnit().getConversions() != null) {
            units.addAll(product.getUnit().getConversions().stream()
                    .map(conversion -> unitAdapter.transform(conversion.getTo()))
                    .collect(Collectors.toSet()));
        }

        return new ProductWithAvailableUnitsResponse(p, units);
    }

}
