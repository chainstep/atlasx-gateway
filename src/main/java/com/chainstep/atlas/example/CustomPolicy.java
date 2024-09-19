package com.chainstep.atlas.example;

import id.walt.auditor.SimpleVerificationPolicy;
import id.walt.auditor.VerificationPolicyResult;
import id.walt.credentials.w3c.VerifiableCredential;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
@Slf4j
public class CustomPolicy extends SimpleVerificationPolicy {
    public CustomPolicy() {
    }

    public boolean getApplyToVC() {
      return false;
    }
 
    public boolean getApplyToVP() {
      return true;
    }
    
    @NotNull
    public String getDescription() {
        return "bla bla bla";
    }

    @NotNull
    protected VerificationPolicyResult doVerify(@NotNull VerifiableCredential vc) {
        log.info("CustomPolicy");
        return VerificationPolicyResult.Companion.success();
    }
}
