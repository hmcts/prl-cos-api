package uk.gov.hmcts.reform.prl.services.cafcass;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.filter.cafcaas.CafCassFilter;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CafcassDateTimeUpdateHelperTest {

    @Mock
    private HearingService hearingService;

    @Mock
    private SystemUserService systemUserService;

    private CafcassDateTimeUpdateHelper cafcassDateTimeUpdateHelper;

    @BeforeEach
    void setUp() {
        cafcassDateTimeUpdateHelper = new CafcassDateTimeUpdateHelper(
            new CafCassFilter(),
            hearingService,
            systemUserService,
            CcdObjectMapper.getObjectMapper()
        );
        when(systemUserService.getSysUserToken()).thenReturn("authorisation");
        when(hearingService.getHearingsForAllCases(anyString(), anyMap())).thenReturn(Collections.emptyList());
    }

    @Test
    void shouldReturnFalseWhenOnlyEmptyCafcassElementsAreFilteredOut() {
        Map<String, Object> documentWithoutValue = new HashMap<>();
        documentWithoutValue.put("id", "00000000-0000-0000-0000-000000000001");
        documentWithoutValue.put("value", null);

        Map<String, Object> caseData = new HashMap<>();
        caseData.put("caseManagementLocation", caseManagementLocation());
        caseData.put("otherDocuments", List.of(documentWithoutValue));

        Map<String, Object> caseDataBefore = new HashMap<>();
        caseDataBefore.put("caseManagementLocation", caseManagementLocation());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .data(caseData)
            .build();
        CaseDetails caseDetailsBefore = CaseDetails.builder()
            .id(123L)
            .data(caseDataBefore)
            .build();

        assertFalse(cafcassDateTimeUpdateHelper.hasCafcassCaseDataChanged(caseDetails, caseDetailsBefore));
    }

    @Test
    void shouldReturnTrueWhenCafcassCaseDataHasChanged() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("caseManagementLocation", caseManagementLocation());
        caseData.put("caseTypeOfApplication", "FL401");

        Map<String, Object> caseDataBefore = new HashMap<>();
        caseDataBefore.put("caseManagementLocation", caseManagementLocation());
        caseDataBefore.put("caseTypeOfApplication", "C100");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .data(caseData)
            .build();
        CaseDetails caseDetailsBefore = CaseDetails.builder()
            .id(123L)
            .data(caseDataBefore)
            .build();

        assertTrue(cafcassDateTimeUpdateHelper.hasCafcassCaseDataChanged(caseDetails, caseDetailsBefore));
    }

    private Map<String, Object> caseManagementLocation() {
        Map<String, Object> caseManagementLocation = new HashMap<>();
        caseManagementLocation.put("regionId", "1");
        caseManagementLocation.put("baseLocationId", "123456");
        return caseManagementLocation;
    }
}
