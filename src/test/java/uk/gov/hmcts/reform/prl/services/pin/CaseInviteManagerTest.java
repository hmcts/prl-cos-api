package uk.gov.hmcts.reform.prl.services.pin;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseInviteManagerTest {

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @Mock
    private C100CaseInviteService c100CaseInviteService;

    @Mock
    private FL401CaseInviteService fl401CaseInviteService;

    @InjectMocks
    private CaseInviteManager caseInviteManager;

    private CaseData caseData;

    private PartyDetails applicantPartyDetails;

    private PartyDetails respondentPartyDetails;

    @Before
    public void init() {
        applicantPartyDetails = PartyDetails.builder()
            .email("abc1@de.com")
            .representativeLastName("LastName")
            .representativeFirstName("FirstName")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        respondentPartyDetails = PartyDetails.builder()
            .email("abc2@de.com")
            .representativeLastName("LastName")
            .representativeFirstName("FirstName")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(applicantPartyDetails)))
            .respondents(List.of(element(respondentPartyDetails)))
            .build();
        CaseInvite caseInvite1 = new CaseInvite("abc1@de.com", "ABCD1234", "abc1",
                                                UUID.randomUUID(), YesOrNo.Yes
        );
        CaseInvite caseInvite2 = new CaseInvite("abc2@de.com", "WXYZ5678", "abc2",
                                                UUID.randomUUID(), YesOrNo.No
        );
        List<Element<CaseInvite>> caseInvites = List.of(element(caseInvite1), element(caseInvite2));

        CaseDetails caseDetails = CaseDetails.builder().build();
        when(launchDarklyClient.isFeatureEnabled("generate-pin")).thenReturn(true);
        when(c100CaseInviteService.sendCaseInviteEmail(any()))
            .thenReturn(CaseData.builder().caseInvites(caseInvites).build());
        when(fl401CaseInviteService.sendCaseInviteEmail(any()))
            .thenReturn(CaseData.builder().caseInvites(caseInvites).build());
        when(c100CaseInviteService.generateCaseInvite(any(), any()))
            .thenReturn(caseInvite1);
        when(fl401CaseInviteService.generateCaseInvite(any(), any()))
            .thenReturn(caseInvite2);
    }

    @Test
    public void testGeneratePinAndNotificationEmailForC100() throws Exception {

        CaseData actualCaseData = caseInviteManager.sendAccessCodeNotificationEmail(caseData);

        assertEquals(2, actualCaseData.getCaseInvites().size());
        assertEquals("abc1@de.com", actualCaseData.getCaseInvites().get(0).getValue()
            .getCaseInviteEmail());
        assertEquals("abc2@de.com", actualCaseData.getCaseInvites().get(1).getValue()
            .getCaseInviteEmail());

    }

    @Test
    public void testGeneratePinAndNotificationEmailForFL401() throws Exception {

        CaseData actualCaseData = caseInviteManager.sendAccessCodeNotificationEmail(caseData.toBuilder().caseTypeOfApplication(
            "FL401").build());

        assertEquals(2, actualCaseData.getCaseInvites().size());
        assertEquals("abc1@de.com", actualCaseData.getCaseInvites().get(0).getValue()
            .getCaseInviteEmail());
        assertEquals("abc2@de.com", actualCaseData.getCaseInvites().get(1).getValue()
            .getCaseInviteEmail());

    }

    @Test
    public void testReGeneratePinAndNotificationEmailForC100() throws Exception {

        CaseData actualCaseData = caseInviteManager.reGeneratePinAndSendNotificationEmail(caseData);

        assertEquals(2, actualCaseData.getCaseInvites().size());
        assertEquals("abc1@de.com", actualCaseData.getCaseInvites().get(0).getValue()
            .getCaseInviteEmail());
        assertEquals("abc2@de.com", actualCaseData.getCaseInvites().get(1).getValue()
            .getCaseInviteEmail());

    }

    @Test
    public void testReGeneratePinAndNotificationEmailForFL401() throws Exception {
        CaseData actualCaseData = caseInviteManager.reGeneratePinAndSendNotificationEmail(caseData.toBuilder().caseTypeOfApplication(
            "FL401").build());

        assertEquals(2, actualCaseData.getCaseInvites().size());
        assertEquals("abc1@de.com", actualCaseData.getCaseInvites().get(0).getValue()
            .getCaseInviteEmail());
        assertEquals("abc2@de.com", actualCaseData.getCaseInvites().get(1).getValue()
            .getCaseInviteEmail());

    }

    @Test
    public void testGeneratePinAfterLegalRepresentationRemovedForC100Applicant() {
        when(launchDarklyClient.isFeatureEnabled("generate-ca-citizen-applicant-pin")).thenReturn(true);
        caseData = caseData.toBuilder().caseCreatedBy(CaseCreatedBy.CITIZEN).build();
        CaseInvite caseInvite = caseInviteManager.generatePinAfterLegalRepresentationRemoved(element(applicantPartyDetails),
                                                                                               SolicitorRole.C100APPLICANTSOLICITOR1);
        assertNotNull(caseInvite);
        assertEquals("ABCD1234", caseInvite.getAccessCode());
    }

    @Test
    public void testGeneratePinAfterLegalRepresentationRemovedForC100Respondent() {
        caseData = caseData.toBuilder().caseCreatedBy(CaseCreatedBy.CITIZEN).build();
        CaseInvite caseInvite = caseInviteManager.generatePinAfterLegalRepresentationRemoved(element(respondentPartyDetails),
                                                                                             SolicitorRole.C100RESPONDENTSOLICITOR1);
        assertNotNull(caseInvite);
        assertEquals("ABCD1234", caseInvite.getAccessCode());
    }

    @Test
    public void testGeneratePinAfterLegalRepresentationRemovedForFL401Respondent() {
        caseData = caseData.toBuilder().caseCreatedBy(CaseCreatedBy.CITIZEN).build();
        CaseInvite caseInvite = caseInviteManager.generatePinAfterLegalRepresentationRemoved(element(respondentPartyDetails),
                                                                                             SolicitorRole.FL401RESPONDENTSOLICITOR);
        assertNotNull(caseInvite);
        assertEquals("WXYZ5678", caseInvite.getAccessCode());
    }

    @Test
    public void testGeneratePinAfterLegalRepresentationRemovedForFL401Applicant() {
        when(launchDarklyClient.isFeatureEnabled("generate-da-citizen-applicant-pin")).thenReturn(true);
        caseData = caseData.toBuilder().caseCreatedBy(CaseCreatedBy.CITIZEN).build();
        CaseInvite caseInvite = caseInviteManager.generatePinAfterLegalRepresentationRemoved(element(applicantPartyDetails),
                                                                                             SolicitorRole.FL401APPLICANTSOLICITOR);
        assertNotNull(caseInvite);
        assertEquals("WXYZ5678", caseInvite.getAccessCode());
    }
}
