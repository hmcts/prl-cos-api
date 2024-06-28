package uk.gov.hmcts.reform.prl.services.pin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FL401CaseInviteServiceTest {

    @InjectMocks
    FL401CaseInviteService fl401CaseInviteService;

    @Mock
    CaseInviteEmailService caseInviteEmailService;

    @Mock
    LaunchDarklyClient launchDarklyClient;

    private CaseData caseDataWithRespondentsAndEmailsNoRepresentation;

    private CaseData caseDataWithRespondentsAndEmailsOnePartyNoRepresentation;

    private CaseData getCaseDataWithRespondentsNoEmails;

    private CaseData caseDataWithRespondentsAllWithRepresentation;

    private CaseData caseDataNoRespondents;

    @Before
    public void init() {

        PartyDetails respondentOneWithEmail = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondentOne@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.dontKnow)
            .build();

        caseDataWithRespondentsAndEmailsNoRepresentation = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .respondentsFL401(respondentOneWithEmail).build();

        PartyDetails respondentTwoWithEmailAndRepresentation = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("Two")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondentTwo@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();


        caseDataWithRespondentsAndEmailsOnePartyNoRepresentation = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .respondentsFL401(respondentTwoWithEmailAndRepresentation).build();

        PartyDetails respondentOneNoEmail = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.No)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.dontKnow)
            .build();

        getCaseDataWithRespondentsNoEmails = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .respondentsFL401(respondentOneNoEmail).build();

        PartyDetails respondentOneWithEmailAndRepresentation = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondentOne@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        caseDataWithRespondentsAllWithRepresentation = CaseData.builder()
            .caseTypeOfApplication("C100")
            .respondentsFL401(respondentOneWithEmailAndRepresentation).build();

        caseDataNoRespondents = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .build();

    }

    @Test
    public void givenRespondentsWithNoRepresentation_whenCaseInvitesGenerated_thenSentToAllRespondentsAndStoredInCaseData() {
        CaseData actualCaseData = fl401CaseInviteService
            .sendCaseInviteEmail(caseDataWithRespondentsAndEmailsNoRepresentation);

        verify(caseInviteEmailService,times(1)).sendCaseInviteEmail(any(),any(),any());
    }

    @Test
    public void givenRespondentWithRepresentation_whenCaseInvitesGenerated_thenSentToOnlyThoseWithoutRepresentation() {
        CaseData actualCaseData = fl401CaseInviteService
            .sendCaseInviteEmail(caseDataWithRespondentsAndEmailsOnePartyNoRepresentation);

        //two respondents but only one should have a case invite generated
        verify(caseInviteEmailService,times(1)).sendCaseInviteEmail(any(),any(),any());
    }

    @Test
    public void givenMultipleRespondentsWithNoEmail_whenCaseInvitesGenerated_thenNoRespondentsReceiveInvite() {
        CaseData actualCaseData = fl401CaseInviteService
            .sendCaseInviteEmail(getCaseDataWithRespondentsNoEmails);
        assertTrue(actualCaseData.getCaseInvites().isEmpty());
    }

    @Test
    public void givenMultipleRespondentsWithEmailAndRepresentation_whenCaseInvitesGenerated_thenNoRespondentsReceiveInvite() {
        CaseData actualCaseData = fl401CaseInviteService
            .sendCaseInviteEmail(caseDataWithRespondentsAllWithRepresentation);
        assertTrue(actualCaseData.getCaseInvites().isEmpty());
    }

    @Test
    public void givenNoRespondents_whenCaseInvitesGenerated_thenNoInvitesGenerated() {
        CaseData actualCaseData = fl401CaseInviteService
            .sendCaseInviteEmail(caseDataWithRespondentsAllWithRepresentation);
        assertTrue(actualCaseData.getCaseInvites().isEmpty());
    }

    @Test
    public void givenApplicants_whenCaseInvitesGenerated_thenSendInvite() {
        PartyDetails applicant = PartyDetails.builder()
            .firstName("test")
            .email("testfl401@applicant.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .build();
        caseDataWithRespondentsAllWithRepresentation = caseDataWithRespondentsAllWithRepresentation.toBuilder()
            .applicantsFL401(applicant)
            .build();
        verify(caseInviteEmailService,times(0)).sendCaseInviteEmail(any(),any(),any());
    }

    @Test
    public void testToGenerateCaseInviteAndSendToFl401Applicant() {

        PartyDetails respondent = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondentOne@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        PartyDetails applicant = PartyDetails.builder()
            .firstName("Applicant")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("applicant@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicantCaseName("test")
            .applicantsFL401(applicant)
            .respondentsFL401(respondent)
            .build();
        when(launchDarklyClient.isFeatureEnabled("generate-da-citizen-applicant-pin")).thenReturn(true);

        List<Element<CaseInvite>> partyCaseInvites = fl401CaseInviteService
            .generateAndSendCaseInviteForDaApplicant(caseData, applicant);
        assertEquals(2, partyCaseInvites.size());
        assertEquals(YesOrNo.Yes, partyCaseInvites.get(0).getValue().getIsApplicant());

    }

    @Test
    public void testToGenerateCaseInviteAndSendToFl401Respondent() {

        PartyDetails respondent = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        PartyDetails applicant = PartyDetails.builder()
            .firstName("Applicant")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("applicant@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicantCaseName("test")
            .applicantsFL401(applicant)
            .respondentsFL401(respondent)
            .build();

        List<Element<CaseInvite>> partyCaseInvites = fl401CaseInviteService
            .generateAndSendCaseInviteForDaRespondent(caseData, respondent);
        assertEquals(1, partyCaseInvites.size());
        assertEquals(YesOrNo.No, partyCaseInvites.get(0).getValue().getIsApplicant());

    }
}
