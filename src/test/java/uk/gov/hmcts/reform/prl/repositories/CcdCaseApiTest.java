package uk.gov.hmcts.reform.prl.repositories;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenCoreCaseDataService;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class CcdCaseApiTest {


    @InjectMocks
    CcdCaseApi ccdCaseApi;

    @Mock
    CitizenCoreCaseDataService citizenCoreCaseDataService;

    @Mock
    IdamClient idamClient;

    @Mock
    CaseAccessApi caseAccessApi;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Mock
    StartEventResponse startEventResponse;

    @Mock
    EventRequestData eventRequestData;

    private static final String AUTH = "auth";

    @Test
    public void testCreateCase() {
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder()
                              .amendOrderDynamicList(DynamicList.builder()
                                                         .value(DynamicListElement.builder()
                                                                    .code(UUID.randomUUID())
                                                                    .build()).build()).build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder().caseTypeId("dsd").build();
        when(citizenCoreCaseDataService.createCase(AUTH, caseData)).thenReturn(caseDetails);
        assertEquals(caseDetails.getCaseTypeId(), ccdCaseApi.createCase(AUTH, caseData).getCaseTypeId());
    }

    @Test
    public void testUpdateCase() {
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder()
                              .amendOrderDynamicList(DynamicList.builder()
                                                         .value(DynamicListElement.builder()
                                                                    .code(UUID.randomUUID())
                                                                    .build()).build()).build())
            .build();

        CaseDetails caseDetails = CaseDetails.builder().caseTypeId("dsd").build();
        when(citizenCoreCaseDataService.updateCase(AUTH, Long.valueOf(12345), caseData,
                                                   CaseEvent.CITIZEN_CASE_UPDATE
        )).thenReturn(caseDetails);
        CaseDetails expectedResponse = ccdCaseApi.updateCase(AUTH, "12345", caseData, CaseEvent.CITIZEN_CASE_UPDATE);
        assertEquals(caseDetails.getCaseTypeId(), expectedResponse.getCaseTypeId());
    }

    @Test
    public void testGetCase() {

        CaseDetails caseDetails = CaseDetails.builder().caseTypeId("dsd").build();
        when(citizenCoreCaseDataService.getCase(AUTH, "12345")).thenReturn(caseDetails);
        CaseDetails expectedResponse = ccdCaseApi.getCase(AUTH, "12345");
        assertEquals(caseDetails.getCaseTypeId(), expectedResponse.getCaseTypeId());
    }
}
