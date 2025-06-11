package uk.gov.hmcts.reform.prl.utils;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EmailObfuscatorTest {

    @Test
    public void obfuscateShortEmails() {
        assertThat(EmailObfuscator.obfuscate("     aa@example.com"), is("a*@example.com"));
        assertThat(EmailObfuscator.obfuscate("aa@example.com"), is("a*@example.com"));
        assertThat(EmailObfuscator.obfuscate("a@example.com"), is("a@example.com"));
    }

    @Test
    public void obfuscateNormalEmails() {
        assertThat(EmailObfuscator.obfuscate("     abcdef@example.com"), is("a***f@example.com"));
        assertThat(EmailObfuscator.obfuscate("abcdef@example.com"), is("a***f@example.com"));
    }

    @Test
    void obfuscateNullShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            EmailObfuscator.obfuscate(null);
        });
    }

    @Test
    void obfuscateEmptyStringShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            EmailObfuscator.obfuscate("");
        });
    }

    @Test
    void obfuscateTooShortEmailShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            EmailObfuscator.obfuscate("@example.com");
        });
    }

    @Test
    void obfuscateInvalidEmailShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            EmailObfuscator.obfuscate("example.com");
        });
    }
}
