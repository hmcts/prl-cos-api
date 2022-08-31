package uk.gov.hmcts.reform.prl.services.pin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


class CaseInviteManagerTest {
    @Mock
    LaunchDarklyClient launchDarklyClient;
    @Mock
    C100CaseInviteService c100CaseInviteService;
    @Mock
    FL401CaseInviteService fl401CaseInviteService;
    @Mock
    Logger log;
    @InjectMocks
    CaseInviteManager caseInviteManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    public CaseInviteManagerTest(){

    }

    @Test
    void testGeneratePinAndSendNotificationEmailFL401() {
        PartyDetails respondentOneWithEmail = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondentOne@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.dontKnow)
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .respondentsFL401(respondentOneWithEmail).build();

        CaseData result = caseInviteManager.generatePinAndSendNotificationEmail(caseData);
        verify(fl401CaseInviteService, times(1))
            .generateAndSendRespondentCaseInvite(caseData);
    }

    @Test
    void testGeneratePinAndSendNotificationEmailC100() {
        PartyDetails respondentOneWithEmail = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondentOne@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.dontKnow)
            .build();

        List<Element<PartyDetails>> respondentsWithEmailsNoRepresentation = List.of(element(respondentOneWithEmail)
                                                                                    );
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .respondents(respondentsWithEmailsNoRepresentation).build();

        CaseData result = caseInviteManager.generatePinAndSendNotificationEmail(caseData);

        verify(c100CaseInviteService, times(1))
            .generateAndSendRespondentCaseInvite(caseData);
    }

    @Test
    void testreGeneratePinAndSendNotificationEmailC100() {
        PartyDetails respondentOneWithEmail = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondentOne@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.dontKnow)
            .build();

        List<Element<PartyDetails>> respondentsWithEmailsNoRepresentation = List.of(element(respondentOneWithEmail)
        );
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .respondents(respondentsWithEmailsNoRepresentation).build();

        CaseData result = caseInviteManager.reGeneratePinAndSendNotificationEmail(caseData);

        verify(c100CaseInviteService, times(1))
            .generateAndSendRespondentCaseInvite(caseData);
    }

    @Test
    void testreGeneratePinAndSendNotificationEmailFL401() {
        PartyDetails respondentOneWithEmail = PartyDetails.builder()
            .firstName("Respondent")
            .lastName("One")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondentOne@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.dontKnow)
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .respondentsFL401(respondentOneWithEmail).build();

        CaseData result = caseInviteManager.reGeneratePinAndSendNotificationEmail(caseData);
        verify(fl401CaseInviteService, times(1))
            .generateAndSendRespondentCaseInvite(caseData);
    }
}
