package uk.gov.hmcts.reform.prl.services.noticeofchange;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
public class NoticeOfChangeContentProviderTest {
    @InjectMocks
    NoticeOfChangeContentProvider noticeOfChangeContentProvider;

    CaseData caseData;

    @Before
    public void setUp() {
        log.info("My changes");
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
    public void testBuildNoticeOfChangeEmail() {
        log.info("My changes");
        EmailTemplateVars emailTemplateVars = noticeOfChangeContentProvider.buildNoticeOfChangeEmail(caseData, "Solicitor Name");
        assertEquals("123455",emailTemplateVars.getCaseReference());
    }

}
