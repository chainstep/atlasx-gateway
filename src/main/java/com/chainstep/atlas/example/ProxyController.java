package com.chainstep.atlas.example;

import com.cartrust.atlas.ssikit.AtlasCommunicator;
import com.cartrust.atlas.ssikit.waltid.AtlasJwtService;
import id.walt.sdjwt.JwtVerificationResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.annotation.Nullable;
import java.net.URI;

@Slf4j
@Configuration
@EnableConfigurationProperties
@RestController
@RequestMapping(path = "")
public class ProxyController {
    private final AtlasCommunicator communicator;

    public ProxyController(AtlasCommunicator communicator) {
        this.communicator = communicator;
    }

    @Autowired
    ServicesConfig services;

    @Value("${internal-api-key}")
    private String internalApiKey;

    @RequestMapping("/proxy/{serviceName}/**")
    public ResponseEntity<?> sendAuthorizedProxyRequest(
            HttpServletRequest request, @Nullable @RequestBody Object body,
            @PathVariable("serviceName") String serviceName
    ) {
        if (!this.isAuthenticated(request))
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);

        ServicesConfig.Service service = services.getServiceByName(serviceName);
        if (service == null) {
            return new ResponseEntity<>(
                    "Service with name '" + serviceName + "' not found. Available services are: " + String.join(", ", services.getServiceNames()),
                    HttpStatus.NOT_FOUND
            );
        }
        String queryString = request.getQueryString();
        String servicePath = queryString != null ? (request.getServletPath() + "?" + queryString).replace("/proxy/" + serviceName, "") : request.getServletPath().replace("/proxy/" + serviceName, "");

        URI uri = URI.create(String.format("%s%s", service.getUrl(), servicePath));

        HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());
        String contentType = request.getContentType();
        log.info(String.format("/proxy, Request: %s %s", httpMethod, uri));

        return getResponseEntity(body, uri, httpMethod, contentType);
    }

    @RequestMapping("/internal-proxy")
    public ResponseEntity<?> sendAuthorizedInternalProxyRequest(
            HttpServletRequest request, @Nullable @RequestBody Object body,
            @RequestHeader(name = "X-Api-Key", required = false) String apiKey,
            @RequestHeader(name = "X-Service-Url") String serviceUrl
    ) {
        if (apiKey == null || !apiKey.equals(internalApiKey))
            return new ResponseEntity<>("X-Api-Key does not match", HttpStatus.UNAUTHORIZED);

        String serviceUri = String.format("%s", serviceUrl);
        URI uri = URI.create(serviceUri);

        HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());
        String contentType = request.getContentType();
        log.info(String.format("/internal-proxy, Request: %s %s", httpMethod, uri));

        return getResponseEntity(body, uri, httpMethod, contentType);

    }

    private ResponseEntity<?> getResponseEntity(@RequestBody @Nullable Object body, URI uri, HttpMethod httpMethod, String contentType) {
        HttpEntity<?> newHttpEntity = communicator.createHttpEntity(body, contentType != null ? MediaType.valueOf(contentType) : null, true);
        try {
            return communicator.exchange(httpMethod, newHttpEntity, uri, String.class);
        } catch (HttpClientErrorException e) {
            return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error occurred", e);
        }
    }

    private boolean isAuthenticated(HttpServletRequest request) {
        var authorization = request.getHeader("Authorization");
        if (authorization == null) {
            return false;
        }

        String jwt = authorization.replace("Bearer ", "");
        try {
            JwtVerificationResult verificationResult = AtlasJwtService.Companion.getService().verify(jwt);
            if (!verificationResult.getVerified())
                return false;
        } catch (Exception e) {
            log.info(e.getMessage());
            return false;
        }
        return true;
    }

}