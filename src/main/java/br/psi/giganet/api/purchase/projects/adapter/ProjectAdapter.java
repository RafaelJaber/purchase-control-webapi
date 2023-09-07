package br.psi.giganet.api.purchase.projects.adapter;

import br.psi.giganet.api.purchase.projects.controller.request.ProjectRequest;
import br.psi.giganet.api.purchase.projects.controller.response.ProjectResponse;
import br.psi.giganet.api.purchase.projects.model.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectAdapter {

    public Project create(Long id) {
        Project project = new Project();
        project.setId(id);

        return project;
    }

    public Project transform(ProjectRequest request) {
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        return project;
    }

    public ProjectResponse transform(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        return response;
    }

}
