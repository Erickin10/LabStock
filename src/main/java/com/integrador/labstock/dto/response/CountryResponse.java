package com.integrador.labstock.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CountryResponse {

    private String name;
    private String capital;
    private String region;
    private Long population;
}
