package com.londres.drogueria.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MercadoPagoService {

    @Value("${mercadopago.access.token:}")
    private String accessToken;

    public String createPreference(String title, Double price, Integer quantity) {
        if (accessToken == null || accessToken.isEmpty()) {
            return "https://www.mercadopago.com.pe/sandbox"; // Mock link
        }

        try {
            MercadoPagoConfig.setAccessToken(accessToken);

            PreferenceClient client = new PreferenceClient();

            List<PreferenceItemRequest> items = new ArrayList<>();
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title(title)
                    .quantity(quantity)
                    .unitPrice(new BigDecimal(price))
                    .build();
            items.add(item);

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(PreferenceBackUrlsRequest.builder()
                            .success("http://localhost:8080/marketplace/success")
                            .failure("http://localhost:8080/marketplace/failure")
                            .pending("http://localhost:8080/marketplace/pending")
                            .build())
                    .autoReturn("approved")
                    .build();

            Preference preference = client.create(request);
            return preference.getInitPoint();
        } catch (Exception e) {
            System.err.println("Error creating Mercado Pago preference: " + e.getMessage());
            return "https://www.mercadopago.com.pe/sandbox";
        }
    }
}
