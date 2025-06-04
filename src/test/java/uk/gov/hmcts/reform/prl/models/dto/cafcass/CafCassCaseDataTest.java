package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.enums.Gender;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

class CafCassCaseDataTest {

    private final CafCassCaseData caseData = new CafCassCaseData();

    @Test
    void testBuildFullNameWhenFirstAndLastProvided() {
        String result = caseData.buildFullName("John", "Doe");
        Assertions.assertEquals("John Doe", result);
    }

    @Test
    void testBuildFullNameWhenFirstIsNull() {
        String result = caseData.buildFullName(null, "Doe");
        Assertions.assertEquals("Doe", result);
    }

    @Test
    void testBuildFullNameWhenLastIsNull() {
        String result = caseData.buildFullName("John", null);
        Assertions.assertEquals("John", result);
    }

    @Test
    void testBuildFullNameWhenBothAreNull() {
        String result = caseData.buildFullName(null, null);
        Assertions.assertEquals("", result);
    }

    @Test
    void testBuildFullNameWhenFirstAndLastAreEmpty() {
        String result = caseData.buildFullName("", "");
        Assertions.assertEquals("", result);
    }

    @Test
    void testBuildWithOtherPartyConfidentialInfo() {
        List<Element<ApplicantDetails>> applicantDetails = new ArrayList<>();
        ApplicantDetails app = ApplicantDetails.builder().isAddressConfidential(Yes).isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .gender(Gender.male)
            .build();
        Element el = Element.builder().value(app).build();
        applicantDetails.add(el);

        caseData.setOtherPartyInTheCaseRevised(applicantDetails);
        Assertions.assertEquals(Yes, caseData.getOtherPeopleInTheCaseTable().get(0).getValue().getIsAddressConfidential());
        Assertions.assertEquals(Yes, caseData.getOtherPeopleInTheCaseTable().get(0).getValue().getIsEmailAddressConfidential());
        Assertions.assertEquals(Yes, caseData.getOtherPeopleInTheCaseTable().get(0).getValue().getIsPhoneNumberConfidential());
    }
}
