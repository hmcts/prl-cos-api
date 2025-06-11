package uk.gov.hmcts.reform.prl.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_UPDATE;

@ExtendWith(MockitoExtension.class)
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

    @BeforeEach
    public void setup() {
        caseId = "test_case";
        caseData = CaseData.builder().build();
        emptyCaseDetails = CaseDetails.builder().build();
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
