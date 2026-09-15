package com.integrador.labstock.controller;

import com.integrador.labstock.dto.request.UpdateProfileRequest;
import com.integrador.labstock.dto.request.UpdateRoleRequest;
import com.integrador.labstock.dto.response.ApiResponse;
import com.integrador.labstock.dto.response.UserResponse;
import com.integrador.labstock.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> listAll (@RequestParam(required = false) String search) {
        List<UserResponse> responses = userService.listAll(search);
        return ResponseEntity.ok(ApiResponse.success("Usuários listados com sucesso", responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> findById (@PathVariable Long id) {
        UserResponse response = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Usuário encontrado", response));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateRole (@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        UserResponse response = userService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.success("Role atualizada com sucesso", response));
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile (@PathVariable Long id, @RequestBody UpdateProfileRequest request) {
        UserResponse response = userService.updateProfile(id, request);
        return ResponseEntity.ok(ApiResponse.success("Perfil atualizado com sucesso", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete (@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Usuário removido com sucesso", null));
    }
}
