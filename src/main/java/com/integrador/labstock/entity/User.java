package com.integrador.labstock.entity;

import com.integrador.labstock.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    // @Column(nullable = false) = NOT NULL no banco
    @Column(nullable = false)
    private String name;

    // unique = true cria um indice UNIQUE no banco — nao permite emails repetidos
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    // @Enumerated(EnumType.STRING) salva o enum como texto no banco ("STUDENT", "TEACHER", "LAB")
    // Sem isso, o JPA salvaria como numero (0, 1, 2) — o que e confuso de ler no banco
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.STUDENT;
}
