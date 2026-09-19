package com.integrador.labstock.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
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
@Transactional(readOnly = true)
public class LendingService {

    @Autowired
    private LendingRepository lendingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(LendingService.class);


    @Transactional
    public LendingResponse create (LendingRequest request) {

        logger.info("Iniciando criacaoo de emprestimo para o item id={}", request.getItemId());

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado"));

        if (item.getDeletedAt() != null || item.getIsInactive() == true) {
            throw new ResourceNotFoundException("Item não encontrado");
        }

        Integer lentQuantity = lendingRepository.sumLentQuantityByItemId(item.getId());
        Integer availableQuantity = item.getQuantity() - lentQuantity;

        if (request.getQuantity() > availableQuantity) {
            logger.warn("Estoque insuficiente para criacao de emprestimo. Quantidade solicitada={}, Quantidade disponível={}", request.getQuantity(), availableQuantity);
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

        logger.info("Empréstimo criado com sucesso, id={}", lending.getId());

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

    @Transactional
    public LendingResponse approve (Long id) {

        logger.info("Aprovando emprestimo id={}", id);

        Lending lending = lendingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empréstimo não encontrado"));

        if (lending.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Empréstimo não encontrado");
        }

        if (lending.getStatus() != LendingStatus.PENDING) {
            logger.warn("Aprovacao recusada: emprestimo id={} nao esta pendente", id);
            throw new BusinessException("Apenas empréstimos pendentes podem ser aprovados");
        }

        lending.setStatus(LendingStatus.APPROVED);
        lending.setLendingDate(LocalDateTime.now());

        lendingRepository.save(lending);

        logger.info("Emprestimo id={} aprovado com sucesso", id);

        return toLendingResponse(lending);
    }

    @Transactional
    public LendingResponse reject (Long id) {

        logger.info("Rejeitando emprestimo id={}", id);

        Lending lending = lendingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empréstimo não encontrado"));

        if (lending.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Empréstimo não encontrado");
        }

        if (lending.getStatus() != LendingStatus.PENDING) {
            logger.warn("Rejeicao recusada: emprestimo id={} não está pendente", id);
            throw new BusinessException("Apenas empréstimos pendentes podem ser rejeitados");
        }

        lending.setStatus(LendingStatus.REJECTED);

        logger.info("Emprestimo id={} rejeitado com sucesso", id);

        lendingRepository.save(lending);

        return toLendingResponse(lending);
    }

    @Transactional
    public LendingResponse returnItem (Long id) {

        logger.info("Registrando devolucao do emprestimo id={}", id);

        Lending lending = lendingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empréstimo não encontrado"));

        if (lending.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Empréstimo não encontrado");
        }

        if (lending.getStatus() != LendingStatus.APPROVED) {
            logger.warn("Devolucao recusada: emprestimo id={} nao esta aprovado", id);
            throw new BusinessException("Apenas empréstimos aprovados podem ser devolvidos");
        }

        if (lending.getReturned()) {
            logger.warn("Devolucao recusada: emprestimo id={} ja foi devolvido", id);
            throw new BusinessException("Empréstimo já foi devolvido");
        }

        lending.setReturned(true);
        lending.setReturnDate(LocalDateTime.now());

        lendingRepository.save(lending);

        logger.info("Empréstimo id={} devolvido com sucesso", id);

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
