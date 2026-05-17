package cl.innovatech.projects_service.controller;

import cl.innovatech.projects_service.dto.ProjectRequest;
import cl.innovatech.projects_service.dto.ProjectResponse;
import cl.innovatech.projects_service.entity.Project;
import cl.innovatech.projects_service.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ProjectResponse create(@Valid @RequestBody ProjectRequest request) {
        return projectService.create(request);
    }

    @GetMapping
    public List<ProjectResponse> findAll(
            @RequestParam(required = false) Project.Estado estado,
            @RequestParam(required = false) Long responsableId
    ) {
        if (estado != null) {
            return projectService.findByEstado(estado);
        }

        if (responsableId != null) {
            return projectService.findByResponsableId(responsableId);
        }

        return projectService.findAll();
    }

    @GetMapping("/{id}")
    public ProjectResponse findById(@PathVariable Long id) {
        return projectService.findById(id);
    }

    @PutMapping("/{id}")
    public ProjectResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request
    ) {
        return projectService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        projectService.delete(id);
    }

    @PatchMapping("/{id}/status")
    public ProjectResponse changeStatus(
            @PathVariable Long id,
            @RequestParam Project.Estado estado
    ) {
        return projectService.changeStatus(id, estado);
    }

    @PatchMapping("/{id}/close")
    public ProjectResponse close(@PathVariable Long id) {
        return projectService.close(id);
    }
}