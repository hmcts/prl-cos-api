package uk.gov.hmcts.reform.prl.services.noticeofchange;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.CaseAssignmentApi;
import uk.gov.hmcts.reform.prl.request.RequestData;

import static uk.gov.hmcts.reform.prl.models.noticeofchange.DecisionRequest.decisionRequest;


@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseAssignmentService {

    private final RequestData requestData;
    private final AuthTokenGenerator tokenGenerator;
    private final CaseAssignmentApi caseAssignmentApi;




    public AboutToStartOrSubmitCallbackResponse applyDecision(CaseDetails caseDetails) {
        return applyDecision(caseDetails, requestData.authorisation());
    }

    private AboutToStartOrSubmitCallbackResponse applyDecision(CaseDetails caseDetails, String userToken) {
        return caseAssignmentApi.applyDecision(
            userToken,
            tokenGenerator.generate(),
            decisionRequest(caseDetails));
    }

}
