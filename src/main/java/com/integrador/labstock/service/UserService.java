package com.integrador.labstock.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import com.integrador.labstock.dto.request.LoginRequest;
import com.integrador.labstock.dto.request.RegisterRequest;
import com.integrador.labstock.dto.request.UpdateProfileRequest;
import com.integrador.labstock.dto.request.UpdateRoleRequest;
import com.integrador.labstock.dto.response.LoginResponse;
import com.integrador.labstock.dto.response.UserResponse;
import com.integrador.labstock.entity.User;
import com.integrador.labstock.enums.Role;
import com.integrador.labstock.exception.BusinessException;
import com.integrador.labstock.exception.ResourceNotFoundException;
import com.integrador.labstock.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Transactional
    public LoginResponse register (RegisterRequest request) {

        logger.info("Registrando novo usuário, email={}", request.getEmail());

        // Verifica se o email ja existe no banco
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new BusinessException("Email já cadastrado");
        }

        // Cria o usuario com os dados do request
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(Role.STUDENT); // cadastro sempre cria como STUDENT

        // Salva no banco
        userRepository.save(user);

        logger.info("Usuário registrado com sucesso, id={}", user.getId());

        // Monta e retorna o response
        LoginResponse response = new LoginResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name()); // converte o enum pra String

        return response;
    }

    public LoginResponse login (LoginRequest request) {

        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() ->
                        new BusinessException("Email ou senha inválidos"));
                        logger.warn("Login recusado: email={} não encontrado", request.getEmail());

        // Compara a senha
        if (!user.getPassword().equals(request.getPassword())) {
            logger.warn("Login recusado: senha invalida para email={}", request.getEmail());
            throw new BusinessException("Email ou senha inválidos");
        }

        logger.info("Login realizado com sucesso, id={}", user.getId());

        LoginResponse response = new LoginResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());

        return response;
    }

    public List<UserResponse> listAll (String search) {

        List<User> users;

        if (search != null && !search.isEmpty()) {
            users = userRepository.findBySearch(search);
        } else {
            users = userRepository.findByDeletedAtIsNull();
        }

        List<UserResponse> responses = new ArrayList<>();
        for (User user : users) {
            responses.add(toUserResponse(user));
        }
        return responses;
    }

    public UserResponse findById (Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (user.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }

        return toUserResponse(user);
    }

    @Transactional
    public UserResponse updateRole (Long id, UpdateRoleRequest request) {

        logger.info("Atualizando role do usuario id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (user.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }

        try {
            Role role = Role.valueOf(request.getRole().toUpperCase());
            user.setRole(role);
        } catch (IllegalArgumentException e) {
            logger.warn("Atualização de role recusada: role inválida={}", request.getRole());
            throw new BusinessException("Role inválida");
        }

        userRepository.save(user);

        logger.info("Role do usuario id={} atualizada com sucesso", id);

        return toUserResponse(user);
    }

    @Transactional
    public UserResponse updateProfile (Long id, UpdateProfileRequest request) {

        logger.info("Atualizando perfil do usuario id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (user.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }

        if (request.getName() != null && !request.getName().isEmpty()) {
            user.setName(request.getName());
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (!request.getEmail().equals(user.getEmail())
                    && userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
                logger.warn("Atualizacao de perfil recusada: email ja em uso para usuario id={}", id);
                throw new BusinessException("Email já está em uso");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(request.getPassword());
        }

        userRepository.save(user);

        logger.info("Perfil do usuario id={} atualizado com sucesso", id);

        return toUserResponse(user);
    }

    @Transactional
    public void delete (Long id) {

        logger.info("Excluindo usuario id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (user.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Usuário não encontrado");
        }

        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);

        logger.info("Usuario id={} excluido com sucesso", id);
    }

    private UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
