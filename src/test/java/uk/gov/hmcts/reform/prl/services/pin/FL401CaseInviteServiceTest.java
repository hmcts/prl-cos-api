package uk.gov.hmcts.reform.prl.services.pin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class FL401CaseInviteServiceTest {


    @InjectMocks
    FL401CaseInviteService fl401CaseInviteService;

    @Mock
    CaseInviteEmailService caseInviteEmailService;

    @Mock
    LaunchDarklyClient launchDarklyClient;

    private CaseData caseDataWithRespondentsAndEmailsNoRepresentation;

    private CaseData getCaseDataWithRespondentsNoEmails;

    private CaseData caseDataWithRespondentsAllWithRepresentation;

    @Before
    public void init() {

        PartyDetails respondentOneWithEmail = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondentOne@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.dontKnow)
            .build();

        PartyDetails respondentOneNoEmail = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.No)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.dontKnow)
            .build();

        PartyDetails respondentOneWithEmailAndRepresentation = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondentOne@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        caseDataWithRespondentsAndEmailsNoRepresentation = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .respondentsFL401(respondentOneWithEmail).build();

        getCaseDataWithRespondentsNoEmails = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .respondentsFL401(respondentOneNoEmail).build();

        caseDataWithRespondentsAllWithRepresentation = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .respondentsFL401(respondentOneWithEmailAndRepresentation).build();


    }

    @Test
    public void givenRespondentWithNoRepresentation_whenCaseInvitesGenerated_thenSentToRespondentAndStoredInCaseData() {
        CaseData actualCaseData = fl401CaseInviteService
            .generateAndSendRespondentCaseInvite(caseDataWithRespondentsAndEmailsNoRepresentation);

        assertEquals(1, actualCaseData.getRespondentCaseInvites().size());
        assertEquals("respondentOne@email.com", actualCaseData.getRespondentCaseInvites().get(0).getValue()
            .getCaseInviteEmail());
    }

    @Test
    public void givenRespondentsWithNoEmail() {
        CaseData actualCaseData = fl401CaseInviteService
            .generateAndSendRespondentCaseInvite(getCaseDataWithRespondentsNoEmails);
        assertTrue(actualCaseData.getRespondentCaseInvites().isEmpty());
    }

    @Test
    public void givenRespondentWithRepresentation_whenCaseInvitesGenerated_thenSentToRespondentAndStoredInCaseData() {
        CaseData actualCaseData = fl401CaseInviteService
            .generateAndSendRespondentCaseInvite(caseDataWithRespondentsAllWithRepresentation);
        assertTrue(actualCaseData.getRespondentCaseInvites().isEmpty());

    }

}
