package uk.gov.hmcts.reform.prl.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.addcafcassofficer.ChildAndCafcassOfficer;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AddCafcassOfficerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(SpringRunner.class)
public class AddCafcassOfficerControllerTest {

    @Mock
    AddCafcassOfficerService addCafcassOfficerService;

    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    private AddCafcassOfficerController addCafcassOfficerController;

    @Test
    public void testPrePopulateChildDetails() {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        List<Element<ChildAndCafcassOfficer>> childAndCafcassOfficers = new ArrayList<>();
        when(addCafcassOfficerService.prePopulateChildName(caseData)).thenReturn(childAndCafcassOfficers);
        addCafcassOfficerController.prePopulateChildDetails(callbackRequest);
        assertTrue(stringObjectMap.containsKey("childAndCafcassOfficers"));
        assertEquals(childAndCafcassOfficers, stringObjectMap.get("childAndCafcassOfficers"));
    }

    @Test
    public void testUpdateChildDetailsWithCafcassOfficer() {
        List<Element<ChildAndCafcassOfficer>> childAndCafcassOfficers = new ArrayList<>();
        Element<ChildAndCafcassOfficer> cafcassOfficerElement = element(ChildAndCafcassOfficer.builder().build());
        childAndCafcassOfficers.add(cafcassOfficerElement);
        CaseData caseData = CaseData.builder()
            .id(123L)
            .childAndCafcassOfficers(childAndCafcassOfficers)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        addCafcassOfficerController.updateChildDetailsWithCafcassOfficer(callbackRequest);
        verify(addCafcassOfficerService, times(1))
            .populateCafcassOfficerDetails(caseData, stringObjectMap, cafcassOfficerElement);
    }
}
