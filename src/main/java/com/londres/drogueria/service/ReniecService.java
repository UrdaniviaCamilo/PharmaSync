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
public class ReniecService {


    @Value("${reniec.api.token:}")
    private String apiToken;

    @Autowired
    private RestTemplate restTemplate;

    public Map<String, String> consultarDni(String dni) {
        if (apiToken == null || apiToken.isEmpty()) {
            return simulateReniecResponse(dni);
        }

        try {
            String url = "https://api.apis.net.pe/v2/reniec/dni?numero=" + dni;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiToken);
            headers.set("Referer", "https://apis.net.pe/api-peru-dni");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ParameterizedTypeReference<Map<String, String>> typeRef = new ParameterizedTypeReference<>() {};
            ResponseEntity<Map<String, String>> response = restTemplate.exchange(url, HttpMethod.GET, entity, typeRef);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
            return simulateReniecResponse(dni);
        } catch (Exception e) {
            System.err.println("Error calling RENIEC API: " + e.getMessage());
            return simulateReniecResponse(dni);
        }
    }

    private Map<String, String> simulateReniecResponse(String dni) {
        Map<String, String> mock = new HashMap<>();
        mock.put("numeroDocumento", dni);
        if ("12345678".equals(dni)) {
            mock.put("nombres", "JUAN CARLOS");
            mock.put("apellidoPaterno", "PEREZ");
            mock.put("apellidoMaterno", "RODRIGUEZ");
        } else if ("87654321".equals(dni)) {
            mock.put("nombres", "MARIA ELENA");
            mock.put("apellidoPaterno", "GARCIA");
            mock.put("apellidoMaterno", "LOPEZ");
        } else {
            mock.put("nombres", "CIUDADANO");
            mock.put("apellidoPaterno", "VERIFICADO");
            mock.put("apellidoMaterno", "(MOCK)");
        }
        return mock;
    }
}
