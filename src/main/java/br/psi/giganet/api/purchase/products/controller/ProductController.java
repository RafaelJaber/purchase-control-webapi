package br.psi.giganet.api.purchase.products.controller;

import br.psi.giganet.api.purchase.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.api.purchase.products.adapter.ProductAdapter;
import br.psi.giganet.api.purchase.products.categories.adapter.CategoryAdapter;
import br.psi.giganet.api.purchase.products.controller.request.ProductRequest;
import br.psi.giganet.api.purchase.products.controller.response.ProductProjection;
import br.psi.giganet.api.purchase.products.controller.response.ProductResponse;
import br.psi.giganet.api.purchase.products.controller.response.ProductWithAvailableUnitsResponse;
import br.psi.giganet.api.purchase.products.controller.security.RoleProductsRead;
import br.psi.giganet.api.purchase.products.controller.security.RoleProductsWrite;
import br.psi.giganet.api.purchase.products.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService products;

    @Autowired
    private ProductAdapter adapter;

    @Autowired
    private CategoryAdapter categoryAdapter;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    //@RoleProductsWrite
    public Optional<ProductProjection> insert(@RequestBody @Valid ProductRequest product) {
        return this.products.insert(adapter.transform(product)).map(adapter::transform);
    }

    @PutMapping("/{id}")
    //@RoleProductsWrite
    public Optional<ProductProjection> update(
            @PathVariable Long id,
            @RequestBody @Valid ProductRequest product) {
        return this.products.update(id, adapter.transform(product)).map(adapter::transform);
    }

    @GetMapping
    @RoleProductsRead
    public Page<ProductProjection> findByNameContaining(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return this.products.findByNameContaining(name, page, pageSize).map(adapter::transform);
    }

    @GetMapping(params = {"name", "code"})
    @RoleProductsRead
    public Page<ProductProjection> findByNameAndCodeContaining(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String code,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return this.products.findByNameAndCodeContaining(name, code, page, pageSize).map(adapter::transform);
    }

    @GetMapping(params = {"code"})
    @RoleProductsRead
    public Page<ProductProjection> findByCodeContaining(
            @RequestParam(defaultValue = "") String code,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return this.products.findByCodeContaining(code, page, pageSize).map(adapter::transform);
    }

    @GetMapping(params = {"name", "withUnits"})
    @RoleProductsRead
    public Page<ProductWithAvailableUnitsResponse> findProductWithUnitsByName(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return this.products.findByNameContaining(name, page, pageSize)
                .map(adapter::transformToProductWithAvailableUnitsResponse);
    }

    @GetMapping("/{id}")
    @RoleProductsRead
    public ProductResponse findById(@PathVariable Long id) {
        return this.products.findById(id)
                .map(adapter::transformToFullResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
    }

    @GetMapping("/code/{code}")
    @RoleProductsRead
    public ProductResponse findByCode(@PathVariable String code) {
        return this.products.findByCode(code)
                .map(adapter::transformToFullResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
    }

    @GetMapping(path = "/code/{code}", params = {"withUnits"})
    @RoleProductsRead
    public ProductWithAvailableUnitsResponse findProductWithUnitsByCode(@PathVariable String code) {
        return this.products.findByCode(code)
                .map(adapter::transformToProductWithAvailableUnitsResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
    }

    @GetMapping("/code/generate")
    @RoleProductsRead
    public Map<String, String> generateByCategory(@RequestParam Long category) {
        return this.products.getNextProductCodeByCategory(categoryAdapter.create(category))
                .map(code -> Collections.singletonMap("code", code))
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
    }

}
