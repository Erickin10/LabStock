package com.integrador.labstock.service;

import com.integrador.labstock.client.CountryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CountryService {

    @Autowired
    private CountryClient countryClient;

    public Object getCountryByName(String name) {
        return countryClient.getCountryByName(name, "Bearer rc_live_demo");
    }
}