package br.psi.giganet.api.purchase.products.repository;

import br.psi.giganet.api.purchase.products.categories.model.Category;
import br.psi.giganet.api.purchase.products.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String name);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByCodeContainingIgnoreCase(String code, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE UPPER(p.name) LIKE CONCAT('%', UPPER(:name), '%') AND p.code LIKE CONCAT('%', :code ,'%')")
    Page<Product> findByNameAndCode(String name, String code, Pageable pageable);

    Optional<Product> findByCode(String code);

    List<Product> findByCategory(Category category, Pageable pageable);
}
