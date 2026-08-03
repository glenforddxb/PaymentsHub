package com.cth.sdm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MfaService {
    private boolean mfaToggledOn = false;

    public boolean isMfaToggledOn() {
        return mfaToggledOn;
    }

    public void setMfaToggledOn(boolean enabled) {
        this.mfaToggledOn = enabled;
        log.info("MFA authentication state set to: {}", enabled);
    }

    public void sendMfaCode(String username, String contactDetail) {
        if (!mfaToggledOn) {
            log.info("MFA is currently disabled for demo. Simulated OTP bypass generated for user {}.", username);
            return;
        }
        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        log.info("--- MFA SIMULATION TRIGGERED ---");
        log.info("Sending 6-digit verification code [{}] via SMS/Email to: {}", otp, contactDetail);
        log.info("---------------------------------");
    }
}
