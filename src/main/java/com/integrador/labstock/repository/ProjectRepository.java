package com.integrador.labstock.repository;

import com.integrador.labstock.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Barra de busca — filtra por nome
    @Query("SELECT p FROM Project p WHERE p.deletedAt IS NULL " +
           "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Project> findBySearch(@Param("search") String search);

    // Lista todos os projetos ativos
    List<Project> findByDeletedAtIsNull();
}
