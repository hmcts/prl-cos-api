package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collections;

import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.APPLICANT;

public class CourtLocatorServiceTest {

    @Autowired
    CourtLocatorService courtLocatorService;

    @Test
    public void givenChildPresent_whenLivesWithApplicant_thenReturnApplicantPostcode() {

        Address applicantAddress = Address.builder()
            .addressLine1("123 Test Address")
            .postTown("London")
            .country("UK")
            .postCode("AB12 3AL")
            .build();

        PartyDetails applicant = PartyDetails.builder()
            .address(applicantAddress)
            .build();

        Child child = Child.builder()
            .childLiveWith(Collections.singletonList(APPLICANT))
            .build();

        Element<Child> wrappedChild = Element.<Child>builder().value(child).build();;

        CaseData caseData = CaseData.builder()
            .children(Collections.singletonList(wrappedChild))
            .build();

        assert(courtLocatorService.whichPostCodeToUse(caseData).equals("AB12 3AL"));

    }



}
