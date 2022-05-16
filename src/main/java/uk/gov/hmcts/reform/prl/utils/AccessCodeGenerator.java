package uk.gov.hmcts.reform.prl.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.security.SecureRandom;

public final class AccessCodeGenerator {

    private static final String ALLOWED_CHARS = "ABCDEFGHJKLMNPRSTVWXYZ23456789";

    private AccessCodeGenerator() {
    }

    public static String generateAccessCode() {
        return RandomStringUtils.random(8, 0, ALLOWED_CHARS.length(), false, false, ALLOWED_CHARS.toCharArray(), new SecureRandom());
    }

}
