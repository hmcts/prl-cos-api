package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CourtFinderControllerTest {

    @InjectMocks
    CourtFinderController courtFinderController;

    @Mock
    CourtFinderService courtFinderService;

    @Mock
    CallbackResponse callbackResponse;

    @Mock
    ObjectMapper objectMapper;

    @Before
    public void setUp() {

        CaseData caseData = CaseData.builder()
            .court(Court.builder().build())
            .build();

        callbackResponse = CallbackResponse.builder()
            .data(caseData)
            .build();

    }

    @Test
    public void testCallBackResponseContainsCourtData() throws NotFoundException {

        CaseData caseData = CaseData.builder().build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .caseData(caseData)
                             .build())
            .build();

        Court court = Court.builder().build();

        caseData.setCourt(court);

        when(courtFinderService.getClosestChildArrangementsCourt(callbackRequest.getCaseDetails().getCaseData()))
            .thenReturn(court);

        when(courtFinderService.setCourtUnlessCourtAlreadyPresent(caseData, court)).thenReturn(caseData);

        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getCaseData(), CaseData.class))
            .thenReturn(caseData);

        CallbackResponse response = courtFinderController.getChildArrangementsCourtAndAddToCaseData(callbackRequest);

        Assert.assertNotNull(response.getData().getCourt());

    }


    @Test
    public void verifyInteractionWithCourtFinderService() throws NotFoundException {

        CaseData caseData = CaseData.builder().build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .caseData(caseData)
                             .build())
            .build();

        Court court = Court.builder().build();

        when(courtFinderService.getClosestChildArrangementsCourt(callbackRequest.getCaseDetails().getCaseData()))
            .thenReturn(court);

        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getCaseData(), CaseData.class))
            .thenReturn(caseData);

        when(courtFinderService.setCourtUnlessCourtAlreadyPresent(caseData, court)).thenReturn(caseData);

        courtFinderController.getChildArrangementsCourtAndAddToCaseData(callbackRequest);

        verify(courtFinderService).getClosestChildArrangementsCourt(caseData);
        verify(courtFinderService).setCourtUnlessCourtAlreadyPresent(caseData, court);

    }

}

