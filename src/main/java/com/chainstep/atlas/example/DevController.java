package com.chainstep.atlas.example;

import com.cartrust.atlas.ssikit.AtlasCommunicator;
import com.cartrust.atlas.ssikit.waltid.AtlasJwtService;
import id.walt.sdjwt.JwtVerificationResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping(path = "dev")
@Profile("dev")
// This Controller is only used for development purposes and will not work in production environments
public class DevController {
    private final AtlasCommunicator communicator;
    public DevController(AtlasCommunicator communicator) {
        this.communicator = communicator;
    }
    // This endpoint is used to get a JWT from Atlas using our self-description for testing purposes
    @GetMapping(path = "/jwt")
    public ResponseEntity<?> getJWTRequest(HttpServletRequest request) {
        HttpEntity<?> originalHttpEntity = communicator.createHttpEntity(null, null, true);
        String jwt = Objects.requireNonNull(originalHttpEntity.getHeaders().get("Authorization")).get(0).replace("Bearer ", "");
        return ResponseEntity.ofNullable(jwt);
    }
}
