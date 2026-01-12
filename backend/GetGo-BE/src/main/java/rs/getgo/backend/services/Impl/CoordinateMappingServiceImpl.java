package rs.getgo.backend.services.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import rs.getgo.backend.services.CoordinateMappingService;

import java.util.Optional;

@Service
public class CoordinateMappingServiceImpl implements CoordinateMappingService {

    private static final Logger log = LoggerFactory.getLogger(CoordinateMappingServiceImpl.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Optional<double[]> call(String url, String mode) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "GetGo-App/1.0 (contact: dalenikolic03@gmail.com)");
            headers.set("Referer", "https://localhost:4200");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            log.info("Calling Nominatim ({})", mode);
            log.debug("URL={}", url);

            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());

            if (!root.isArray() || root.isEmpty()) {
                log.warn("Nominatim returned no results ({})", mode);
                return Optional.empty();
            }

            JsonNode first = root.get(0);
            double lat = first.path("lat").asDouble();
            double lon = first.path("lon").asDouble();

            log.info("Nominatim OK ({}) -> ({}, {})", mode, lat, lon);

            return Optional.of(new double[]{lat, lon});

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 403) {
                // concise warning; do not log full stacktrace to avoid noisy logs
                log.warn("Nominatim blocked (403) for mode '{}'. Ensure User-Agent/Referer set and you comply with policy.", mode);
            } else {
                log.warn("Nominatim HTTP error ({}): {} {}", mode, e.getStatusCode(), e.getStatusText());
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Nominatim call failed ({}) : {}", mode, e.getMessage());
            return Optional.empty();
        }
    }
}
