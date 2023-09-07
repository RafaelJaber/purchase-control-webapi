package br.psi.giganet.api.purchase.projects.controller;

import br.psi.giganet.api.purchase.config.exception.exception.ResourceNotFoundException;
import br.psi.giganet.api.purchase.projects.adapter.ProjectAdapter;
import br.psi.giganet.api.purchase.projects.controller.request.ProjectRequest;
import br.psi.giganet.api.purchase.projects.controller.response.ProjectResponse;
import br.psi.giganet.api.purchase.projects.controller.security.RoleProjectsRead;
import br.psi.giganet.api.purchase.projects.controller.security.RoleProjectsWrite;
import br.psi.giganet.api.purchase.projects.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectAdapter projectAdapter;

    @GetMapping
    @RoleProjectsRead
    public List<ProjectResponse> findAll() {
        return projectService.findAll()
                .stream()
                .map(projectAdapter::transform)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @RoleProjectsRead
    public ProjectResponse findById(@PathVariable Long id) {
        return projectService.findById(id)
                .map(projectAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RoleProjectsWrite
    public ProjectResponse insert(@Valid @RequestBody ProjectRequest request) {
        return projectService.insert(projectAdapter.transform(request))
                .map(projectAdapter::transform)
                .orElseThrow(() -> new RuntimeException("Não foi possível cadastrar este projeto"));
    }

    @PutMapping("/{id}")
    @RoleProjectsWrite
    public ProjectResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request) {
        return projectService.update(id, projectAdapter.transform(request))
                .map(projectAdapter::transform)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));
    }
}
