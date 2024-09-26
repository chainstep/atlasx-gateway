package com.chainstep.atlas.example;

import com.cartrust.atlas.ssikit.catalogue.AtlasCatalogueConfiguration;
import com.cartrust.atlas.ssikit.config.AtlasConfigProperties;
import com.cartrust.atlas.ssikit.config.AtlasInitializer;
import com.cartrust.atlas.ssikit.config.AtlasJwtConfigBuilder;
import com.cartrust.atlas.ssikit.config.OpenApiAccessor;
import com.cartrust.atlas.ssikit.gx.AtlasServiceOfferingManager;
import com.cartrust.atlas.ssikit.policies.GxCredentialSignedByAuthorityVerificationPolicy;
import com.cartrust.atlas.ssikit.policies.PresentedBySubjectVerificationPolicy;
import com.cartrust.atlas.ssikit.waltid.AtlasVcTemplateService;
import com.cartrust.atlas.ssikit.waltid.FSHKVStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.walt.auditor.VerificationPolicy;
import id.walt.services.hkvstore.HKVStoreService;
import id.walt.signatory.Signatory;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@Slf4j
public class AtlasConfiguration {

    @Bean
    @ConfigurationProperties("routes")
    public Map<String, List<String>> getRoutes() {
        return new HashMap<>();
    }

    @Bean
    @ConfigurationProperties("policies.custompolicy")
    public Map<String, CustomPolicy> getCustomPolicies() {
        return new HashMap<>();
    }

    @Bean
    public AtlasCatalogueConfiguration atlasCatalogueConfiguration() {
        return AtlasCatalogueConfiguration.builder()
                .peer("https://authority.atlas.cartrust.com/gx/catalogue")
                .build();
    }

    @Bean
    public GroupedOpenApi distanceGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("ServiceOfferingProxy") // This name has to match the ServiceOffering template name
                .packagesToScan("com.chainstep.atlas.example")
                .pathsToMatch("/proxy").addOpenApiCustomizer(openApi -> openApi.addSecurityItem(new SecurityRequirement().addList("Authorization"))
                        .components(new Components()
                                .addSecuritySchemes("Authorization", new SecurityScheme()
                                        .in(SecurityScheme.In.HEADER)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                )
                .build();
    }

    @Bean
    public HKVStoreService hkvStore() {
        return new FSHKVStore("hkv-example");
    }

    @Bean
    public AtlasServiceOfferingManager serviceOfferingManager(
            AtlasConfigProperties configuration,
            AtlasInitializer atlasInitializer,
            Signatory signatoryService,
            AtlasVcTemplateService templateService,
            OpenApiAccessor openApiAccessor,
            ObjectMapper mapper
    ) {

        return new AtlasServiceOfferingManager(
                configuration,
                atlasInitializer,
                signatoryService,
                templateService,
                openApiAccessor,
                mapper
        );
    }

    @Bean
    public AtlasJwtConfigBuilder atlasJwtConfigBuilder(
            Map<String, List<String>> routes,
            Map<String, VerificationPolicy> defaultPolicies,
            Map<String, CustomPolicy> customPolicies) {
        var policies = Stream
                .concat(defaultPolicies.entrySet().stream(), customPolicies.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return configurer -> routes.forEach((route, policyNames) -> {
            var routePolicies = policyNames.stream().map(policies::get).map(p -> (VerificationPolicy)p).toList();
            log.info("Configure path {} with policies {}", route, routePolicies);
            configurer.configure("/proxy/" + route).all(routePolicies);
        });
    }

    @Bean("PresentedBySubjectPolicy")
    public PresentedBySubjectVerificationPolicy getPresentedBySubjectVerificationPolicy() {
        return new PresentedBySubjectVerificationPolicy();
    }

    @Bean("CredentialSignedByAuthorityPolicy")
    public GxCredentialSignedByAuthorityVerificationPolicy getGxCredentialSignedByAuthorityVerificationPolicy() {
        return new GxCredentialSignedByAuthorityVerificationPolicy();
    }
}
