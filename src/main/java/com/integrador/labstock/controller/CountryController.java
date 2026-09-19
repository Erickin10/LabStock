package com.integrador.labstock.controller;

import com.integrador.labstock.dto.response.ApiResponse;
import com.integrador.labstock.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/countries")
public class CountryController {

    @Autowired
    private CountryService countryService;

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getCountry(@RequestParam String name) {
        Object country = countryService.getCountryByName(name);
        return ResponseEntity.ok(ApiResponse.success("País encontrado", country));
    }
}