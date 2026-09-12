package com.integrador.labstock.repository;

import com.integrador.labstock.entity.Damaged;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DamagedRepository extends JpaRepository<Damaged, Long> {

    // Lista todos os registros de dano ativos
    List<Damaged> findByDeletedAtIsNull();
}
