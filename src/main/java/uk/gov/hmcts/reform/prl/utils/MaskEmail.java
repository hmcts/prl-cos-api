package uk.gov.hmcts.reform.prl.utils;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MaskEmail {

    public String mask(String text, String email) {
        return Optional.ofNullable(text)
            .map(t -> t.replaceAll(email, mask(email)))
            .orElse(null);
    }

    public String mask(String email) {
        return Optional.ofNullable(email)
            .map(e -> {
                int at = e.indexOf('@');
                if (at < 1 || at == e.length() - 1 || e.indexOf('@', at + 1) != -1) {
                    return e; // invalid shape
                }
                String user = e.substring(0, at);
                String domain = e.substring(at + 1);
                String maskedUser = user.length() <= 2
                    ? "*".repeat(user.length())
                    : user.charAt(0) + "*".repeat(user.length() - 2) + user.charAt(user.length() - 1);
                return maskedUser + "@" + domain;
            })
            .orElse("");
    }
}
