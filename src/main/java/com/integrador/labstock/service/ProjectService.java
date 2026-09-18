package com.integrador.labstock.service;

import org.springframework.transaction.annotation.Transactional;
import com.integrador.labstock.dto.request.ProjectItemRequest;
import com.integrador.labstock.dto.request.ProjectRequest;
import com.integrador.labstock.dto.response.ProjectItemResponse;
import com.integrador.labstock.dto.response.ProjectResponse;
import com.integrador.labstock.entity.Item;
import com.integrador.labstock.entity.Project;
import com.integrador.labstock.entity.ProjectItem;
import com.integrador.labstock.exception.BusinessException;
import com.integrador.labstock.exception.ResourceNotFoundException;
import com.integrador.labstock.repository.ItemRepository;
import com.integrador.labstock.repository.LendingRepository;
import com.integrador.labstock.repository.ProjectItemRepository;
import com.integrador.labstock.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectItemRepository projectItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private LendingRepository lendingRepository;

    @Transactional
    public ProjectResponse create (ProjectRequest request) {

        Project project = new Project();
        project.setName(request.getName());

        projectRepository.save(project);

        return toProjectResponse(project);
    }

    public List<ProjectResponse> listAll (String search) {

        List<Project> projects;

        if (search != null && !search.isEmpty()) {
            projects = projectRepository.findBySearch(search);
        } else {
            projects = projectRepository.findByDeletedAtIsNull();
        }

        List<ProjectResponse> responses = new ArrayList<>();
        for (Project project : projects) {
            responses.add(toProjectResponse(project));
        }
        return responses;
    }

    public ProjectResponse findById (Long id) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));

        if (project.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Projeto não encontrado");
        }

        return toProjectResponse(project);
    }

    @Transactional
    public ProjectResponse update (Long id, ProjectRequest request) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));

        if (project.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Projeto não encontrado");
        }

        project.setName(request.getName());

        projectRepository.save(project);

        return toProjectResponse(project);
    }

    @Transactional
    public ProjectResponse inactivate (Long id) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));

        if (project.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Projeto não encontrado");
        }

        project.setIsInactive(!project.getIsInactive());

        projectRepository.save(project);

        return toProjectResponse(project);
    }

    @Transactional
    public void delete (Long id) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));

        if (project.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Projeto não encontrado");
        }

        project.setDeletedAt(LocalDateTime.now());
        projectRepository.save(project);
    }

    @Transactional
    public ProjectResponse addItem (Long projectId, ProjectItemRequest request) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));

        if (project.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Projeto não encontrado");
        }

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado"));

        if (item.getDeletedAt() != null || item.getIsInactive() == true) {
            throw new ResourceNotFoundException("Item não encontrado");
        }

        if (projectItemRepository.existsByProjectIdAndItemId(projectId, request.getItemId())) {
            throw new BusinessException("Item já está vinculado a este projeto");
        }

        ProjectItem projectItem = new ProjectItem();
        projectItem.setProject(project);
        projectItem.setItem(item);
        projectItem.setQuantityNeeded(request.getQuantityNeeded());

        projectItemRepository.save(projectItem);

        return toProjectResponse(project);
    }

    @Transactional
    public ProjectResponse removeItem (Long projectId, Long itemId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado"));

        if (project.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Projeto não encontrado");
        }

        ProjectItem projectItem = projectItemRepository.findByProjectIdAndItemId(projectId, itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item não está vinculado a este projeto"));

        projectItemRepository.delete(projectItem);

        return toProjectResponse(project);
    }

    private ProjectResponse toProjectResponse (Project project) {
        List<ProjectItem> projectItems = projectItemRepository.findByProjectIdAndDeletedAtIsNull(project.getId());

        List<ProjectItemResponse> itemResponses = new ArrayList<>();
        for (ProjectItem projectItem : projectItems) {
            Item item = projectItem.getItem();
            Integer lentQuantity = lendingRepository.sumLentQuantityByItemId(item.getId());
            Integer availableQuantity = item.getQuantity() - lentQuantity;

            ProjectItemResponse itemResponse = new ProjectItemResponse();
            itemResponse.setItemId(item.getId());
            itemResponse.setItemName(item.getName());
            itemResponse.setCategory(item.getCategory().name());
            itemResponse.setQuantityNeeded(projectItem.getQuantityNeeded());
            itemResponse.setAvailableQuantity(availableQuantity);
            itemResponses.add(itemResponse);
        }

        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setIsInactive(project.getIsInactive());
        response.setCreatedAt(project.getCreatedAt());
        response.setItems(itemResponses);
        return response;
    }
}
