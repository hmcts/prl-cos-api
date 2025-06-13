package uk.gov.hmcts.reform.prl.services.transfercase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
class TransferCaseContentProviderTest {
    @InjectMocks
    TransferCaseContentProvider transferCaseContentProvider;

    CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder()
            .id(123455)
            .caseTypeOfApplication("fl401")
            .respondentsFL401(PartyDetails.builder().representativeFirstName("1Abc")
                                  .representativeLastName("1Xyz")
                                  .gender(Gender.male)
                                  .email("1abc@xyz.com")
                                  .phoneNumber("11234567890")
                                  .canYouProvideEmailAddress(Yes)
                                  .isEmailAddressConfidential(Yes)
                                  .isPhoneNumberConfidential(Yes)
                                  .solicitorOrg(Organisation.builder().organisationID("1ABC").organisationName("1XYZ").build())
                                  .solicitorAddress(Address.builder().addressLine1("1ABC").postCode("1AB1 2MN").build())
                                  .doTheyHaveLegalRepresentation(YesNoDontKnow.yes).firstName("fn").lastName("ln")
                                  .build())
            .build();

    }

    @Test
    void testBuildTransferOfCaseEmailSolicitor() {
        EmailTemplateVars emailTemplateVars = transferCaseContentProvider.buildCourtTransferEmail(caseData,"test");
        assertEquals("123455",emailTemplateVars.getCaseReference());
    }
}
