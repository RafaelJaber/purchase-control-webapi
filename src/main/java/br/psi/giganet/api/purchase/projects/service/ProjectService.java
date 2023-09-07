package br.psi.giganet.api.purchase.projects.service;

import br.psi.giganet.api.purchase.projects.model.Project;
import br.psi.giganet.api.purchase.projects.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;


    public List<Project> findAll() {
        return projectRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }

    public Optional<Project> insert(Project project) {
        return Optional.of(projectRepository.save(project));
    }

    @Transactional
    public Optional<Project> update(Long id, Project project) {
        return projectRepository.findById(id)
                .map(saved -> {
                    saved.setName(project.getName());
                    saved.setDescription(project.getDescription());

                    return projectRepository.save(saved);
                });
    }

}
