package uk.gov.hmcts.reform.prl.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.framework.context.TaskContext;
import uk.gov.hmcts.reform.prl.framework.exceptions.TaskException;
import uk.gov.hmcts.reform.prl.framework.task.Task;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WorkflowResult;
import uk.gov.hmcts.reform.prl.request.RequestData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import static uk.gov.hmcts.reform.prl.enums.OrchestrationConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.OrchestrationConstants.JURISDICTION;

@Component
public class ServiceRequestUpdateTask implements Task<WorkflowResult> {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;
    @Autowired
    private RequestData requestData;

    @Autowired
    private SystemUserService systemUserService;


    @Override
    public WorkflowResult execute(TaskContext context, WorkflowResult payload) throws TaskException {

        CaseData caseData = objectMapper.convertValue(payload.getCaseData(), CaseData.class);

        String userToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(userToken);
        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            userToken,
            authTokenGenerator.generate(),
            systemUpdateUserId,
            JURISDICTION,
            CASE_TYPE,
            "1638976581389540",
            "paymentCallback");

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .build())
            .data(payload.getCaseData().put("paymentServiceRequestReferenceNumber",
                                            "111sdfsd11111"))
            .build();

        coreCaseDataApi.submitEventForCaseWorker(
            userToken,
            authTokenGenerator.generate(),
            systemUpdateUserId,
            JURISDICTION,
            CASE_TYPE,
            "1638976581389540",
            true,
            caseDataContent);
        return payload;
    }
}

