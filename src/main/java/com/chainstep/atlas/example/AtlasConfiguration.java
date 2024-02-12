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
import id.walt.services.hkvstore.HKVStoreService;
import id.walt.signatory.Signatory;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AtlasConfiguration {
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
    public AtlasJwtConfigBuilder atlasJwtConfigBuilder() {
        PresentedBySubjectVerificationPolicy presentedPolicy = new PresentedBySubjectVerificationPolicy();
        GxCredentialSignedByAuthorityVerificationPolicy authorityVerificationPolicy = new GxCredentialSignedByAuthorityVerificationPolicy();
        return configurer -> {
            configurer.configure("/proxy").all(List.of(authorityVerificationPolicy, presentedPolicy));
        };
    }
}
