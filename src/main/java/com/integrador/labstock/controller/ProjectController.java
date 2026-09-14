package com.integrador.labstock.controller;

import com.integrador.labstock.dto.request.ProjectItemRequest;
import com.integrador.labstock.dto.request.ProjectRequest;
import com.integrador.labstock.dto.response.ApiResponse;
import com.integrador.labstock.dto.response.ProjectResponse;
import com.integrador.labstock.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> create (@Valid @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Projeto criado com sucesso", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> listAll (@RequestParam(required = false) String search) {
        List<ProjectResponse> responses = projectService.listAll(search);
        return ResponseEntity.ok(ApiResponse.success("Projetos listados com sucesso", responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> findById (@PathVariable Long id) {
        ProjectResponse response = projectService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Projeto encontrado", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> update (@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Projeto atualizado com sucesso", response));
    }

    @PutMapping("/{id}/inactivate")
    public ResponseEntity<ApiResponse<ProjectResponse>> inactivate (@PathVariable Long id) {
        ProjectResponse response = projectService.inactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Status do projeto alterado", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete (@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Projeto removido com sucesso", null));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ApiResponse<ProjectResponse>> addItem (@PathVariable Long id, @Valid @RequestBody ProjectItemRequest request) {
        ProjectResponse response = projectService.addItem(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Item vinculado ao projeto", response));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> removeItem (@PathVariable Long id, @PathVariable Long itemId) {
        ProjectResponse response = projectService.removeItem(id, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removido do projeto", response));
    }
}
