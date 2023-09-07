package br.psi.giganet.api.purchase.projects.repository;

import br.psi.giganet.api.purchase.projects.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

}
