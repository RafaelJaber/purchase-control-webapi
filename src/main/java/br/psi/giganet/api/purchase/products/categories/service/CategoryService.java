package br.psi.giganet.api.purchase.products.categories.service;

import br.psi.giganet.api.purchase.common.webhooks.services.WebhooksHandlerService;
import br.psi.giganet.api.purchase.products.categories.model.Category;
import br.psi.giganet.api.purchase.products.categories.repository.ProductCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private ProductCategoryRepository categoryRepository;

    @Autowired
    private WebhooksHandlerService webhooksHandlerService;

    public List<Category> findAll() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    public Optional<Category> insert(Category category) {
        return Optional.of(save(category));
    }

    @Transactional
    public Optional<Category> update(Long id, Category category) {
        return categoryRepository.findById(id)
                .map(saved -> {
                    saved.setName(category.getName());
                    saved.setPattern(category.getPattern());
                    saved.setDescription(category.getDescription());

                    return save(saved);
                });
    }

    private Category save(Category category){
        final Category saved = categoryRepository.save(category);
        webhooksHandlerService.onSaveCategory(saved);

        return saved;
    }
}
