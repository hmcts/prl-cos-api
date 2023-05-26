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
import uk.gov.hmcts.reform.prl.services.AuthorisationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(SpringRunner.class)
public class AddCafcassOfficerControllerTest {

    @Mock
    AddCafcassOfficerService addCafcassOfficerService;

    @InjectMocks
    private AddCafcassOfficerController addCafcassOfficerController;

    @Mock
    private AuthorisationService authorisationService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

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
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        addCafcassOfficerController.updateChildDetailsWithCafcassOfficer(s2sToken, authToken, callbackRequest);
        verify(addCafcassOfficerService, times(1))
            .populateCafcassOfficerDetails(callbackRequest);
    }
}
