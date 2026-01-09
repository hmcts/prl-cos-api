package uk.gov.hmcts.reform.prl.mapper.dynamiclistelement;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffProfile;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LegalAdviserDynamicListElementBiConverterTest {

    @Test
    void testConvert() {
        String idamId = UUID.randomUUID().toString();
        StaffProfile staffProfile = StaffProfile.builder()
            .id(idamId)
            .firstName("Armitage")
            .lastName("Shanks")
            .emailId("armitage.shanks@justice.gov.uk")
            .build();

        LegalAdviserDynamicListElementBiConverter converter = new LegalAdviserDynamicListElementBiConverter();
        DynamicListElement dynamicListElement = converter.convert(staffProfile);

        assertThat(dynamicListElement.getCode()).isEqualTo(idamId);
        assertThat(dynamicListElement.getLabel()).isEqualTo("Armitage Shanks (armitage.shanks@justice.gov.uk)");
    }

    @Test
    void testConvertFromDynamicListElement() {
        String idamId = UUID.randomUUID().toString();
        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code(idamId)
            .label("Armitage Shanks (armitage.shanks@justice.gov.uk)")
            .build();

        LegalAdviserDynamicListElementBiConverter converter = new LegalAdviserDynamicListElementBiConverter();
        LegalAdviserIdamId legalAdviserIdamId = converter.convertFromDynamicListElement(dynamicListElement);
        assertThat(legalAdviserIdamId.getIdamId()).isEqualTo(idamId);
        assertThat(legalAdviserIdamId.getFullName()).isEqualTo("Armitage Shanks");
        assertThat(legalAdviserIdamId.getEmail()).isEqualTo("armitage.shanks@justice.gov.uk");
    }

    @Test
    void givenInvalidLabel_whenConvertFromDynamicListElement_thenHandleGracefully() {
        String idamId = UUID.randomUUID().toString();
        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code(idamId)
            .label("InvalidLabelWithoutEmailFormat")
            .build();

        LegalAdviserDynamicListElementBiConverter converter = new LegalAdviserDynamicListElementBiConverter();
        LegalAdviserIdamId legalAdviserIdamId = converter.convertFromDynamicListElement(dynamicListElement);
        assertThat(legalAdviserIdamId.getIdamId()).isEqualTo(idamId);
        assertThat(legalAdviserIdamId.getFullName()).isEqualTo("InvalidLabelWithoutEmailFormat");
        assertThat(legalAdviserIdamId.getEmail()).isEqualTo("InvalidLabelWithoutEmailFormat");
    }
}
