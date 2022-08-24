package uk.gov.hmcts.reform.prl.services;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.pin.C100CaseInviteService;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.services.pin.FL401CaseInviteService;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
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

    @Before
    public void init() {
        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(PartyDetails.builder()
                                            .email("abc1@de.com")
                                            .representativeLastName("LastName")
                                            .representativeFirstName("FirstName")
                                            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
                                            .build())))
            .respondents(List.of(element(PartyDetails.builder()
                                             .email("abc2@de.com")
                                             .representativeLastName("LastName")
                                             .representativeFirstName("FirstName")
                                             .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
                                             .build())))
            .build();
        CaseInvite caseInvite1 = new CaseInvite("abc1@de.com", "ABCD1234", "abc1",
                                                UUID.randomUUID());
        CaseInvite caseInvite2 = new CaseInvite("abc2@de.com", "WXYZ5678", "abc2",
                                                UUID.randomUUID());
        List<Element<CaseInvite>> caseInvites = List.of(element(caseInvite1), element(caseInvite2));

        CaseDetails caseDetails = CaseDetails.builder().build();
        when(launchDarklyClient.isFeatureEnabled("generate-pin")).thenReturn(true);
        when(c100CaseInviteService.generateAndSendRespondentCaseInvite(any()))
            .thenReturn(CaseData.builder().caseInvites(caseInvites).build());
        when(fl401CaseInviteService.generateAndSendRespondentCaseInvite(any()))
            .thenReturn(CaseData.builder().caseInvites(caseInvites).build());
    }

    @Test
    public void testGeneratePinAndNotificationEmailForC100() throws Exception {

        CaseData actualCaseData = caseInviteManager.generatePinAndSendNotificationEmail(caseData);

        assertEquals(2, actualCaseData.getCaseInvites().size());
        assertEquals("abc1@de.com", actualCaseData.getCaseInvites().get(0).getValue()
            .getCaseInviteEmail());
        assertEquals("abc2@de.com", actualCaseData.getCaseInvites().get(1).getValue()
            .getCaseInviteEmail());

    }

    @Test
    public void testGeneratePinAndNotificationEmailForFL401() throws Exception {

        CaseData actualCaseData = caseInviteManager.generatePinAndSendNotificationEmail(caseData.toBuilder().caseTypeOfApplication(
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
}
