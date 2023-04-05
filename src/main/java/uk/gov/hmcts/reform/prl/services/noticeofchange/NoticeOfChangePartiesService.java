package uk.gov.hmcts.reform.prl.services.noticeofchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.noticeofchange.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.prl.models.noticeofchange.NoticeOfChangeParties;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessClient;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.noticeofchange.NoticeOfChangePartiesConverter;
import uk.gov.hmcts.reform.prl.utils.noticeofchange.RespondentPolicyConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
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
    private final UserService userService;
    private final EventService eventPublisher;
    @Qualifier("allTabsService")
    private final AllTabServiceImpl tabService;

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
            log.info("applyDecision start getCaseDetails json ===>" + objectMapper.writeValueAsString(callbackRequest.getCaseDetails()));
        } catch (JsonProcessingException e) {
            log.info("error");
        }
        log.info("inside changeOrganisationRequest present");

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        //caseDetails.getData().putAll(updateRepresentedPartyDetails(authorisation, caseDetails));
        log.info("applyDecision updated caseDetails ===> " + caseDetails);
        return assignCaseAccessClient.applyDecision(
            authorisation,
            tokenGenerator.generate(),
            decisionRequest(caseDetails)
        );
    }

    private Map<String, Object> getRepresentedPartyDetails(String authorisation,
                                                           ChangeOrganisationRequest changeOrganisationRequest, CaseData caseData) {
        Optional<SolicitorRole> solicitorRole = getSolicitorRole(changeOrganisationRequest);
        if (solicitorRole.isPresent()) {
            int partyIndex = solicitorRole.get().getIndex();
            if (RESPONDENT.equals(solicitorRole.get().getRepresenting())) {
                List<Element<PartyDetails>> respondents = RESPONDENT.getTarget().apply(caseData);
                log.info("inside solicitorRole present");
                if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                    return updateC100RespondentDetails(authorisation, changeOrganisationRequest, partyIndex, respondents);
                }
            }
        }
        return new HashMap<>();
    }

    private Map<String, Object> updateC100RespondentDetails(String authorisation, ChangeOrganisationRequest changeOrganisationRequest,
                                                            int partyIndex, List<Element<PartyDetails>> respondents) {
        Map<String, Object> updatedPartyDetails = new HashMap<>();
        Element<PartyDetails> representedRespondentElement = respondents.get(partyIndex);
        UserDetails legalRepresentativeSolicitorInfo = userService.getUserDetails(
            authorisation
        );
        PartyDetails updPartyDetails = representedRespondentElement.getValue().toBuilder()
            .user(User.builder()
                      .idamId(legalRepresentativeSolicitorInfo.getId())
                      .email(changeOrganisationRequest.getCreatedBy())
                      .solicitorRepresented(YesOrNo.Yes)
                      .build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> updatedRepresentedRespondentElement = ElementUtils
            .element(representedRespondentElement.getId(), updPartyDetails);
        log.info("updated representedRespondentElement ===> " + updatedRepresentedRespondentElement);
        respondents.set(partyIndex, updatedRepresentedRespondentElement);
        updatedPartyDetails.put("respondents", respondents);
        log.info("caseDataUpdated ===> " + updatedPartyDetails);
        return updatedPartyDetails;
    }

    private static Optional<SolicitorRole> getSolicitorRole(ChangeOrganisationRequest changeOrganisationRequest) {
        Optional<SolicitorRole> solicitorRole = Optional.empty();
        if (changeOrganisationRequest != null
            && changeOrganisationRequest.getCaseRoleId() != null
            && changeOrganisationRequest.getCaseRoleId().getValue() != null) {
            log.info("inside changeOrganisationRequest present");
            String caseRoleLabel = changeOrganisationRequest.getCaseRoleId().getValue().getCode();
            solicitorRole = SolicitorRole.from(caseRoleLabel);
        }
        return solicitorRole;
    }

    public void nocRequestSubmitted(CallbackRequest callbackRequest, String authorisation) {
        CaseData oldCaseData = getCaseData(callbackRequest.getCaseDetailsBefore(), objectMapper);
        CaseData newCaseData = getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        ChangeOrganisationRequest changeOrganisationRequest = oldCaseData.getChangeOrganisationRequestField();

        UserDetails legalRepresentativeSolicitorInfo = userService.getUserDetails(
            authorisation
        );
        String representativeSolicitorName = legalRepresentativeSolicitorInfo.getForename()
            + EMPTY_SPACE_STRING + legalRepresentativeSolicitorInfo.getSurname();

        NoticeOfChangeEvent noticeOfChangeEvent = prepareNoticeOfChangeEvent(
            newCaseData,
            changeOrganisationRequest,
            representativeSolicitorName
        );
        log.info("NoticeOfChangeEvent ===> " + noticeOfChangeEvent);
        eventPublisher.publishEvent(noticeOfChangeEvent);
        tabService.updatePartyDetailsForNoc(newCaseData, getRepresentedPartyDetails(authorisation, changeOrganisationRequest, newCaseData));
    }

    private NoticeOfChangeEvent prepareNoticeOfChangeEvent(CaseData newCaseData,
                                                           ChangeOrganisationRequest changeOrganisationRequest, String representativeSolicitorName) {

        Optional<SolicitorRole> solicitorRole = getSolicitorRole(changeOrganisationRequest);
        if (solicitorRole.isPresent()) {
            int partyIndex = solicitorRole.get().getIndex();
            return NoticeOfChangeEvent.builder()
                .caseData(newCaseData)
                .solicitorEmailAddress(changeOrganisationRequest.getCreatedBy())
                .solicitorName(representativeSolicitorName)
                .representedPartyIndex(partyIndex)
                .representing(solicitorRole.get().getRepresenting())
                .build();

        }
        return null;
    }
}

