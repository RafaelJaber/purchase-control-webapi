package br.psi.giganet.api.purchase.products.service;

import br.psi.giganet.api.purchase.common.webhooks.services.WebhooksHandlerService;
import br.psi.giganet.api.purchase.config.exception.exception.IllegalArgumentException;
import br.psi.giganet.api.purchase.products.adapter.ProductAdapter;
import br.psi.giganet.api.purchase.products.categories.model.Category;
import br.psi.giganet.api.purchase.products.categories.service.CategoryService;
import br.psi.giganet.api.purchase.products.model.Product;
import br.psi.giganet.api.purchase.products.repository.ProductRepository;
import br.psi.giganet.api.purchase.units.service.UnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository products;

    @Autowired
    private ProductAdapter adapter;

    @Autowired
    private UnitService unitService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WebhooksHandlerService webHooksHandlerService;

    public Optional<Product> insert(Product product) {
        this.products.findByCode(product.getCode())
                .ifPresent(p -> {
                    throw new IllegalArgumentException("Este código já está sendo utilizado por outro produto");
                });

        product.setUnit(unitService
                .findById(product.getUnit().getId())
                .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada")));
        product.setCategory(categoryService
                .findById(product.getCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada")));

        return Optional.of(save(product));
    }

    public Optional<Product> update(Long id, Product product) {
        return this.findById(id)
                .map(saved -> {
                    saved.setUnit(unitService
                            .findById(product.getUnit().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada")));
                    saved.setCategory(categoryService
                            .findById(product.getCategory().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada")));
                    saved.setManufacturer(product.getManufacturer());
                    saved.setDescription(product.getDescription());
                    saved.setCode(product.getCode());
                    saved.setName(product.getName());

                    return save(saved);
                });
    }

    public Page<Product> findAll(int page, int size) {
        return this.products.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
    }

    public List<Product> findAll() {
        return this.products.findAll();
    }

    public Page<Product> findByNameAndCodeContaining(String name, String code, int page, int size) {
        return this.products.findByNameAndCode(name, code, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
    }

    public Page<Product> findByNameContaining(String name, int page, int size) {
        return this.products.findByNameContainingIgnoreCase(name, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
    }

    public Page<Product> findByCodeContaining(String code, int page, int size) {
        return this.products.findByCodeContainingIgnoreCase(code, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
    }

    public Optional<Product> findById(Long id) {
        return this.products.findById(id);
    }

    public Optional<Product> findByCode(String code) {
        return this.products.findByCode(code);
    }

    private Product save(Product product) {
        final Product saved = this.products.save(product);
        webHooksHandlerService.onSaveProduct(saved);
        return saved;
    }

    public Optional<String> getNextProductCodeByCategory(Category category) {
        Category found = categoryService.findById(category.getId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));

        String generatedCode = "";
        Product lastProduct = products.findByCategory(found, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "id")))
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    Product product = new Product();
                    product.setCode("0");

                    return product;
                });

        try {

            String pattern = found.getPattern();
            String prefix = pattern.replaceAll("X", "");
            String codePattern = pattern.replaceAll(prefix, "");
            int lastCode = Integer.parseInt(lastProduct.getCode().replaceFirst(prefix, ""));

            for (int maxAttempts = 0; maxAttempts < 10; maxAttempts++) {
                generatedCode = prefix + String.format("%0" + codePattern.length() + "d", (lastCode + 1));

                if (products.findByCode(generatedCode).isEmpty()) {
                    break;
                } else {
                    lastCode++;
                }
            }

            return Optional.of(generatedCode);
        } catch (Exception e) {
            return Optional.of("");
        }
    }
}
