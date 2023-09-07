package br.psi.giganet.api.purchase.products.categories.adapter;

import br.psi.giganet.api.purchase.products.categories.controller.request.CategoryRequest;
import br.psi.giganet.api.purchase.products.categories.controller.response.CategoryResponse;
import br.psi.giganet.api.purchase.products.categories.controller.response.CategoryResponseWithPrefix;
import br.psi.giganet.api.purchase.products.categories.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryAdapter {

    public Category create(Long id) {
        Category category = new Category();
        category.setId(id);

        return category;
    }

    public Category transform(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setPattern(request.getPattern());
        category.setDescription(request.getDescription());
        return category;
    }

    public CategoryResponse transform(Category category) {
        return transform(category, ResponseType.RESPONSE);
    }

    public CategoryResponse transform(Category category, ResponseType type) {
        CategoryResponse response;
        switch (type) {
            case RESPONSE:
                response = new CategoryResponse();
                response.setId(category.getId());
                response.setName(category.getName());
                response.setDescription(category.getDescription());
                return response;

            case RESPONSE_WITH_PREFIX:
                response = new CategoryResponseWithPrefix();
                response.setId(category.getId());
                response.setName(category.getName());
                ((CategoryResponseWithPrefix) response).setPattern(category.getPattern());
                response.setDescription(category.getDescription());
                return response;

            default:
                return null;
        }
    }

    public enum ResponseType {
        RESPONSE,
        RESPONSE_WITH_PREFIX
    }

}
