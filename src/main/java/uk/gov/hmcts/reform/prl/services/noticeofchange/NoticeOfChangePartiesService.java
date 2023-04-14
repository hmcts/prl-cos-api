package uk.gov.hmcts.reform.prl.services.noticeofchange;

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

        List<Element<PartyDetails>> elements = representing.getTarget().apply(caseData);
        int numElements = null != elements ? elements.size() : 0;

        List<SolicitorRole> solicitorRoles = SolicitorRole.values(representing);
        for (int i = 0; i < solicitorRoles.size(); i++) {
            SolicitorRole solicitorRole = solicitorRoles.get(i);

            if (null != elements) {
                Optional<Element<PartyDetails>> solicitorContainer = i < numElements
                    ? Optional.of(elements.get(i))
                    : Optional.empty();

                OrganisationPolicy organisationPolicy = policyConverter.generate(
                    solicitorRole, solicitorContainer
                );

                data.put(String.format(representing.getPolicyFieldTemplate(), i), organisationPolicy);

                Optional<NoticeOfChangeParties> possibleAnswer = populateAnswer(
                    strategy, solicitorContainer
                );

                if (possibleAnswer.isPresent()) {
                    data.put(String.format(representing.getNocAnswersTemplate(), i), possibleAnswer.get());
                }

            }
        }

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
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        return assignCaseAccessClient.applyDecision(
            authorisation,
            tokenGenerator.generate(),
            decisionRequest(caseDetails)
        );
    }

    public void nocRequestSubmitted(CallbackRequest callbackRequest, String authorisation) {
        CaseData oldCaseData = getCaseData(callbackRequest.getCaseDetailsBefore(), objectMapper);
        CaseData newCaseData = getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        ChangeOrganisationRequest changeOrganisationRequest = oldCaseData.getChangeOrganisationRequestField();

        UserDetails legalRepresentativeSolicitorDetails = userService.getUserDetails(
            authorisation
        );

        newCaseData = getRepresentedPartyDetails(
            changeOrganisationRequest,
            newCaseData,
            legalRepresentativeSolicitorDetails
        );
        Optional<SolicitorRole> solicitorRole = getSolicitorRole(changeOrganisationRequest);
        tabService.updatePartyDetailsForNoc(newCaseData, solicitorRole);

        String solicitorName = legalRepresentativeSolicitorDetails.getFullName()
            + EMPTY_SPACE_STRING + legalRepresentativeSolicitorDetails.getSurname();

        if (changeOrganisationRequest != null) {
            NoticeOfChangeEvent noticeOfChangeEvent = prepareNoticeOfChangeEvent(
                newCaseData,
                solicitorRole,
                solicitorName,
                changeOrganisationRequest.getCreatedBy()
            );
            eventPublisher.publishEvent(noticeOfChangeEvent);
        }
    }

    private CaseData getRepresentedPartyDetails(ChangeOrganisationRequest changeOrganisationRequest,
                                                CaseData caseData,
                                                UserDetails legalRepresentativeSolicitorDetails) {
        Optional<SolicitorRole> solicitorRole = getSolicitorRole(changeOrganisationRequest);
        if (solicitorRole.isPresent()) {
            int partyIndex = solicitorRole.get().getIndex();
            if (RESPONDENT.equals(solicitorRole.get().getRepresenting())) {
                List<Element<PartyDetails>> respondents = RESPONDENT.getTarget().apply(caseData);
                if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                    return updateC100RespondentDetails(partyIndex, respondents, legalRepresentativeSolicitorDetails,
                                                       changeOrganisationRequest, caseData
                    );
                }
            }
        }
        return null;
    }

    private static Optional<SolicitorRole> getSolicitorRole(ChangeOrganisationRequest changeOrganisationRequest) {
        Optional<SolicitorRole> solicitorRole = Optional.empty();
        if (changeOrganisationRequest != null
            && changeOrganisationRequest.getCaseRoleId() != null
            && changeOrganisationRequest.getCaseRoleId().getValue() != null) {
            String caseRoleLabel = changeOrganisationRequest.getCaseRoleId().getValue().getCode();
            solicitorRole = SolicitorRole.fromCaseRoleLabel(caseRoleLabel);
        }
        return solicitorRole;
    }

    private CaseData updateC100RespondentDetails(int partyIndex,
                                                 List<Element<PartyDetails>> respondents,
                                                 UserDetails legalRepresentativeSolicitorDetails,
                                                 ChangeOrganisationRequest changeOrganisationRequest,
                                                 CaseData caseData) {
        Element<PartyDetails> representedRespondentElement = respondents.get(partyIndex);
        String surname = "";
        if (legalRepresentativeSolicitorDetails != null && legalRepresentativeSolicitorDetails.getSurname().isPresent()) {
            surname = legalRepresentativeSolicitorDetails.getSurname().get();
        }
        PartyDetails updPartyDetails = representedRespondentElement.getValue().toBuilder()
            .user(representedRespondentElement.getValue().getUser().toBuilder()
                      .solicitorRepresented(YesOrNo.Yes)
                      .build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorEmail(changeOrganisationRequest.getCreatedBy())
            .representativeFirstName(legalRepresentativeSolicitorDetails.getFullName())
            .representativeLastName(surname)
            .solicitorOrg(changeOrganisationRequest.getOrganisationToAdd())
            .build();
        Element<PartyDetails> updatedRepresentedRespondentElement = ElementUtils
            .element(representedRespondentElement.getId(), updPartyDetails);
        caseData.getRespondents().set(partyIndex, updatedRepresentedRespondentElement);
        return caseData;
    }

    private NoticeOfChangeEvent prepareNoticeOfChangeEvent(CaseData newCaseData,
                                                           Optional<SolicitorRole> solicitorRole,
                                                           String solicitorName,
                                                           String solicitorEmailAddress) {
        if (solicitorRole.isPresent()) {
            int partyIndex = solicitorRole.get().getIndex();
            return NoticeOfChangeEvent.builder()
                .caseData(newCaseData)
                .solicitorEmailAddress(solicitorEmailAddress)
                .solicitorName(solicitorName)
                .representedPartyIndex(partyIndex)
                .representing(solicitorRole.get().getRepresenting())
                .build();

        }
        return null;
    }
}
