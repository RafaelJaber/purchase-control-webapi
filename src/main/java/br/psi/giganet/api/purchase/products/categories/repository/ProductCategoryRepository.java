package br.psi.giganet.api.purchase.products.categories.repository;

import br.psi.giganet.api.purchase.products.categories.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<Category, Long> {

}
