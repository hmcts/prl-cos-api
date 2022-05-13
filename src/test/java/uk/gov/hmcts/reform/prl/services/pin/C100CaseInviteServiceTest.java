package uk.gov.hmcts.reform.prl.services.pin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.class)
public class C100CaseInviteServiceTest {


    @InjectMocks
    C100CaseInviteService c100CaseInviteService;

    @Mock
    CaseInviteEmailService caseInviteEmailService;

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

        PartyDetails respondentTwoWithEmail = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("Two")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondentTwo@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        List<Element<PartyDetails>> respondentsWithEmailsNoRepresentation = List.of(element(respondentOneWithEmail), element(respondentTwoWithEmail));

        caseDataWithRespondentsAndEmailsNoRepresentation = CaseData.builder()
            .caseTypeOfApplication("C100")
            .respondents(respondentsWithEmailsNoRepresentation).build();

        PartyDetails respondentTwoWithEmailAndRepresentation = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("Two")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondentTwo@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        List<Element<PartyDetails>> respondentsWithEmailsOneNoRepresentation = List.of(element(respondentOneWithEmail), element(respondentTwoWithEmailAndRepresentation));


        caseDataWithRespondentsAndEmailsOnePartyNoRepresentation = CaseData.builder()
            .caseTypeOfApplication("C100")
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

        List<Element<PartyDetails>> respondentsNoEmailsNoRepresentation = List.of(element(respondentOneNoEmail), element(respondentTwoNoEmail));

        getCaseDataWithRespondentsNoEmails = CaseData.builder()
            .caseTypeOfApplication("C100")
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

        List<Element<PartyDetails>> respondentsWithEmailsAndRepresentation = List.of(element(respondentOneWithEmailAndRepresentation), element(secondRespondentWithEmailAndRepresentation));

        caseDataWithRespondentsAllWithRepresentation = CaseData.builder()
            .caseTypeOfApplication("C100")
            .respondents(respondentsWithEmailsAndRepresentation).build();


        caseDataNoRespondents = CaseData.builder()
            .caseTypeOfApplication("C100")
            .build();


    }

    @Test
    public void givenRespondentsWithNoRepresentation_whenCaseInvitesGenerated_thenSentToAllRespondentsAndStoredInCaseData() {
        CaseData actualCaseData = c100CaseInviteService.generateAndSendRespondentCaseInvite(caseDataWithRespondentsAndEmailsNoRepresentation);

        //case invite for both respondents
        assertEquals(2, actualCaseData.getRespondentCaseInvites().size());
        assertEquals("respondentOne@email.com", actualCaseData.getRespondentCaseInvites().get(0).getValue().getCaseInviteEmail());
        assertEquals("respondentTwo@email.com", actualCaseData.getRespondentCaseInvites().get(1).getValue().getCaseInviteEmail());
    }

    @Test
    public void givenRespondentWithRepresentation_whenCaseInvitesGenerated_thenSentToOnlyThoseWithoutRepresentation() {
        CaseData actualCaseData = c100CaseInviteService.generateAndSendRespondentCaseInvite(caseDataWithRespondentsAndEmailsOnePartyNoRepresentation);

        //two respondents but only one should have a case invite generated
        assertEquals(1, actualCaseData.getRespondentCaseInvites().size());
        assertEquals("respondentOne@email.com", actualCaseData.getRespondentCaseInvites().get(0).getValue().getCaseInviteEmail());
    }

    @Test
    public void givenMultipleRespondentsWithNoEmail_whenCaseInvitesGenerated_thenNoRespondentsReceiveInvite() {
        CaseData actualCaseData = c100CaseInviteService.generateAndSendRespondentCaseInvite(getCaseDataWithRespondentsNoEmails);
        assertTrue(actualCaseData.getRespondentCaseInvites().isEmpty());
    }

    @Test
    public void givenMultipleRespondentsWithEmailAndRepresentation_whenCaseInvitesGenerated_thenNoRespondentsReceiveInvite() {
        CaseData actualCaseData = c100CaseInviteService.generateAndSendRespondentCaseInvite(caseDataWithRespondentsAllWithRepresentation);
        assertTrue(actualCaseData.getRespondentCaseInvites().isEmpty());
    }

    @Test
    public void givenNoRespondents_whenCaseInvitesGenerated_thenNoInvitesGenerated() {
        CaseData actualCaseData = c100CaseInviteService.generateAndSendRespondentCaseInvite(caseDataWithRespondentsAllWithRepresentation);
        assertTrue(actualCaseData.getRespondentCaseInvites().isEmpty());
    }


}
