package com.integrador.labstock.service;

import com.integrador.labstock.dto.request.LendingRequest;
import com.integrador.labstock.dto.response.LendingResponse;
import com.integrador.labstock.entity.Item;
import com.integrador.labstock.entity.Lending;
import com.integrador.labstock.entity.User;
import com.integrador.labstock.enums.LendingStatus;
import com.integrador.labstock.exception.BusinessException;
import com.integrador.labstock.exception.ResourceNotFoundException;
import com.integrador.labstock.repository.ItemRepository;
import com.integrador.labstock.repository.LendingRepository;
import com.integrador.labstock.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LendingService {

    @Autowired
    private LendingRepository lendingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    public LendingResponse create (LendingRequest request) {

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado"));

        if (item.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Item não encontrado");
        }

        Integer lentQuantity = lendingRepository.sumLentQuantityByItemId(item.getId());
        Integer availableQuantity = item.getQuantity() - lentQuantity;

        if (request.getQuantity() > availableQuantity) {
            throw new BusinessException("Estoque insuficiente. Disponível: " + availableQuantity);
        }

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado"));

        if (student.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Aluno não encontrado");
        }

        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Professor não encontrado"));

        if (teacher.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Professor não encontrado");
        }

        Lending lending = new Lending();
        lending.setItem(item);
        lending.setStudent(student);
        lending.setTeacher(teacher);
        lending.setQuantity(request.getQuantity());

        lendingRepository.save(lending);

        return toLendingResponse(lending);
    }

    public List<LendingResponse> listAll () {

        List<Lending> lendings = lendingRepository.findByDeletedAtIsNull();

        List<LendingResponse> responses = new ArrayList<>();
        for (Lending lending : lendings) {
            responses.add(toLendingResponse(lending));
        }
        return responses;
    }

    public List<LendingResponse> listByStatus (String status) {

        LendingStatus lendingStatus;
        try {
            lendingStatus = LendingStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Status inválido");
        }

        List<Lending> lendings = lendingRepository.findByStatusAndDeletedAtIsNull(lendingStatus);

        List<LendingResponse> responses = new ArrayList<>();
        for (Lending lending : lendings) {
            responses.add(toLendingResponse(lending));
        }
        return responses;
    }

    public List<LendingResponse> listByStudent (Long studentId) {

        List<Lending> lendings = lendingRepository.findByStudentIdAndDeletedAtIsNull(studentId);

        List<LendingResponse> responses = new ArrayList<>();
        for (Lending lending : lendings) {
            responses.add(toLendingResponse(lending));
        }
        return responses;
    }

    public LendingResponse approve (Long id) {

        Lending lending = lendingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empréstimo não encontrado"));

        if (lending.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Empréstimo não encontrado");
        }

        if (lending.getStatus() != LendingStatus.PENDING) {
            throw new BusinessException("Apenas empréstimos pendentes podem ser aprovados");
        }

        lending.setStatus(LendingStatus.APPROVED);
        lending.setLendingDate(LocalDateTime.now());

        lendingRepository.save(lending);

        return toLendingResponse(lending);
    }

    public LendingResponse reject (Long id) {

        Lending lending = lendingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empréstimo não encontrado"));

        if (lending.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Empréstimo não encontrado");
        }

        if (lending.getStatus() != LendingStatus.PENDING) {
            throw new BusinessException("Apenas empréstimos pendentes podem ser rejeitados");
        }

        lending.setStatus(LendingStatus.REJECTED);

        lendingRepository.save(lending);

        return toLendingResponse(lending);
    }

    public LendingResponse returnItem (Long id) {

        Lending lending = lendingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empréstimo não encontrado"));

        if (lending.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Empréstimo não encontrado");
        }

        if (lending.getStatus() != LendingStatus.APPROVED) {
            throw new BusinessException("Apenas empréstimos aprovados podem ser devolvidos");
        }

        if (lending.getReturned()) {
            throw new BusinessException("Empréstimo já foi devolvido");
        }

        lending.setReturned(true);
        lending.setReturnDate(LocalDateTime.now());

        lendingRepository.save(lending);

        return toLendingResponse(lending);
    }

    private LendingResponse toLendingResponse(Lending lending) {
        LendingResponse response = new LendingResponse();
        response.setId(lending.getId());
        response.setItemId(lending.getItem().getId());
        response.setItemName(lending.getItem().getName());
        response.setStudentId(lending.getStudent().getId());
        response.setStudentName(lending.getStudent().getName());
        response.setTeacherId(lending.getTeacher().getId());
        response.setTeacherName(lending.getTeacher().getName());
        response.setQuantity(lending.getQuantity());
        response.setStatus(lending.getStatus().name());
        response.setReturned(lending.getReturned());
        response.setLendingDate(lending.getLendingDate());
        response.setReturnDate(lending.getReturnDate());
        response.setCreatedAt(lending.getCreatedAt());
        return response;
    }
}
