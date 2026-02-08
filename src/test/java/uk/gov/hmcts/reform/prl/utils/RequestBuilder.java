package uk.gov.hmcts.reform.prl.utils;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.utils.TestConstants.ID;

public class RequestBuilder {



    public static CallbackRequest buildCallbackRequest() {
        Map<String, Object> data = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().data(data).id(ID).build();
        return CallbackRequest.builder().caseDetails(caseDetails).caseDetailsBefore(caseDetails).build();
    }
}
