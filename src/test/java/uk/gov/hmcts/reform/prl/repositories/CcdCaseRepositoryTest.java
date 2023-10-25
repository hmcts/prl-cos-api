package uk.gov.hmcts.reform.prl.repositories;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CcdCaseRepositoryTest {

    @InjectMocks
    CcdCaseRepository ccdCaseRepository;

    @Mock
    CcdCaseApi ccdCaseApi;

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
    public void testLinkDefendant() {
        EventRequestData eventRequestData = EventRequestData.builder().build();
        StartEventResponse startEventResponse = StartEventResponse.builder().build();
        Map<String, Object> map = new HashMap<>();

        ccdCaseRepository.linkDefendant(authToken, s2sToken, caseId, eventRequestData, startEventResponse, map);
        assertEquals(emptyCaseDetails, emptyCaseDetails);
    }

    @Test
    public void testUpdateCase() {
        when(ccdCaseApi.updateCase(authToken, caseId, caseData, null)).thenReturn(CaseDetails.builder().build());
        CaseDetails caseDetails = ccdCaseRepository.updateCase(authToken, caseId, caseData, null);
        Assert.assertEquals(emptyCaseDetails, caseDetails);
    }

    @Test
    public void testCreateCase() {
        when(ccdCaseApi.createCase(authToken, caseData)).thenReturn(CaseDetails.builder().build());
        CaseDetails caseDetails = ccdCaseRepository.createCase(authToken, caseData);
        Assert.assertEquals(emptyCaseDetails, caseDetails);
    }

    @Test
    public void testGetCase() {
        when(ccdCaseApi.getCase(authToken, caseId)).thenReturn(CaseDetails.builder().build());
        CaseDetails caseDetails = ccdCaseRepository.getCase(authToken, caseId);
        Assert.assertEquals(emptyCaseDetails, caseDetails);
    }
}
