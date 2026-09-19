package com.integrador.labstock.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import com.integrador.labstock.dto.request.DamagedRequest;
import com.integrador.labstock.dto.response.DamagedResponse;
import com.integrador.labstock.entity.Damaged;
import com.integrador.labstock.entity.Item;
import com.integrador.labstock.entity.User;
import com.integrador.labstock.exception.BusinessException;
import com.integrador.labstock.exception.ResourceNotFoundException;
import com.integrador.labstock.repository.DamagedRepository;
import com.integrador.labstock.repository.ItemRepository;
import com.integrador.labstock.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DamagedService {

    @Autowired
    private DamagedRepository damagedRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(DamagedService.class);

    @Transactional
    public DamagedResponse create (DamagedRequest request) {

        logger.info("Registrando dano para o item id={}", request.getItemId());

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado"));

        if (item.getDeletedAt() != null || item.getIsInactive()) {
            throw new ResourceNotFoundException("Item não encontrado");
        }

        if (request.getQuantity() > item.getQuantity()) {
            logger.warn("Registro de dano recusado: quantidade maior que o estoque do item id={}", item.getId());
            throw new BusinessException("Quantidade danificada maior que o estoque total");
        }

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado"));

        if (student.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Aluno não encontrado");
        }

        User lab = userRepository.findById(request.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Laboratorista não encontrado"));

        if (lab.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Laboratorista não encontrado");
        }

        item.setQuantity(item.getQuantity() - request.getQuantity());
        itemRepository.save(item);

        logger.debug("Estoque do item id={} atualizado apos dano, nova quantidade={}", item.getId(), item.getQuantity());

        Damaged damaged = new Damaged();
        damaged.setItem(item);
        damaged.setStudent(student);
        damaged.setLab(lab);
        damaged.setReason(request.getReason());
        damaged.setQuantity(request.getQuantity());

        damagedRepository.save(damaged);

        return toDamagedResponse(damaged);
    }

    public List<DamagedResponse> listAll () {

        List<Damaged> damageds = damagedRepository.findByDeletedAtIsNull();

        List<DamagedResponse> responses = new ArrayList<>();
        for (Damaged damaged : damageds) {
            responses.add(toDamagedResponse(damaged));
        }
        return responses;
    }

    public DamagedResponse findById (Long id) {

        Damaged damaged = damagedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de dano não encontrado"));

        if (damaged.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Registro de dano não encontrado");
        }

        return toDamagedResponse(damaged);
    }

    private DamagedResponse toDamagedResponse(Damaged damaged) {
        DamagedResponse response = new DamagedResponse();
        response.setId(damaged.getId());
        response.setItemId(damaged.getItem().getId());
        response.setItemName(damaged.getItem().getName());
        response.setStudentId(damaged.getStudent().getId());
        response.setStudentName(damaged.getStudent().getName());
        response.setLabId(damaged.getLab().getId());
        response.setLabName(damaged.getLab().getName());
        response.setReason(damaged.getReason());
        response.setQuantity(damaged.getQuantity());
        response.setCreatedAt(damaged.getCreatedAt());
        return response;
    }
}
