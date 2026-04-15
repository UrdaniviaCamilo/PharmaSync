package com.londres.drogueria.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.HashMap;
import org.springframework.core.ParameterizedTypeReference;

@Service
public class SunatService {

    @Value("${sunat.api.token:}")
    private String apiToken;

    @Autowired
    private RestTemplate restTemplate;

    public Map<String, String> consultarRuc(String ruc) {
        if (apiToken == null || apiToken.isEmpty()) {
            return simulateSunatResponse(ruc);
        }

        try {
            String url = "https://api.apis.net.pe/v2/sunat/ruc?numero=" + ruc;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiToken);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ParameterizedTypeReference<Map<String, String>> typeRef = new ParameterizedTypeReference<>() {};
            ResponseEntity<Map<String, String>> response = restTemplate.exchange(url, HttpMethod.GET, entity, typeRef);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return simulateSunatResponse(ruc);
        } catch (Exception e) {
            System.err.println("Error calling SUNAT API: " + e.getMessage());
            return simulateSunatResponse(ruc);
        }
    }

    private Map<String, String> simulateSunatResponse(String ruc) {
        Map<String, String> mock = new HashMap<>();
        mock.put("numeroDocumento", ruc);
        if ("20100000001".equals(ruc)) {
            mock.put("razonSocial", "FARMACIA UNIVERSAL S.A.C.");
            mock.put("nombreComercial", "Farmacia Universal");
            mock.put("direccion", "AV. ABANCAY 123, LIMA");
            mock.put("departamento", "LIMA");
            mock.put("provincia", "LIMA");
            mock.put("distrito", "LIMA");
        } else {
            mock.put("razonSocial", "EMPRESA CONSULTADA (MOCK)");
            mock.put("nombreComercial", "PHARMANET NEGOCIO");
            mock.put("direccion", "AV. SIEMPRE VIVA 742");
            mock.put("departamento", "LIMA");
            mock.put("provincia", "LIMA");
            mock.put("distrito", "MIRAFLORES");
        }
        return mock;
    }
}
