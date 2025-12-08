package uk.gov.hmcts.reform.prl.mapper.staffresponse;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.mapper.dynamiclistelement.LegalAdviserDynamicListElementConverter;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffProfile;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffResponse;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGALOFFICE;

class LegalAdviserDynamicListElementFilterTest {

    @Test
    void givenUserIsLegalAdviser_whenConvert_thenReturnDynamicList() {
        StaffProfile staffProfile = StaffProfile.builder()
            .userType(LEGALOFFICE)
            .build();
        StaffResponse staffResponse = StaffResponse.builder()
            .staffProfile(staffProfile)
            .build();

        LegalAdviserDynamicListElementConverter converter = mock(LegalAdviserDynamicListElementConverter.class);
        DynamicListElement dynamicListElement = mock(DynamicListElement.class);
        when((converter.convert(staffProfile))).thenReturn(dynamicListElement);

        LegalAdviserDynamicListElementFilter filter = new LegalAdviserDynamicListElementFilter(converter);
        Optional<DynamicListElement> result = filter.filter(staffResponse);

        assertThat(result).contains(dynamicListElement);
    }

    @Test
    void givenUserIsNotLegalAdviser_whenConvert_thenReturnEmpty() {
        StaffProfile staffProfile = StaffProfile.builder()
            .userType("SomeOtherType")
            .build();
        StaffResponse staffResponse = StaffResponse.builder()
            .staffProfile(staffProfile)
            .build();

        LegalAdviserDynamicListElementConverter converter = mock(LegalAdviserDynamicListElementConverter.class);

        LegalAdviserDynamicListElementFilter filter = new LegalAdviserDynamicListElementFilter(converter);
        Optional<DynamicListElement> result = filter.filter(staffResponse);

        assertThat(result).isNotPresent();
    }
}
