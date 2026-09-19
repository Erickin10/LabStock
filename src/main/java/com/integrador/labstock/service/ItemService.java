package com.integrador.labstock.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import com.integrador.labstock.dto.request.ItemRequest;
import com.integrador.labstock.dto.response.ItemResponse;
import com.integrador.labstock.entity.Item;
import com.integrador.labstock.enums.Category;
import com.integrador.labstock.exception.BusinessException;
import com.integrador.labstock.exception.ResourceNotFoundException;
import com.integrador.labstock.repository.ItemRepository;
import com.integrador.labstock.repository.LendingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private LendingRepository lendingRepository;

    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);

    @Transactional
    public ItemResponse create (ItemRequest request) {

        logger.info("Criando item com nome={}", request.getName());

        Category category;
        try {
            category = Category.valueOf(request.getCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Criacao de item recusada: categoria invalida={}", request.getCategory());
            throw new BusinessException("Categoria inválida");
        }

        Item item = new Item();
        item.setName(request.getName());
        item.setCategory(category);
        item.setQuantity(request.getQuantity());
        item.setMinQuantity(request.getMinQuantity());

        itemRepository.save(item);

        logger.info("Item criado com sucesso, id={}", item.getId());

        return toItemResponse(item);
    }

    public List<ItemResponse> listAll (String name, String category) {

        logger.debug("Buscando itens: nome={}, categoria={}", name, category);

        List<Item> items;

        if (name != null && !name.isEmpty() && category != null && !category.isEmpty()) {
            items = itemRepository.findBySearchAndCategory(name, category);
        } else if (name != null && !name.isEmpty()) {
            items = itemRepository.findBySearch(name);
        } else if (category != null && !category.isEmpty()) {
            items = itemRepository.findByCategory(category);
        } else {
            items = itemRepository.findByDeletedAtIsNull();
        }

        List<ItemResponse> responses = new ArrayList<>();
        for (Item item : items) {
            responses.add(toItemResponse(item));
        }
        return responses;
    }

    public ItemResponse findById (Long id) {

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado"));

        if (item.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Item não encontrado");
        }

        return toItemResponse(item);
    }

    @Transactional
    public ItemResponse update (Long id, ItemRequest request) {

        logger.info("Atualizando item id={}", id);

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado"));

        if (item.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Item não encontrado");
        }

        Category category;
        try {
            category = Category.valueOf(request.getCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Atualização recusada: categoria inválida={}", request.getCategory());
            throw new BusinessException("Categoria inválida");
        }

        item.setName(request.getName());
        item.setCategory(category);
        item.setQuantity(request.getQuantity());
        item.setMinQuantity(request.getMinQuantity());

        itemRepository.save(item);

        logger.info("Item id={} atualizado com sucesso", id);

        return toItemResponse(item);
    }

    @Transactional
    public ItemResponse inactivate (Long id) {

        logger.info("Alternando status ativo para inativo do item id={}", id);

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado"));

        if (item.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Item não encontrado");
        }

        item.setIsInactive(!item.getIsInactive());

        itemRepository.save(item);

        logger.info("Item id={} agora esta com isInactive={}", id, item.getIsInactive());

        return toItemResponse(item);
    }

    @Transactional
    public void delete (Long id) {

        logger.info("Excluindo item id={}", id);

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado"));

        if (item.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Item não encontrado");
        }

        item.setDeletedAt(LocalDateTime.now());
        itemRepository.save(item);

        logger.info("Item id={} excluído com sucesso", id);
    }

    private ItemResponse toItemResponse(Item item) {
        Integer lentQuantity = lendingRepository.sumLentQuantityByItemId(item.getId());
        Integer availableQuantity = item.getQuantity() - lentQuantity;

        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setName(item.getName());
        response.setCategory(item.getCategory().name());
        response.setQuantity(item.getQuantity());
        response.setAvailableQuantity(availableQuantity);
        response.setMinQuantity(item.getMinQuantity());
        response.setIsInactive(item.getIsInactive());
        return response;
    }
}
