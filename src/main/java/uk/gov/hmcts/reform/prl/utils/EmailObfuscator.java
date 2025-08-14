package uk.gov.hmcts.reform.prl.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailObfuscator {

    public static String obfuscate(String email) {
        if (!Optional.ofNullable(email).isPresent()) {
            throw new IllegalArgumentException("Email must not be empty");
        }

        String[] parts = email.split("@");

        if (parts.length != 2 || parts[0].length() < 1) {
            throw new IllegalArgumentException("Invalid email.");
        }

        return replaceAddressWithAsterisks(parts);
    }

    private static String replaceAddressWithAsterisks(String[] parts) {
        String address = parts[0].trim();
        String domain = parts[1].trim();

        if (address.length() < 3) {
            return address.charAt(0)
                + StringUtils.repeat("*", address.length() - 1)
                + "@" + domain;
        }

        return concatenateParts(address, parts[1]);
    }

    private static String concatenateParts(String address, String domain) {
        final int numberOfAsterisks = address.length() - 3;
        final int lastCharacterIndex = address.length() - 1;
        final int firstCharacterIndex = 0;

        return address.charAt(firstCharacterIndex)
            + StringUtils.repeat("*", numberOfAsterisks)
            + address.charAt(lastCharacterIndex)
            + "@" + domain;
    }
}
