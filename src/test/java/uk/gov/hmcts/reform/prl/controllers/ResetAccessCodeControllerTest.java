package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.pin.C100CaseInviteService;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ResetAccessCodeControllerTest {


    @InjectMocks
    private ResetAccessCodeController resetAccessCodeController;

    @Mock
    private CaseInviteManager caseInviteManager;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private C100CaseInviteService c100CaseInviteService;

    private CaseData caseData1;
    private Map<String, Object> caseData;

    @Before
    public void init() {

        caseData = new HashMap<>();
        caseData.put("id", 12345L);
        caseData.put("caseTypeOfApplication", "C100");
        caseData.put("applicants", List.of(element(PartyDetails.builder()
                                                       .email("abc1@de.com")
                                                       .representativeLastName("LastName")
                                                       .representativeFirstName("FirstName")
                                                       .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
                                                       .build())));
        caseData.put("respondents", List.of(element(PartyDetails.builder()
                                                        .email("abc2@de.com")
                                                        .representativeLastName("LastName")
                                                        .representativeFirstName("FirstName")
                                                        .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
                                                        .build())));


        caseData1 = CaseData.builder()
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
        CaseInvite caseInvite1 = new CaseInvite("abc1@de.com", "ABCD1234", "abc1", UUID.randomUUID());
        CaseInvite caseInvite2 = new CaseInvite("abc2@de.com", "WXYZ5678", "abc2", UUID.randomUUID());
        List<Element<CaseInvite>> respondentCaseInvites = List.of(element(caseInvite1), element(caseInvite2));

        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData1);
        when(caseInviteManager.reGeneratePinAndSendNotificationEmail(any())).thenReturn(CaseData.builder().respondentCaseInvites(
            respondentCaseInvites).build());

    }

    @Test
    public void testResetAccessCodeFlow() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();

        CaseInvite caseInvite1 = new CaseInvite("abc1@de.com", "ABCD1234", "abc1", UUID.randomUUID());
        CaseInvite caseInvite2 = new CaseInvite("abc2@de.com", "WXYZ5678", "abc2", UUID.randomUUID());
        List<Element<CaseInvite>> respondentCaseInvites = List.of(element(caseInvite1), element(caseInvite2));

        AboutToStartOrSubmitCallbackResponse response = resetAccessCodeController
            .resetPin(callbackRequest);
        assertTrue(response.getData().containsKey("respondentCaseInvites"));
    }
}
