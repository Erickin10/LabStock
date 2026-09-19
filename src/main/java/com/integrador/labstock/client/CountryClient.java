package com.integrador.labstock.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "countryClient",
        url = "https://api.restcountries.com/countries/v5"
)
public interface CountryClient {

    @GetMapping("/names.common/{name}")
    Object getCountryByName(
            @PathVariable String name,
            @RequestHeader("Authorization") String authorization
    );

}