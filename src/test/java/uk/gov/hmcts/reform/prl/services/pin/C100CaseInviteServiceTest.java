package uk.gov.hmcts.reform.prl.services.pin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.class)
public class C100CaseInviteServiceTest {


    @InjectMocks
    C100CaseInviteService c100CaseInviteService;

    @Mock
    CaseInviteEmailService caseInviteEmailService;

    @Mock
    LaunchDarklyClient launchDarklyClient;

    private CaseData caseDataWithRespondentsAndEmailsNoRepresentation;

    private CaseData caseDataWithRespondentsAndEmailsOnePartyNoRepresentation;


    private CaseData caseDataWithRespondentsAllWithRepresentation;
    private CaseData caseDataWithApplicants;

    private CaseData caseDataNoRespondents;

    private CaseData citizenCaseDataWithApplicantEmail;

    PartyDetails respondentOneWithEmail;
    PartyDetails respondentTwoWithEmail;
    PartyDetails applicantWithOutEmail;
    PartyDetails applicantWithEmail;

    @Before
    public void init() {

        respondentOneWithEmail = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondentOne@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.dontKnow)
            .build();

        respondentTwoWithEmail = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("Two")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondentTwo@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        List<Element<PartyDetails>> respondentsWithEmailsNoRepresentation = List.of(element(respondentOneWithEmail),
                                                                                    element(respondentTwoWithEmail));

        caseDataWithRespondentsAndEmailsNoRepresentation = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(PartyDetails.builder().canYouProvideEmailAddress(YesOrNo.No).build())))
            .respondents(respondentsWithEmailsNoRepresentation).build();

        PartyDetails respondentTwoWithEmailAndRepresentation = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("Two")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondentTwo@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        List<Element<PartyDetails>> respondentsWithEmailsOneNoRepresentation = List.of(element(respondentOneWithEmail),
                                                                                       element(respondentTwoWithEmailAndRepresentation));


        caseDataWithRespondentsAndEmailsOnePartyNoRepresentation = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(PartyDetails.builder().canYouProvideEmailAddress(YesOrNo.Yes)
                                            .doTheyHaveLegalRepresentation(YesNoDontKnow.no).build())))
            .respondents(respondentsWithEmailsOneNoRepresentation).build();

        PartyDetails respondentOneNoEmail = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.No)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.dontKnow)
            .build();

        PartyDetails respondentTwoNoEmail = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("Two")
            .canYouProvideEmailAddress(YesOrNo.No)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        List<Element<PartyDetails>> respondentsNoEmailsNoRepresentation = List.of(element(respondentOneNoEmail),
                                                                                  element(respondentTwoNoEmail));

        CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(PartyDetails.builder().canYouProvideEmailAddress(YesOrNo.No).build())))
            .respondents(respondentsNoEmailsNoRepresentation).build();

        PartyDetails respondentOneWithEmailAndRepresentation = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondentOne@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        PartyDetails secondRespondentWithEmailAndRepresentation = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("Two")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondentTwo@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        List<Element<PartyDetails>> respondentsWithEmailsAndRepresentation = List.of(element(respondentOneWithEmailAndRepresentation),
                                                                                     element(secondRespondentWithEmailAndRepresentation));

        caseDataWithRespondentsAllWithRepresentation = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(PartyDetails.builder().canYouProvideEmailAddress(YesOrNo.No).build())))
            .respondents(respondentsWithEmailsAndRepresentation).build();


        caseDataNoRespondents = CaseData.builder()
            .caseTypeOfApplication("C100")
            .build();

        applicantWithEmail = PartyDetails.builder()
            .firstName("Applicant")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("applicant@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        applicantWithOutEmail = PartyDetails.builder()
            .firstName("Applicant")
            .lastName("Two")
            .canYouProvideEmailAddress(YesOrNo.No)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        citizenCaseDataWithApplicantEmail = CaseData.builder()
            .caseTypeOfApplication("C100")
            .caseCreatedBy(CaseCreatedBy.CITIZEN)
            .applicants(List.of(element(applicantWithEmail),
                                element(applicantWithOutEmail)))
            .respondents(List.of(element(respondentOneWithEmailAndRepresentation),
                                 element(secondRespondentWithEmailAndRepresentation)))
            .build();

        CaseData.builder()
            .caseTypeOfApplication("C100")
            .caseCreatedBy(CaseCreatedBy.SOLICITOR)
            .applicants(List.of(element(applicantWithEmail),
                element(applicantWithOutEmail)))
            .respondents(List.of(element(respondentOneWithEmailAndRepresentation),
                element(secondRespondentWithEmailAndRepresentation)))
            .build();

    }

    @Test
    public void givenRespondentsWithNoRepresentation_whenCaseInvitesGenerated_thenSentToAllRespondentsAndStoredInCaseData() {
        c100CaseInviteService.sendCaseInviteEmail(caseDataWithRespondentsAndEmailsNoRepresentation);

        verify(caseInviteEmailService,times(2)).sendCaseInviteEmail(any(),any(),any());
    }

    @Test
    public void givenRespondentWithRepresentation_whenCaseInvitesGenerated_thenSentToOnlyThoseWithoutRepresentation() {
        c100CaseInviteService.sendCaseInviteEmail(caseDataWithRespondentsAndEmailsOnePartyNoRepresentation);

        //two respondents but only one should have a case invite generated
        verify(caseInviteEmailService,times(1)).sendCaseInviteEmail(any(),any(),any());
    }

    @Test
    public void givenMultipleRespondentsWithNoEmail_whenCaseInvitesGenerated_thenNoRespondentsReceiveInvite() {
        verify(caseInviteEmailService,times(0)).sendCaseInviteEmail(any(),any(),any());
    }

    @Test
    public void givenMultipleRespondentsWithEmailAndRepresentation_whenCaseInvitesGenerated_thenNoRespondentsReceiveInvite() {
        verify(caseInviteEmailService,times(0)).sendCaseInviteEmail(any(),any(),any());
    }

    @Test
    public void givenNoRespondents_whenCaseInvitesGenerated_thenNoInvitesGenerated() {
        verify(caseInviteEmailService,times(0)).sendCaseInviteEmail(any(),any(),any());
    }

    @Test
    public void noCitizenApplicantsInvitesGeneratedAndSentWhenLdFlagIsTurnedOff() {
        verify(caseInviteEmailService, times(0)).sendCaseInviteEmail(any(), any(), any());
    }

    @Test
    public void noCitizenApplicantsInvitesGeneratedAndSentForCaseCreatedBySolicitor() {
        verify(caseInviteEmailService,times(0)).sendCaseInviteEmail(any(),any(),any());
    }

    @Test
    public void generateApplicantsInvitesAndSentForCaseCreatedByCitizen() {
        when(launchDarklyClient.isFeatureEnabled("generate-ca-citizen-applicant-pin")).thenReturn(true);

        c100CaseInviteService.sendCaseInviteEmail(citizenCaseDataWithApplicantEmail);
        verify(caseInviteEmailService,times(1)).sendCaseInviteEmail(any(),any(),any());
    }

    @Test
    public void testToGenerateCaseInviteAndSendToC100Respondent() {

        List<Element<CaseInvite>> respondentCaseInvites = c100CaseInviteService
            .generateAndSendCaseInviteForCaRespondent(caseDataWithRespondentsAllWithRepresentation, element(respondentTwoWithEmail));
        assertEquals(1, respondentCaseInvites.size());
        assertEquals(YesOrNo.No, respondentCaseInvites.get(0).getValue().getIsApplicant());
        assertEquals("respondentTwo@email.com", respondentCaseInvites.get(0).getValue()
            .getCaseInviteEmail());
    }

    @Test
    public void testToGenerateCaseInviteAndSendToC100Applicant() {

        caseDataWithApplicants = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicantCaseName("test")
            .applicants(List.of(element(applicantWithEmail), element(applicantWithOutEmail)))
            .respondents(List.of(element(respondentOneWithEmail), element(respondentTwoWithEmail)))
            .build();

        List<Element<CaseInvite>> applicantCaseInvites = c100CaseInviteService
            .generateAndSendCaseInviteEmailForCaApplicant(caseDataWithApplicants, element(applicantWithEmail));
        assertEquals(1, applicantCaseInvites.size());
        assertEquals(YesOrNo.Yes, applicantCaseInvites.get(0).getValue().getIsApplicant());
        assertEquals("applicant@email.com", applicantCaseInvites.get(0).getValue()
            .getCaseInviteEmail());
    }

    @Test
    public void testToGenerateCaseInviteAndSendToC100ApplicantAndRespondent() {

        PartyDetails respondentOne = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondentOne@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        PartyDetails respondentTwo = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("Two")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondentTwo@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.dontKnow)
            .build();

        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("Applicant")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("applicant@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        PartyDetails applicant2 = PartyDetails.builder()
            .firstName("Applicant")
            .lastName("Two")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("applicant@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicantCaseName("test")
            .applicants(List.of(element(applicant1), element(applicant2)))
            .respondents(List.of(element(respondentOne), element(respondentTwo)))
            .build();

        List<Element<CaseInvite>> partyCaseInvites = c100CaseInviteService
            .generateAndSendCaseInviteForAllC100AppAndResp(caseData);
        assertEquals(2, partyCaseInvites.size());
        assertEquals(YesOrNo.No, partyCaseInvites.get(0).getValue().getIsApplicant());

    }

    @Test
    public void testToGenerateCaseInviteAndSendToC100ApplicantAndRespondent2() {

        PartyDetails respondentOne = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondentOne@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        PartyDetails respondentTwo = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("Two")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondentTwo@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("Applicant")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("applicant@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        PartyDetails applicant2 = PartyDetails.builder()
            .firstName("Applicant")
            .lastName("Two")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("applicant@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.dontKnow)
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicantCaseName("test")
            .applicants(List.of(element(applicant1), element(applicant2)))
            .respondents(List.of(element(respondentOne), element(respondentTwo)))
            .build();

        List<Element<CaseInvite>> partyCaseInvites = c100CaseInviteService
            .generateAndSendCaseInviteForAllC100AppAndResp(caseData);
        assertEquals(2, partyCaseInvites.size());
        assertEquals(YesOrNo.Yes, partyCaseInvites.get(0).getValue().getIsApplicant());

    }
}
