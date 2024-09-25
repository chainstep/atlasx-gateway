package com.chainstep.atlas.example;

import com.cartrust.atlas.ssikit.config.AtlasInitializer;
import id.walt.credentials.w3c.PresentableCredential;
import id.walt.credentials.w3c.VerifiableCredential;
import id.walt.services.vc.JwtCredentialService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles(profiles = "test")
public class AtlasGatewayTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Autowired
    JwtCredentialService jwtCredentialService;

    @Autowired
    AtlasInitializer initializer;

    @Test
    void unauthorized() {
        var url = "http://localhost:" + port + "/proxy/ExampleServiceOffering1";

        // when
        var response = this.restTemplate.exchange(url, HttpMethod.GET, null, String.class);

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void notFound() {
        var token = getToken(null);
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        var entity = new HttpEntity<>(null, headers);
        var url = "http://localhost:" + port + "/proxy/UnknownServiceOffering";

        // when
        var response = this.restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void ok() {
        var token = getToken(null);
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        var entity = new HttpEntity<>(null, headers);
        var url = "http://localhost:" + port + "/proxy/ExampleServiceOffering1";

        // when
        var response = this.restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Hello World", response.getBody());
    }

    @Test
    void admin_policy_allows_access() {
        var token = getToken(vp_0815_ADMIN);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        var url = "http://localhost:" + port + "/proxy/ExampleServiceOffering2";

        // when
        var response = this.restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Hello World", response.getBody());
    }

    @Test
    void admin_policy_denies_access() {
        var token = getToken(vp_0815_USER);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        var url = "http://localhost:" + port + "/proxy/ExampleServiceOffering2";

        // when
        var response = this.restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        System.out.println(response.getBody());

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    private @NotNull String getToken(String vp) {
        var did = initializer.getIssuerDid();
        var expirationDate = Instant.now().plus(24, ChronoUnit.HOURS);
        List<PresentableCredential> presentations = new ArrayList<>();
        if (vp != null) {
            var verifiableCredential = VerifiableCredential.Companion.fromString(vp);
            var pc = new PresentableCredential(verifiableCredential, null, true);
            presentations.add(pc);
        }
        return jwtCredentialService.present(presentations, did, did, null, expirationDate);
    }

    String vp_0815_ADMIN = """
            {
              "type": ["VerifiableCredential"],
              "credentialSubject": {
                "role":"ADMIN",
                "legalRegistrationNumber": "0815"
              }
            }
            """;

    String vp_0815_USER = """
            {
              "type": ["VerifiableCredential"],
              "credentialSubject": {
                "role":"USER",
                "legalRegistrationNumber": "0815"
              }
            }
            """;

}
