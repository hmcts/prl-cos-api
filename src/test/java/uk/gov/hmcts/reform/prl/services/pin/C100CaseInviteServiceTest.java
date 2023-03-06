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
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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

    private CaseData getCaseDataWithRespondentsNoEmails;

    private CaseData caseDataWithRespondentsAllWithRepresentation;

    private CaseData caseDataNoRespondents;

    private CaseData citizenCaseDataWithApplicantEmail;

    private CaseData solicitorCaseDataWithApplicantEmail;

    @Before
    public void init() {

        PartyDetails respondentOneWithEmail = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondentOne@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.dontKnow)
            .build();

        PartyDetails respondentTwoWithEmail = PartyDetails.builder()
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
            .applicants(List.of(element(PartyDetails.builder().canYouProvideEmailAddress(YesOrNo.Yes).build())))
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

        getCaseDataWithRespondentsNoEmails = CaseData.builder()
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

        PartyDetails applicantWithEmail = PartyDetails.builder()
            .firstName("Applicant")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("applicant@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        PartyDetails applicantWithOutEmail = PartyDetails.builder()
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

        solicitorCaseDataWithApplicantEmail = CaseData.builder()
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
        CaseData actualCaseData = c100CaseInviteService
            .generateAndSendCaseInvite(caseDataWithRespondentsAndEmailsNoRepresentation);

        //case invite for both respondents
        assertEquals(2, actualCaseData.getCaseInvites().size());
        assertEquals("respondentOne@email.com", actualCaseData.getCaseInvites().get(0).getValue()
            .getCaseInviteEmail());
        assertEquals("respondentTwo@email.com", actualCaseData.getCaseInvites().get(1).getValue()
            .getCaseInviteEmail());
    }

    @Test
    public void givenRespondentWithRepresentation_whenCaseInvitesGenerated_thenSentToOnlyThoseWithoutRepresentation() {
        CaseData actualCaseData = c100CaseInviteService
            .generateAndSendCaseInvite(caseDataWithRespondentsAndEmailsOnePartyNoRepresentation);

        //two respondents but only one should have a case invite generated
        assertEquals(1, actualCaseData.getCaseInvites().size());
        assertEquals("respondentOne@email.com", actualCaseData.getCaseInvites().get(0).getValue()
            .getCaseInviteEmail());
    }

    @Test
    public void givenMultipleRespondentsWithNoEmail_whenCaseInvitesGenerated_thenNoRespondentsReceiveInvite() {
        CaseData actualCaseData = c100CaseInviteService
            .generateAndSendCaseInvite(getCaseDataWithRespondentsNoEmails);
        assertTrue(actualCaseData.getCaseInvites().isEmpty());
    }

    @Test
    public void givenMultipleRespondentsWithEmailAndRepresentation_whenCaseInvitesGenerated_thenNoRespondentsReceiveInvite() {
        CaseData actualCaseData = c100CaseInviteService
            .generateAndSendCaseInvite(caseDataWithRespondentsAllWithRepresentation);
        assertTrue(actualCaseData.getCaseInvites().isEmpty());
    }

    @Test
    public void givenNoRespondents_whenCaseInvitesGenerated_thenNoInvitesGenerated() {
        CaseData actualCaseData = c100CaseInviteService
            .generateAndSendCaseInvite(caseDataWithRespondentsAllWithRepresentation);
        assertTrue(actualCaseData.getCaseInvites().isEmpty());
    }

    @Test
    public void noCitizenApplicantsInvitesGeneratedAndSentWhenLdFlagIsTurnedOff() {
        when(launchDarklyClient.isFeatureEnabled("generate-ca-citizen-applicant-pin")).thenReturn(false);

        CaseData actualCaseData = c100CaseInviteService
            .generateAndSendCaseInvite(citizenCaseDataWithApplicantEmail);
        assertTrue(actualCaseData.getCaseInvites().stream().noneMatch(t -> YesOrNo.Yes.equals(t.getValue().getIsApplicant())));
    }

    @Test
    public void noCitizenApplicantsInvitesGeneratedAndSentForCaseCreatedBySolicitor() {
        when(launchDarklyClient.isFeatureEnabled("generate-ca-citizen-applicant-pin")).thenReturn(true);

        CaseData actualCaseData = c100CaseInviteService
            .generateAndSendCaseInvite(solicitorCaseDataWithApplicantEmail);
        assertTrue(actualCaseData.getCaseInvites().stream().noneMatch(t -> YesOrNo.Yes.equals(t.getValue().getIsApplicant())));
    }

    @Test
    public void generateApplicantsInvitesAndSentForCaseCreatedByCitizen() {
        when(launchDarklyClient.isFeatureEnabled("generate-ca-citizen-applicant-pin")).thenReturn(true);

        CaseData actualCaseData = c100CaseInviteService
            .generateAndSendCaseInvite(citizenCaseDataWithApplicantEmail);
        List<Element<CaseInvite>> applicantCaseInvites = actualCaseData.getCaseInvites().stream()
                .filter(t -> YesOrNo.Yes.equals(t.getValue().getIsApplicant())).collect(Collectors.toList());
        assertEquals(1, applicantCaseInvites.size());
        assertEquals(YesOrNo.Yes, applicantCaseInvites.get(0).getValue().getIsApplicant());
        assertEquals("applicant@email.com", applicantCaseInvites.get(0).getValue()
            .getCaseInviteEmail());
    }
}
