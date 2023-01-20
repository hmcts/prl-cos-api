package uk.gov.hmcts.reform.prl.services.noticeofchange;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

public class NoticeOfChangePartiesServiceTest {

    @Mock
    NoticeOfChangePartiesService noticeOfChangePartiesService;

    CaseData caseData;
    SolicitorRole role;

    @Before
    public void setUp(){
        MockitoAnnotations.openMocks(this);

        PartyDetails respondent = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);

        caseData = CaseData.builder().respondents(respondentList)
            .build();

        role = SolicitorRole.SOLICITORA;
    }

    @Test
    public void generate(){

        Map<String, Object> test = noticeOfChangePartiesService.generate(caseData, role.getRepresenting());

        Assert.assertNotNull(test);

    }

    @Test
    public void generateWithStrategy(){

        NoticeOfChangePartiesService
            .NoticeOfChangeAnswersPopulationStrategy strategy = NoticeOfChangePartiesService
            .NoticeOfChangeAnswersPopulationStrategy.POPULATE;

        Map<String, Object> test = noticeOfChangePartiesService.generate(caseData, role.getRepresenting(), strategy);

        Assert.assertNotNull(test);

    }
}
