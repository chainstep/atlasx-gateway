package com.chainstep.atlas.example;

import id.walt.auditor.SimpleVerificationPolicy;
import id.walt.auditor.VerificationPolicyResult;
import id.walt.credentials.w3c.VerifiableCredential;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@Slf4j
public class CustomPolicy extends SimpleVerificationPolicy {
    // Constructor - initializes CustomPolicy
    public CustomPolicy() {
    }

    // TODO: Please change these Values to define which role of which company is
    // allowed to access yor service.
    private String legalRegistrationNumString = "DummyLRN";
    private String roleToGrantAccessString = "DummyRole";

    // Method to return a description of the policy
    @NotNull
    public String getDescription() {
        return "CustomPolicy checks your defined policies.";
    }

    // Returns false if this policy does not apply to Verifiable Credentials (VCs)
    public boolean getApplyToVC() {
        return false;
    }

    // Returns true if this policy applies to Verifiable Presentations (VPs)
    public boolean getApplyToVP() {
        return true;
    }

    // The main verification method, checks the VC against custom policy
    @NotNull
    protected VerificationPolicyResult doVerify(@NotNull VerifiableCredential vc) {
        log.info("CustomPolicy is called");

        try {
            // Extract the "verifiableCredential" property and convert it to JSON string
            Object object = vc.getProperties().get("verifiableCredential");
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String jsonString = ow.writeValueAsString(object);
            // log.info("returning JSON String: " + jsonString);

            ObjectMapper objectMapper = new ObjectMapper();

            // Parse the JSON string into an array of JsonNodes
            JsonNode rootNode = objectMapper.readTree(jsonString);

            // Access the first element in the array (the credential itself)
            JsonNode credentialNode = rootNode.get(0);

            // Check if the credential has the required legal registration number
            if (isFromLegalRegistrationNumber(credentialNode, legalRegistrationNumString)) {
                //log.info("isFromLegalRegistrationNumber true");
            }
            if (!isFromLegalRegistrationNumber(credentialNode, legalRegistrationNumString)) {
                log.info("isFromLegalRegistrationNumber: Given LRN has no access!");
                return VerificationPolicyResult.Companion.failure(); // Fail if the legal registration number does not
                                                                     // match
            }

            // Check if the credential has the required role
            if (isRole(credentialNode, roleToGrantAccessString)) {
                //log.info("isRole true");
            }
            if (!isRole(credentialNode, roleToGrantAccessString)) {
                log.info("isRole: Given role has no access!");
                return VerificationPolicyResult.Companion.failure(); // Fail if the role is not "remoteControl"
            }

            // If all checks pass, return success
            log.info("Access to the service is granted.");
            return VerificationPolicyResult.Companion.success();

        } catch (Exception e) {
            log.info("Error during parsing the credential subject" + e);
            return VerificationPolicyResult.Companion.failure(new Exception("parsing Error in the CustomPolicy"));
        }

    }

    // Method to check if the credential's legal registration number matches the
    // expected one
    private boolean isFromLegalRegistrationNumber(JsonNode jsonNode, String legalRegistrationNumber) {
        // Get the legal registration number from the credentialSubject field
        JsonNode legalRegistrationNumberNode = jsonNode
                .get("credentialSubject")
                .get("legalRegistrationNumber");

        //log.info("received legalRegistrationNumber  : " + legalRegistrationNumberNode.asText());

        if (!legalRegistrationNumberNode.asText().equals(legalRegistrationNumber)) {
            return false;
        } 
        return true;        
    }

    private boolean isRole(JsonNode jsonNode, String role) {
        // get to the legalRegistrationNumber field

        JsonNode roleNumberNode = jsonNode
                .get("credentialSubject")
                .get("role");

        //log.info("received role  : " + roleNumberNode.asText());

        // Check if the role matches
        if (!roleNumberNode.asText().equals(role)) {
            return false;
        }
        return true;
    }

}
