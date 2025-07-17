package uk.gov.hmcts.reform.prl.services;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@RunWith(MockitoJUnitRunner.class)
class ConfidentialDetailsChangeHelperTest {

    @Mock
    private final ConfidentialDetailsChangeHelper confidentialDetailsChangeHelper = new ConfidentialDetailsChangeHelper();

    @Test
    void shouldReturnFalse_whenApplicantsAreSame() {
        PartyDetails applicant = PartyDetails.builder()
            .firstName("John")
            .lastName("Doe")
            .email("JohnDoe@test.com")
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        Element<PartyDetails> applicantElement = Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(applicant)
            .build();

        List<Element<PartyDetails>> applicants = List.of(applicantElement);

        CaseData current = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicants)
            .build();

        CaseData previous = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicants)
            .build();

        boolean changed = confidentialDetailsChangeHelper.haveConfidentialDetailsChanged(current, previous);

        assertFalse(changed);
    }

    @Test
    void shouldReturnTrueWhenApplicantsAreNotSame() {
        PartyDetails applicantPrevious = PartyDetails.builder()
            .firstName("john")
            .lastName("Doe")
            .email("JohnDoe@test.com")
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        PartyDetails applicantCurrent = PartyDetails.builder()
            .firstName("john")
            .lastName("Doe")
            .email("JohnDoe@test.com")
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .build();

        CaseData previous = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(applicantPrevious)
            .build();

        CaseData current = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(applicantCurrent)
            .build();

        boolean changed = confidentialDetailsChangeHelper.haveConfidentialDetailsChanged(current, previous);

        assertTrue(changed);
    }

    @Test
    void shouldReturnTruewhenApplicantsSizeAreDifferent() {
        PartyDetails applicant = PartyDetails.builder()
            .firstName("af1")
            .lastName("al1")
            .email("afl11@test.com")
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        Element<PartyDetails> applicantElement = Element.<PartyDetails>builder()
            .id(UUID.randomUUID())
            .value(applicant)
            .build();

        List<Element<PartyDetails>> applicants = List.of(applicantElement);

        CaseData current = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(null)
            .build();

        CaseData previous = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicants)
            .build();

        boolean changed = confidentialDetailsChangeHelper.haveConfidentialDetailsChanged(current, previous);

        assertTrue(changed);
    }
}

