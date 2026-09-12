package com.integrador.labstock.repository;

import com.integrador.labstock.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Busca usuario por email (para login)
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    // Barra de busca unica — procura o mesmo texto no nome OU no email OU na role
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL " +
           "AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))" +
           "OR LOWER(u.role) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> findBySearch(@Param("search") String search);

    // Lista todos os usuarios ativos
    List<User> findByDeletedAtIsNull();

    // Verifica se email ja existe (para validar cadastro)
    boolean existsByEmailAndDeletedAtIsNull(String email);
}
