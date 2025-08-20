package uk.gov.hmcts.reform.prl.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaskEmailTest {


    @Test
    void testMaskEmail() {
        MaskEmail maskEmail = new MaskEmail();
        String maskedEmail = maskEmail.mask("test_user@mailinator.com");
        assertThat(maskedEmail)
            .isEqualTo("t*******r@mailinator.com");
    }

    @Test
    void testMaskEmailInText() {
        MaskEmail maskEmail = new MaskEmail();
        String maskedEmail = maskEmail.mask("Mask email test_user@mailinator.com",
                                            "test_user@mailinator.com");
        assertThat(maskedEmail)
            .isEqualTo("Mask email t*******r@mailinator.com");

    }
}
