package uk.gov.hmcts.reform.prl.repositories;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
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
        when(citizenCoreCaseDataService.createCase(AUTH,caseData)).thenReturn(caseDetails);
        assertEquals(caseDetails.getCaseTypeId(),ccdCaseApi.createCase(AUTH,caseData).getCaseTypeId());
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
        when(citizenCoreCaseDataService.updateCase(
            Long.valueOf(12345),
            CaseEvent.CITIZEN_CASE_UPDATE)).thenReturn(caseDetails);
        CaseDetails expectedResponse = ccdCaseApi.updateCase(AUTH,"12345",caseData,CaseEvent.CITIZEN_CASE_UPDATE);
        assertEquals(caseDetails.getCaseTypeId(),expectedResponse.getCaseTypeId());
    }

    @Test
    public void testGetCase() {

        CaseDetails caseDetails = CaseDetails.builder().caseTypeId("dsd").build();
        when(citizenCoreCaseDataService.getCase(AUTH, "12345")).thenReturn(caseDetails);
        CaseDetails expectedResponse = ccdCaseApi.getCase(AUTH,"12345");
        assertEquals(caseDetails.getCaseTypeId(),expectedResponse.getCaseTypeId());
    }

    @Test
    public void testLinkCitizen() {

        UserDetails userDetails = UserDetails.builder()
            .forename("solicitor@example.com")
            .surname("Solicitor")
            .id("testId")
            .build();
        CaseDetails caseDetails = CaseDetails.builder().caseTypeId("12345").build();
        when(idamClient.getUserDetails(AUTH)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(AUTH);

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder()
                              .amendOrderDynamicList(DynamicList.builder()
                                                         .value(DynamicListElement.builder()
                                                                    .code(UUID.randomUUID())
                                                                    .build()).build()).build())
            .build();
        when(citizenCoreCaseDataService.linkDefendant(Long.valueOf("12345"),
                                                      CaseEvent.LINK_CITIZEN)).thenReturn(caseDetails);
        ccdCaseApi.linkCitizenToCase(AUTH,AUTH,"12345",caseData);
        assertEquals(caseDetails.getCaseTypeId(),caseDetails.getCaseTypeId());
    }

}
