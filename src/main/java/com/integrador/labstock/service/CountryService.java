package com.integrador.labstock.service;

import com.integrador.labstock.client.CountryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CountryService {

    @Autowired
    private CountryClient countryClient;

    @Value("${restcountries.api.token}")
    private String apiToken;

    private static final Logger logger = LoggerFactory.getLogger(CountryService.class);

    public Object getCountryByName(String name) {
        logger.info("Buscando pais por nome={}", name);
        return countryClient.getCountryByName(name, "Bearer " + apiToken);
    }
}