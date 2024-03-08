package uk.gov.hmcts.reform.prl.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_UPDATE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.LINK_CITIZEN;

@RunWith(MockitoJUnitRunner.class)
public class CcdCaseRepositoryTest {

    @InjectMocks
    CcdCaseRepository ccdCaseRepository;

    @InjectMocks
    CaseService caseService;

    @Mock
    CcdCaseApi ccdCaseApi;

    @Mock
    CaseRepository caseRepository;

    public static final String authToken = "Bearer TestAuthToken";

    public static final String s2sToken = "s2s AuthToken";

    private String caseId = "";

    private CaseData caseData;

    private CaseDetails emptyCaseDetails;

    @Before
    public void setup() {
        caseId = "test_case";
        caseData = CaseData.builder().build();
        emptyCaseDetails = CaseDetails.builder().build();
    }

    @Test
    @Ignore
    public void testLinkDefendant() throws JsonProcessingException {
        Map<String, Object> map = new HashMap<>();
        EventRequestData eventRequestData = EventRequestData.builder().build();
        StartEventResponse startEventResponse = StartEventResponse.builder().build();

        when(caseRepository.updateCase(authToken, caseId, caseData, LINK_CITIZEN)).thenReturn(emptyCaseDetails);

        ccdCaseRepository.linkDefendant(authToken, s2sToken, caseId, eventRequestData, startEventResponse, map);
        CaseDetails emptyCaseDetails1 = caseService.updateCase(caseData, authToken, s2sToken, caseId, LINK_CITIZEN.getValue(), "test");
        assertEquals(emptyCaseDetails, emptyCaseDetails1);
    }

    @Test
    public void testUpdateCase() {
        when(ccdCaseApi.updateCase(authToken, caseId, caseData, CITIZEN_CASE_UPDATE)).thenReturn(CaseDetails.builder().build());
        CaseDetails caseDetails = ccdCaseRepository.updateCase(authToken, caseId, caseData, CITIZEN_CASE_UPDATE);
        assertEquals(emptyCaseDetails, caseDetails);
    }

    @Test
    public void testUpdateCaseCaseEventNull() {
        when(ccdCaseApi.updateCase(authToken, caseId, caseData, null)).thenReturn(null);
        CaseDetails caseDetails = ccdCaseRepository.updateCase(authToken, caseId, caseData, null);
        assertEquals(null, caseDetails);
    }

    @Test
    public void testCreateCase() {
        when(ccdCaseApi.createCase(authToken, caseData)).thenReturn(CaseDetails.builder().build());
        CaseDetails caseDetails = ccdCaseRepository.createCase(authToken, caseData);
        assertEquals(emptyCaseDetails, caseDetails);
    }

    @Test
    public void testGetCase() {
        when(ccdCaseApi.getCase(authToken, caseId)).thenReturn(CaseDetails.builder().build());
        CaseDetails caseDetails = ccdCaseRepository.getCase(authToken, caseId);
        assertEquals(emptyCaseDetails, caseDetails);
    }
}
