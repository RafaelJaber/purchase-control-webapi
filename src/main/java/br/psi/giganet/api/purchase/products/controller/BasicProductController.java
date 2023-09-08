package br.psi.giganet.api.purchase.products.controller;

import br.psi.giganet.api.purchase.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.api.purchase.products.adapter.ProductAdapter;
import br.psi.giganet.api.purchase.products.categories.adapter.CategoryAdapter;
import br.psi.giganet.api.purchase.products.controller.request.ProductRequest;
import br.psi.giganet.api.purchase.products.controller.response.ProductProjection;
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
@RequestMapping("/basic/products")
public class BasicProductController {

    @Autowired
    private ProductService products;

    @Autowired
    private ProductAdapter adapter;

    @Autowired
    private CategoryAdapter categoryAdapter;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Optional<ProductProjection> insert(@RequestBody @Valid ProductRequest product) {
        return this.products.insert(adapter.transform(product)).map(adapter::transform);
    }

    @GetMapping()
    public Page<ProductProjection> findByNameContaining(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return this.products.findByNameContaining(name, page, pageSize).map(adapter::transform);
    }

    @PutMapping("/{id}")
    public Optional<ProductProjection> update(
            @PathVariable Long id,
            @RequestBody @Valid ProductRequest product) {
        return this.products.update(id, adapter.transform(product)).map(adapter::transform);
    }

    @GetMapping("/code/generate")
    public Map<String, String> generateByCategory(@RequestParam Long category) {
        return this.products.getNextProductCodeByCategory(categoryAdapter.create(category))
                .map(code -> Collections.singletonMap("code", code))
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
    }

    @PostMapping("/code/generate")
    public Map<String, String> generate(@RequestBody ProductRequest request) {
        return this.products.getNextProductCodeByCategory(categoryAdapter.create(request.getCategory()))
                .map(code -> Collections.singletonMap("code", code))
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));
    }
}
