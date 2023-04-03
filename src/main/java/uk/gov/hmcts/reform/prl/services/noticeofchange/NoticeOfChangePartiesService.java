package uk.gov.hmcts.reform.prl.services.noticeofchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.noticeofchange.NoticeOfChangeParties;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessClient;
import uk.gov.hmcts.reform.prl.utils.noticeofchange.NoticeOfChangePartiesConverter;
import uk.gov.hmcts.reform.prl.utils.noticeofchange.RespondentPolicyConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.RESPONDENT;
import static uk.gov.hmcts.reform.prl.models.noticeofchange.DecisionRequest.decisionRequest;
import static uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService.NoticeOfChangeAnswersPopulationStrategy.BLANK;
import static uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService.NoticeOfChangeAnswersPopulationStrategy.POPULATE;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getCaseData;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class NoticeOfChangePartiesService {
    public final NoticeOfChangePartiesConverter partiesConverter;
    public final RespondentPolicyConverter policyConverter;
    private final AuthTokenGenerator tokenGenerator;
    private final AssignCaseAccessClient assignCaseAccessClient;
    private final ObjectMapper objectMapper;

    public Map<String, Object> generate(CaseData caseData, SolicitorRole.Representing representing) {
        return generate(caseData, representing, POPULATE);
    }

    public Map<String, Object> generate(CaseData caseData, SolicitorRole.Representing representing,
                                        NoticeOfChangeAnswersPopulationStrategy strategy) {
        Map<String, Object> data = new HashMap<>();
        log.info("Inside NoticeOfChangePartiesService: generate");
        List<Element<PartyDetails>> elements = representing.getTarget().apply(caseData);
        log.info("representing.getTarget().apply(caseData) ==> " + elements);
        int numElements = null != elements ? elements.size() : 0;

        List<SolicitorRole> solicitorRoles = SolicitorRole.values(representing);
        for (int i = 0; i < solicitorRoles.size(); i++) {
            SolicitorRole solicitorRole = solicitorRoles.get(i);
            log.info("solicitorRole" + i + " ==> " + solicitorRole);
            if (null != elements) {
                Optional<Element<PartyDetails>> solicitorContainer = i < numElements
                    ? Optional.of(elements.get(i))
                    : Optional.empty();
                log.info("solicitorContainer ==> " + solicitorContainer);
                OrganisationPolicy organisationPolicy = policyConverter.generate(
                    solicitorRole, solicitorContainer
                );
                log.info("organisationPolicy ==> " + organisationPolicy);
                data.put(String.format(representing.getPolicyFieldTemplate(), i), organisationPolicy);

                Optional<NoticeOfChangeParties> possibleAnswer = populateAnswer(
                    strategy, solicitorContainer
                );
                log.info("possibleAnswer ==> " + possibleAnswer);
                if (possibleAnswer.isPresent()) {
                    data.put(String.format(representing.getNocAnswersTemplate(), i), possibleAnswer.get());
                }

            }
        }
        log.info("Exit NoticeOfChangePartiesService ==> " + data);
        return data;
    }

    private Optional<NoticeOfChangeParties> populateAnswer(NoticeOfChangeAnswersPopulationStrategy strategy,
                                                           Optional<Element<PartyDetails>> element) {
        if (BLANK == strategy) {
            return Optional.of(NoticeOfChangeParties.builder().build());
        }
        return element.map(partiesConverter::generateForSubmission);
    }

    public enum NoticeOfChangeAnswersPopulationStrategy {
        POPULATE, BLANK
    }

    public AboutToStartOrSubmitCallbackResponse applyDecision(CaseDetails caseDetails, String userToken) {
        try {
            log.info("applyDecision start json ===>" + objectMapper.writeValueAsString(caseDetails));
        } catch (JsonProcessingException e) {
            log.info("error");
        }

        return assignCaseAccessClient.applyDecision(
            userToken,
            tokenGenerator.generate(),
            decisionRequest(caseDetails));
    }

    public void nocRequestSubmitted(CallbackRequest callbackRequest) {
        CaseData oldCaseData = getCaseData(callbackRequest.getCaseDetailsBefore(), objectMapper);
        CaseData newCaseData = getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        List<Element<PartyDetails>> oldRepresentables = RESPONDENT.getTarget().apply(oldCaseData);
        List<Element<PartyDetails>> currentRepresentables = RESPONDENT.getTarget().apply(newCaseData);

        try {
            log.info("callbackRequest.getCaseDetailsBefore() json ===>" + objectMapper.writeValueAsString(callbackRequest.getCaseDetailsBefore()));
            log.info("callbackRequest.getCaseDetails() json ===>" + objectMapper.writeValueAsString(callbackRequest.getCaseDetails()));
            log.info("oldRepresentables json ===>" + objectMapper.writeValueAsString(oldRepresentables));
            log.info("currentRepresentables json ===>" + objectMapper.writeValueAsString(currentRepresentables));
        } catch (JsonProcessingException e) {
            log.info("error");
        }
    }
}
