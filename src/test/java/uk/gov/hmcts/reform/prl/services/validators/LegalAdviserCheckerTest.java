package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.staff.StaffUser;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffProfile;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LegalAdviserCheckerTest {

    @InjectMocks
    private LegalAdviserChecker legalAdviserChecker;
    @Mock
    private RefDataUserService refDataUserService;

    private StaffUser geoff = new StaffUser("Geoff");


    @Test
    public void checkNullLegalAdviserListAndNullLegalAdviserUserReturnsNull() {

        Optional<String> result = legalAdviserChecker
            .validateLegalAdviserName(null, null);
        String res = result.orElse(null);
        Assertions.assertNull(res);
    }

    @Test
    public void checkNullLegalAdviserListAndEmptyLegalAdviserIdamIdReturnsNull() {

        Optional<String> result = legalAdviserChecker
            .validateLegalAdviserName(null, new StaffUser(""));
        String res = result.orElse(null);
        Assertions.assertNull(res);
    }

    @Test
    public void checkPopulatedLegalAdviserUserReturnsFirstNameWhenLegalAdviserFirstNameOnlyIsPresent() {

        StaffProfile staffProfile = new StaffProfile("12345", "Geoff", null, "legaladviser", "geoffla@gmail.com");
        when(refDataUserService.getLegalAdviserDetails(any())).thenReturn(Optional.of(staffProfile));
        Optional<String> result = legalAdviserChecker.validateLegalAdviserName(
            null, geoff
        );
        String res = result.orElse(null);
        Assertions.assertEquals("Geoff", res);
    }

    @Test
    public void checkPopulatedLegalAdviserUserReturnsLastNameWhenLegalAdviserLastNameOnlyIsPresent() {

        StaffProfile staffProfile = new StaffProfile("12345", null, "Brisket", "legaladviser", "geoffla@gmail.com");
        when(refDataUserService.getLegalAdviserDetails(any())).thenReturn(Optional.of(staffProfile));
        Optional<String> result = legalAdviserChecker.validateLegalAdviserName(
            null, geoff
        );
        String res = result.orElse(null);
        Assertions.assertEquals("Brisket", res);
    }

    @Test
    public void checkPopulatedLegalAdviserUserReturnsFullNameWhenLegalAdviserFullNameIsPresent() {

        StaffProfile staffProfile = new StaffProfile("12345", "Geoff", "Brisket", "legaladviser", "geoffla@gmail.com");
        when(refDataUserService.getLegalAdviserDetails(any())).thenReturn(Optional.of(staffProfile));
        Optional<String> result = legalAdviserChecker.validateLegalAdviserName(
            null, geoff
        );
        String res = result.orElse(null);
        Assertions.assertEquals("Geoff Brisket", res);
    }

    @Test
    public void checkEmptyLegalAdviserListLabelValueReturnsEmptyOptionalWhenNoLegalAdviserNameIsPresent() {
        DynamicList dynamicList = DynamicList.builder()
            .value(DynamicListElement.builder().code("legaladvisor-swansea-two(prl_legaladvisor_swansea@hmcts.net)").label("")
                       .build()).build();

        Optional<String> result = legalAdviserChecker.validateLegalAdviserName(
            dynamicList, new StaffUser(null)
        );
        Assertions.assertEquals(Optional.empty(), result);
    }

    @Test
    public void checkPopulatedLegalAdviserListAndNullLegalAdviserUserReturnsLegalAdviserList() {

        DynamicList dynamicList = DynamicList.builder()
            .value(DynamicListElement.builder().code("legaladvisor-swansea-two(prl_legaladvisor_swansea@hmcts.net)").label("legaladvisor-swansea-two(prl_legaladvisor_swansea@hmcts.net)")
                                                                  .build()).build();

        Optional<String> result = legalAdviserChecker.validateLegalAdviserName(
            dynamicList, null
        );
        String res = result.orElse(null);
        Assertions.assertEquals("legaladvisor-swansea-two(prl_legaladvisor_swansea@hmcts.net)", res);
    }

    @Test
    public void checkPopulatedLegalAdviserListAndPopulatedLegalAdviserUserReturnsLegalAdviserUser() {

        DynamicList dynamicList = DynamicList.builder().value(
            DynamicListElement.builder().code("12345:").label("test").build()).build();
        StaffProfile staffProfile = new StaffProfile("12345", "Geoff", "Brisket", "legaladviser", "geoffla@gmail.com");
        when(refDataUserService.getLegalAdviserDetails(any())).thenReturn(Optional.of(staffProfile));
        Optional<String> result = legalAdviserChecker.validateLegalAdviserName(
            dynamicList, geoff
        );
        String res = result.orElse(null);
        Assertions.assertEquals("Geoff Brisket", res);
    }

    @Test
    public void isLegalAdviserListPresentReturnsFalseForNullList() {
        boolean result = legalAdviserChecker.isLegalAdviserListPresent(null);
        Assertions.assertFalse(result);
    }

    @Test
    public void isLegalAdviserListPresentReturnsFalseForBlankValueLabel() {
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("").build()).build();
        boolean result = legalAdviserChecker.isLegalAdviserListPresent(dynamicList);
        Assertions.assertFalse(result);
    }

    @Test
    public void isLegalAdviserListPresentReturnsTrueForPopulatedValueLabel() {
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test").build()).build();
        boolean result = legalAdviserChecker.isLegalAdviserListPresent(dynamicList);
        Assertions.assertTrue(result);
    }

}
