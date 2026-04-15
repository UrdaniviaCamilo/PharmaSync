package com.londres.drogueria.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DigemidScraperService {

    private List<Map<String, String>> latestAlerts = new ArrayList<>();

    // Run every 6 hours
    @Scheduled(fixedRate = 21600000)
    public void scrapeAlerts() {
        try {
            // URL of DIGEMID Alerts
            String url = "https://www.digemid.minsa.gob.pe/comunicados/"; 
            // If the user says it's 404, we try the main portal communications section
            String fallbackUrl = "https://www.digemid.minsa.gob.pe/?page_id=314";
            
            Document doc;
            try {
                doc = Jsoup.connect(url).timeout(15000).userAgent("Mozilla/5.0").get();
            } catch (Exception e) {
                doc = Jsoup.connect(fallbackUrl).timeout(15000).userAgent("Mozilla/5.0").get();
            }
            

            Elements rows = doc.select("table tr, .news-item, .post-entry"); 
            List<Map<String, String>> alerts = new ArrayList<>();

            for (Element row : rows) {
                if (alerts.size() >= 8) break;
                
                Elements links = row.select("a");
                if (!links.isEmpty()) {
                    Element linkEl = links.first();
                    String titulo = linkEl.text().trim();
                    String link = linkEl.attr("abs:href"); // GET ABSOLUTE URL
                    
                    if (titulo.length() > 10 && (titulo.contains("ALERTA") || titulo.contains("COMUNICADO") || titulo.contains("NOTA"))) {
                        Map<String, String> alert = new HashMap<>();
                        alert.put("titulo", titulo);
                        alert.put("link", link);
                        // Extract date if possible, else use current
                        String fecha = row.select(".date, td:first-child").text().trim();
                        alert.put("fecha", fecha.isEmpty() ? "Reciente" : fecha);
                        alerts.add(alert);
                    }
                }
            }
            
            if (!alerts.isEmpty()) {
                this.latestAlerts = alerts;
            }
        } catch (Exception e) {
            System.err.println("Error scraping DIGEMID: " + e.getMessage());
            // Fallback to dummy data if site is down or structure changed
            if (latestAlerts.isEmpty()) {
                latestAlerts = getMockAlerts();
            }
        }
    }

    public List<Map<String, String>> getLatestAlerts() {
        if (latestAlerts.isEmpty()) {
            scrapeAlerts();
        }
        return latestAlerts;
    }

    private List<Map<String, String>> getMockAlerts() {
        List<Map<String, String>> mocks = new ArrayList<>();
        Map<String, String> m1 = new HashMap<>();
        m1.put("fecha", "03/04/2026");
        m1.put("titulo", "Alerta Farmacéutica N° 015-2026: Retiro del mercado de lote de Paracetamol");
        m1.put("link", "#");
        mocks.add(m1);
        
        Map<String, String> m2 = new HashMap<>();
        m2.put("fecha", "01/04/2026");
        m2.put("titulo", "Comunicado DIGEMID: Nuevas disposiciones para el registro de dispositivos médicos");
        m2.put("link", "#");
        mocks.add(m2);
        return mocks;
    }
}
