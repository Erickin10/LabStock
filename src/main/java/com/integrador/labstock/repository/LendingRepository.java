package com.integrador.labstock.repository;

import com.integrador.labstock.entity.Lending;
import com.integrador.labstock.enums.LendingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LendingRepository extends JpaRepository<Lending, Long> {

    // Lista todos os emprestimos ativos
    List<Lending> findByDeletedAtIsNull();

    // Filtra por status (PENDING, APPROVED, REJECTED)
    List<Lending> findByStatusAndDeletedAtIsNull(LendingStatus status);

    // Lista emprestimos de um aluno especifico
    List<Lending> findByStudentIdAndDeletedAtIsNull(Long studentId);

    // Busca emprestimos ativos (aprovados e nao devolvidos) de um item
    List<Lending> findByItemIdAndStatusAndReturnedFalseAndDeletedAtIsNull(Long itemId, LendingStatus status);

    // Soma a quantidade emprestada de um item (para calcular estoque disponivel)
    @Query("SELECT COALESCE(SUM(l.quantity), 0) FROM Lending l " +
           "WHERE l.item.id = :itemId AND l.status = 'APPROVED' " +
           "AND l.returned = false AND l.deletedAt IS NULL")
    Integer sumLentQuantityByItemId(@Param("itemId") Long itemId);

    // Verifica se existe emprestimo ativo para um item
    boolean existsByItemIdAndStatusAndReturnedFalseAndDeletedAtIsNull(Long itemId, LendingStatus status);
}
