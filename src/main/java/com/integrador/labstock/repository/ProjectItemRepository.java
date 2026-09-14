package com.integrador.labstock.repository;

import com.integrador.labstock.entity.ProjectItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectItemRepository extends JpaRepository<ProjectItem, Long> {

    // Lista todos os itens vinculados a um projeto
    List<ProjectItem> findByProjectIdAndDeletedAtIsNull(Long projectId);

    // Busca vinculo especifico (para remover item do projeto)
    Optional<ProjectItem> findByProjectIdAndItemId(Long projectId, Long itemId);

    // Verifica se item ja esta vinculado ao projeto
    boolean existsByProjectIdAndItemId(Long projectId, Long itemId);
}
