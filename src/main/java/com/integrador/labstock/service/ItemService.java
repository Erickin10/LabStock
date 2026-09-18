package com.integrador.labstock.service;

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

    @Transactional
    public ItemResponse create (ItemRequest request) {

        Category category;
        try {
            category = Category.valueOf(request.getCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Categoria inválida");
        }

        Item item = new Item();
        item.setName(request.getName());
        item.setCategory(category);
        item.setQuantity(request.getQuantity());
        item.setMin_quantity(request.getMinQuantity());

        itemRepository.save(item);

        return toItemResponse(item);
    }

    public List<ItemResponse> listAll (String search) {

        List<Item> items;

        if (search != null && !search.isEmpty()) {
            items = itemRepository.findBySearch(search);
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

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado"));

        if (item.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Item não encontrado");
        }

        Category category;
        try {
            category = Category.valueOf(request.getCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Categoria inválida");
        }

        item.setName(request.getName());
        item.setCategory(category);
        item.setQuantity(request.getQuantity());
        item.setMin_quantity(request.getMinQuantity());

        itemRepository.save(item);

        return toItemResponse(item);
    }

    @Transactional
    public ItemResponse inactivate (Long id) {

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado"));

        if (item.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Item não encontrado");
        }

        item.setIsInactive(!item.getIsInactive());

        itemRepository.save(item);

        return toItemResponse(item);
    }

    @Transactional
    public void delete (Long id) {

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado"));

        if (item.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Item não encontrado");
        }

        item.setDeletedAt(LocalDateTime.now());
        itemRepository.save(item);
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
        response.setMinQuantity(item.getMin_quantity());
        response.setIsInactive(item.getIsInactive());
        return response;
    }
}
