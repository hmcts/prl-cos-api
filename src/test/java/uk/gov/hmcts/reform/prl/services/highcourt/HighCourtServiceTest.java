package uk.gov.hmcts.reform.prl.services.highcourt;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;

import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class HighCourtServiceTest {

    @InjectMocks
    private HighCourtService highCourtService;

    @Mock
    private RoleAssignmentService roleAssignmentService;

    private CaseDetails caseDetails;

    @Test
    public void shouldAddAccessWhenIsHighCourtCaseIsYes() {
        Map caseDataMap = new HashMap<>();
        caseDataMap.put("id", 12345L);
        caseDataMap.put("caseTypeOfApplication", "C100");
        caseDataMap.put("isHighCourtCase", "Yes");

        caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(caseDataMap)
            .build();

        highCourtService.setCaseAccess(caseDetails);
        // then verify role assignment
    }

    @Test
    public void shouldAddAccessWhenIsHighCourtCaseIsNo() {
        Map caseDataMap = new HashMap<>();
        caseDataMap.put("id", 12345L);
        caseDataMap.put("caseTypeOfApplication", "C100");
        caseDataMap.put("isHighCourtCase", "No");

        caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(caseDataMap)
            .build();

        highCourtService.setCaseAccess(caseDetails);
        // then verify role assignment (removal)
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenIsHighCourtCaseIsNull() {
        Map caseDataMap = new HashMap<>();
        caseDataMap.put("id", 12345L);
        caseDataMap.put("caseTypeOfApplication", "C100");
        caseDataMap.put("isHighCourtCase", null);

        caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(caseDataMap)
            .build();

        highCourtService.setCaseAccess(caseDetails);
    }
}
