package com.integrador.labstock.repository;

import com.integrador.labstock.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // Barra de busca unica — procura no nome OU na categoria
    @Query("SELECT i FROM Item i WHERE i.deletedAt IS NULL " +
           "AND (LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(CAST(i.category AS string)) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Item> findBySearch(@Param("search") String search);

    // Lista todos os itens ativos — usado pelo cron de purchase
    List<Item> findByDeletedAtIsNull();
}
