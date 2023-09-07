package br.psi.giganet.api.purchase.products.categories.controller;

import br.psi.giganet.api.purchase.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.api.purchase.products.categories.adapter.CategoryAdapter;
import br.psi.giganet.api.purchase.products.categories.controller.request.CategoryRequest;
import br.psi.giganet.api.purchase.products.categories.controller.response.CategoryResponse;
import br.psi.giganet.api.purchase.products.categories.service.CategoryService;
import br.psi.giganet.api.purchase.products.controller.security.RoleProductsRead;
import br.psi.giganet.api.purchase.products.controller.security.RoleProductsWrite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("products/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryAdapter categoryAdapter;

    @GetMapping
    @RoleProductsRead
    public List<CategoryResponse> findAll() {
        return categoryService.findAll()
                .stream()
                .map(c -> categoryAdapter.transform(c, CategoryAdapter.ResponseType.RESPONSE_WITH_PREFIX))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @RoleProductsRead
    public CategoryResponse findById(@PathVariable Long id) {
        return categoryService.findById(id)
                .map(c -> categoryAdapter.transform(c, CategoryAdapter.ResponseType.RESPONSE_WITH_PREFIX))
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RoleProductsWrite
    public CategoryResponse insert(@Valid @RequestBody CategoryRequest request) {
        return categoryService.insert(categoryAdapter.transform(request))
                .map(c -> categoryAdapter.transform(c, CategoryAdapter.ResponseType.RESPONSE_WITH_PREFIX))
                .orElseThrow(() -> new RuntimeException("Não foi possível cadastrar esta categoria"));
    }

    @PutMapping("/{id}")
    @RoleProductsWrite
    public CategoryResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(id, categoryAdapter.transform(request))
                .map(c -> categoryAdapter.transform(c, CategoryAdapter.ResponseType.RESPONSE_WITH_PREFIX))
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));
    }
}
