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
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.noticeofchange.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.prl.models.noticeofchange.NoticeOfChangeParties;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessClient;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.noticeofchange.NoticeOfChangePartiesConverter;
import uk.gov.hmcts.reform.prl.utils.noticeofchange.RespondentPolicyConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
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
    @Autowired
    private final UserService userService;

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

    public AboutToStartOrSubmitCallbackResponse applyDecision(CallbackRequest callbackRequest, String authorisation) {
        try {
            log.info("applyDecision start getCaseDetailsBefore json ===>" + objectMapper.writeValueAsString(callbackRequest.getCaseDetailsBefore()));
            log.info("applyDecision start getCaseDetails json ===>" + objectMapper.writeValueAsString(callbackRequest.getCaseDetails()));
        } catch (JsonProcessingException e) {
            log.info("error");
        }

        return assignCaseAccessClient.applyDecision(
            authorisation,
            tokenGenerator.generate(),
            decisionRequest(callbackRequest.getCaseDetails()));
    }

    public CaseData nocRequestSubmitted(CallbackRequest callbackRequest, String authorisation) {
        CaseData oldCaseData = getCaseData(callbackRequest.getCaseDetailsBefore(), objectMapper);
        CaseData newCaseData = getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        try {
            log.info("callbackRequest.getCaseDetailsBefore() json ===>" + objectMapper.writeValueAsString(callbackRequest.getCaseDetailsBefore()));
            log.info("callbackRequest.getCaseDetails() json ===>" + objectMapper.writeValueAsString(callbackRequest.getCaseDetails()));
        } catch (JsonProcessingException e) {
            log.info("error");
        }

        ChangeOrganisationRequest changeOrganisationRequest = oldCaseData.getChangeOrganisationRequestField();
        if (changeOrganisationRequest != null
            && changeOrganisationRequest.getCaseRoleId() != null
            && changeOrganisationRequest.getCaseRoleId().getValue() != null) {
            log.info("inside changeOrganisationRequest present");
            String caseRoleLabel = changeOrganisationRequest.getCaseRoleId().getValue().getCode();
            Optional<SolicitorRole> solicitorRole = SolicitorRole.from(caseRoleLabel);
            if (solicitorRole.isPresent() && RESPONDENT.equals(solicitorRole.get().getRepresenting())) {
                log.info("inside solicitorRole present");
                if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(newCaseData))) {
                    int partyIndex = solicitorRole.get().getIndex();
                    Element<PartyDetails> representedRespondentElement = newCaseData.getRespondents().get(partyIndex);
                    UserInfo legalRepresentativeSolicitorInfo = userService.getUserInfo(
                        authorisation,
                        UserRoles.SOLICITOR
                    );
                    representedRespondentElement.getValue()
                        .setUser(User.builder()
                                     .idamId(legalRepresentativeSolicitorInfo.getIdamId())
                                     .email(legalRepresentativeSolicitorInfo.getEmailAddress())
                                     .solicitorRepresented(YesOrNo.Yes)
                                     .build());
                    log.info("updated representedRespondentElement ===> " + representedRespondentElement);
                    newCaseData.getRespondents().set(partyIndex, representedRespondentElement);
                }
            }
        }
        log.info("return newCaseData ===> " + newCaseData);
        return newCaseData;
    }
}
