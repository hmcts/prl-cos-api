package uk.gov.hmcts.reform.prl.services.noticeofchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.CaseRole;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.TypeOfNocEventEnum;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.caseaccess.CaseUser;
import uk.gov.hmcts.reform.prl.models.caseaccess.FindUserCaseRolesResponse;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.noticeofchange.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.prl.models.noticeofchange.NoticeOfChangeParties;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessClient;
import uk.gov.hmcts.reform.prl.services.caseaccess.CcdDataStoreService;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.noticeofchange.NoticeOfChangePartiesConverter;
import uk.gov.hmcts.reform.prl.utils.noticeofchange.RespondentPolicyConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BLANK_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DARESPONDENT;
import static uk.gov.hmcts.reform.prl.models.noticeofchange.DecisionRequest.decisionRequest;
import static uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService.NoticeOfChangeAnswersPopulationStrategy.BLANK;
import static uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService.NoticeOfChangeAnswersPopulationStrategy.POPULATE;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getCaseData;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class NoticeOfChangePartiesService {
    public static final String NO_REPRESENTATION_FOUND_ERROR = "You do not represent anyone in this case.";
    public static final String INVALID_REPRESENTATION_ERROR = "You do not represent selected party";
    public static final String CASE_NOT_REPRESENTED_BY_SOLICITOR_ERROR = "This case is not represented by solicitor anymore.";
    public static final String SOL_STOP_REP_CHOOSE_PARTIES = "solStopRepChooseParties";

    public static final String REMOVE_LEGAL_REPRESENTATIVE_AND_PARTIES_LIST = "removeLegalRepAndPartiesList";
    public final NoticeOfChangePartiesConverter partiesConverter;
    public final RespondentPolicyConverter policyConverter;
    private final AuthTokenGenerator tokenGenerator;
    private final AssignCaseAccessClient assignCaseAccessClient;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final EventService eventPublisher;
    @Qualifier("allTabsService")
    private final AllTabServiceImpl tabService;
    private final DynamicMultiSelectListService dynamicMultiSelectListService;
    private final Time time;
    private final CcdCoreCaseDataService ccdCoreCaseDataService;

    private final CcdDataStoreService userDataStoreService;
    private final SystemUserService systemUserService;

    private final CaseInviteManager caseInviteManager;
    private final IdamClient idamClient;

    public static final String REPRESENTATIVE_REMOVED_LABEL = "# Representative removed";

    public static final String REPRESENTATIVE_REMOVED_STATUS_LABEL = "### What happens next \n\n The court will consider your "
        + "withdrawal request.";

    public Map<String, Object> generate(CaseData caseData, SolicitorRole.Representing representing) {
        return generate(caseData, representing, POPULATE);
    }

    public Map<String, Object> generate(CaseData caseData, SolicitorRole.Representing representing,
                                        NoticeOfChangeAnswersPopulationStrategy strategy) {
        Map<String, Object> data = new HashMap<>();

        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            generateC100NocDetails(caseData, representing, strategy, data);
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            generateFl401NocDetails(caseData, representing, strategy, data);
        }
        return data;
    }

    public void generateC100NocDetails(CaseData caseData, SolicitorRole.Representing representing,
                                       NoticeOfChangeAnswersPopulationStrategy strategy, Map<String, Object> data) {
        List<Element<PartyDetails>> caElements = representing.getCaTarget().apply(caseData);
        int numElements = null != caElements ? caElements.size() : 0;
        List<SolicitorRole> solicitorRoles = SolicitorRole.matchingRoles(representing);

        for (int i = 0; i < solicitorRoles.size(); i++) {
            SolicitorRole solicitorRole = solicitorRoles.get(i);

            if (null != caElements) {
                Optional<Element<PartyDetails>> solicitorContainer = i < numElements
                    ? Optional.of(caElements.get(i))
                    : Optional.empty();

                OrganisationPolicy organisationPolicy = policyConverter.caGenerate(
                    solicitorRole, solicitorContainer
                );
                data.put(String.format(representing.getPolicyFieldTemplate(), (i + 1)), organisationPolicy);

                Optional<NoticeOfChangeParties> possibleAnswer = populateCaAnswer(
                    strategy, solicitorContainer
                );
                if (possibleAnswer.isPresent()) {
                    data.put(String.format(representing.getNocAnswersTemplate(), (i + 1)), possibleAnswer.get());
                }
            }
        }

        generateRequiredOrgPoliciesForNoc(representing, data);
    }

    public void generateFl401NocDetails(CaseData caseData, SolicitorRole.Representing representing,
                                        NoticeOfChangeAnswersPopulationStrategy strategy, Map<String, Object> data) {
        PartyDetails daElements = representing.getDaTarget().apply(caseData);

        List<SolicitorRole> solicitorRoles = SolicitorRole.matchingRoles(representing);
        for (int i = 0; i < solicitorRoles.size(); i++) {
            SolicitorRole solicitorRole = solicitorRoles.get(i);

            if (null != daElements) {
                OrganisationPolicy organisationPolicy = policyConverter.daGenerate(
                    solicitorRole, daElements
                );
                data.put(representing.getPolicyFieldTemplate(), organisationPolicy);

                Optional<NoticeOfChangeParties> possibleAnswer = populateDaAnswer(
                    strategy, daElements
                );
                data.put(representing.getNocAnswersTemplate(), possibleAnswer.get());
            }
        }

        generateRequiredOrgPoliciesForNoc(representing, data);
    }

    private Optional<NoticeOfChangeParties> populateCaAnswer(NoticeOfChangeAnswersPopulationStrategy strategy,
                                                             Optional<Element<PartyDetails>> element) {
        if (BLANK == strategy) {
            return Optional.of(NoticeOfChangeParties.builder().build());
        }
        return element.map(partiesConverter::generateCaForSubmission);
    }

    private Optional<NoticeOfChangeParties> populateDaAnswer(NoticeOfChangeAnswersPopulationStrategy strategy,
                                                             PartyDetails partyDetails) {
        if (BLANK == strategy) {
            return Optional.of(NoticeOfChangeParties.builder().build());
        }

        return partiesConverter.generateDaForSubmission(partyDetails) == null
            ? Optional.of(NoticeOfChangeParties.builder().build())
            : Optional.of(partiesConverter.generateDaForSubmission(partyDetails));
    }

    public enum NoticeOfChangeAnswersPopulationStrategy {
        POPULATE, BLANK
    }

    public AboutToStartOrSubmitCallbackResponse applyDecision(CallbackRequest callbackRequest, String authorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        UserDetails legalRepresentativeSolicitorDetails = userService.getUserDetails(
            authorisation
        );
        try {
            log.info("caseDetails for applyDecision ==>" + objectMapper.writeValueAsString(caseDetails));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info("legalRepresentativeSolicitorDetails ===> " + legalRepresentativeSolicitorDetails.getId() + "--"
                     + legalRepresentativeSolicitorDetails.getEmail() + "--"
                     + legalRepresentativeSolicitorDetails.getFullName()
        );

        return assignCaseAccessClient.applyDecision(
            authorisation,
            tokenGenerator.generate(),
            decisionRequest(caseDetails)
        );
    }

    public void nocRequestSubmitted(CallbackRequest callbackRequest, String authorisation) {
        CaseData oldCaseData = getCaseData(callbackRequest.getCaseDetailsBefore(), objectMapper);

        log.info("This is start oldCaseData ==> " + oldCaseData);

        String systemAuthorisation = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(systemAuthorisation);
        EventRequestData allTabsUpdateEventRequestData = ccdCoreCaseDataService.eventRequest(
            CaseEvent.UPDATE_ALL_TABS,
            systemUpdateUserId
        );
        StartEventResponse allTabsUpdateStartEventResponse =
            ccdCoreCaseDataService.startUpdate(
                systemAuthorisation,
                allTabsUpdateEventRequestData,
                String.valueOf(callbackRequest.getCaseDetails().getId()),
                true
            );

        CaseData allTabsUpdateCaseData = CaseUtils.getCaseDataFromStartUpdateEventResponse(
            allTabsUpdateStartEventResponse,
            objectMapper
        );

        ChangeOrganisationRequest changeOrganisationRequest = oldCaseData.getChangeOrganisationRequestField();

        log.info("This is start changeOrganisationRequest ==> " + changeOrganisationRequest);

        UserDetails lrDetails = userService.getUserDetails(authorisation);
        log.info("lrDetails before ===> " + lrDetails.getId() + "--"
                     + lrDetails.getEmail() + "--"
                     + lrDetails.getFullName()
        );
        allTabsUpdateCaseData = updateRepresentedPartyDetails(
            changeOrganisationRequest,
            allTabsUpdateCaseData,
            lrDetails,
            TypeOfNocEventEnum.addLegalRepresentation
        );
        Optional<SolicitorRole> solicitorRole = getSolicitorRole(changeOrganisationRequest);

        tabService.updateAllTabsIncludingConfTabRefactored(
            systemAuthorisation,
            String.valueOf(allTabsUpdateCaseData.getId()),
            allTabsUpdateStartEventResponse,
            allTabsUpdateEventRequestData,
            allTabsUpdateCaseData
        );

        eventPublisher.publishEvent(new CaseDataChanged(allTabsUpdateCaseData));

        sendEmailOnAddLegalRepresenative(
            authorisation,
            systemAuthorisation,
            allTabsUpdateCaseData,
            changeOrganisationRequest,
            lrDetails,
            solicitorRole
        );


    }

    private void sendEmailOnAddLegalRepresenative(String authorisation, String systemAuthorisation, CaseData allTabsUpdateCaseData,
                                                  ChangeOrganisationRequest changeOrganisationRequest, UserDetails lrDetails,
                                                  Optional<SolicitorRole> solicitorRole) {
        CaseDetails caseDetails = ccdCoreCaseDataService.findCaseById(
            systemAuthorisation,
            String.valueOf(allTabsUpdateCaseData.getId())
        );
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        lrDetails = getSolicitorUserDetails(
            authorisation,
            String.valueOf(caseData.getId()),
            lrDetails,
            solicitorRole
        );
        String solicitorName = lrDetails.getFullName();

        if (changeOrganisationRequest != null) {
            NoticeOfChangeEvent noticeOfChangeEvent = prepareNoticeOfChangeEvent(
                caseData,
                solicitorRole,
                solicitorName,
                changeOrganisationRequest.getCreatedBy(),
                TypeOfNocEventEnum.addLegalRepresentation.getDisplayedValue(),
                ""
            );
            eventPublisher.publishEvent(noticeOfChangeEvent);
        }
    }

    private UserDetails getSolicitorUserDetails(String authorisation, String caseId,
                                                UserDetails legalRepresentativeSolicitorDetails, Optional<SolicitorRole> solicitorRole) {
        FindUserCaseRolesResponse findUserCaseRolesResponse
            = findUserCaseRoles(caseId, authorisation);
        if (null != findUserCaseRolesResponse && solicitorRole.isPresent()) {
            for (CaseUser caseUser : findUserCaseRolesResponse.getCaseUsers()) {
                if (caseUser.getCaseRole().equalsIgnoreCase(solicitorRole.get().getCaseRoleLabel())) {
                    String userToken = systemUserService.getSysUserToken();
                    legalRepresentativeSolicitorDetails = idamClient.getUserByUserId(userToken, caseUser.getUserId());
                    log.info("legalRepresentativeSolicitorDetails after ===> " + legalRepresentativeSolicitorDetails.getId() + "--"
                                 + legalRepresentativeSolicitorDetails.getEmail() + "--"
                                 + legalRepresentativeSolicitorDetails.getFullName()
                    );
                }
            }
        }
        return legalRepresentativeSolicitorDetails;
    }

    private CaseData updateRepresentedPartyDetails(ChangeOrganisationRequest changeOrganisationRequest,
                                                   CaseData caseData,
                                                   UserDetails legalRepresentativeSolicitorDetails,
                                                   TypeOfNocEventEnum typeOfNocEvent) {
        Optional<SolicitorRole> solicitorRole = getSolicitorRole(changeOrganisationRequest);
        if (solicitorRole.isPresent()) {
            int partyIndex = solicitorRole.get().getIndex();
            if (CARESPONDENT.equals(solicitorRole.get().getRepresenting())
                && C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                List<Element<PartyDetails>> respondents = CARESPONDENT.getCaTarget().apply(caseData);
                return updateC100PartyDetails(partyIndex, respondents, legalRepresentativeSolicitorDetails,
                                              changeOrganisationRequest, caseData, CARESPONDENT, typeOfNocEvent
                );
            } else if (CAAPPLICANT.equals(solicitorRole.get().getRepresenting())
                && C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {

                List<Element<PartyDetails>> applicants = CAAPPLICANT.getCaTarget().apply(caseData);
                return updateC100PartyDetails(partyIndex, applicants, legalRepresentativeSolicitorDetails,
                                              changeOrganisationRequest, caseData, CAAPPLICANT, typeOfNocEvent
                );
            } else if (DAAPPLICANT.equals(solicitorRole.get().getRepresenting())
                && FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                log.info("updateRepresentedPartyDetails DAAPPLICANT");
                return updateFl401PartyDetails(legalRepresentativeSolicitorDetails,
                                               changeOrganisationRequest, caseData,
                                               DAAPPLICANT, typeOfNocEvent
                );
            } else if (DARESPONDENT.equals(solicitorRole.get().getRepresenting())
                && FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                log.info("updateRepresentedPartyDetails DARESPONDENT");
                return updateFl401PartyDetails(legalRepresentativeSolicitorDetails,
                                               changeOrganisationRequest, caseData,
                                               DARESPONDENT, typeOfNocEvent
                );
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

    private CaseData updateC100PartyDetails(int partyIndex,
                                            List<Element<PartyDetails>> parties,
                                            UserDetails legalRepresentativeSolicitorDetails,
                                            ChangeOrganisationRequest changeOrganisationRequest,
                                            CaseData caseData,
                                            SolicitorRole.Representing representing,
                                            TypeOfNocEventEnum typeOfNocEvent) {
        Element<PartyDetails> partyDetailsElement = parties.get(partyIndex);
        PartyDetails updPartyDetails = updatePartyDetails(
            legalRepresentativeSolicitorDetails,
            changeOrganisationRequest,
            partyDetailsElement.getValue(),
            typeOfNocEvent
        );

        Element<PartyDetails> updatedRepresentedRespondentElement;
        if (CARESPONDENT.equals(representing)) {
            if (updPartyDetails.getResponse() != null
                && !YesOrNo.Yes.equals(updPartyDetails.getResponse().getC7ResponseSubmitted())) {
                PartyDetails respondingParty = updPartyDetails.toBuilder().response(Response.builder().build()).build();
                updatedRepresentedRespondentElement = ElementUtils
                    .element(partyDetailsElement.getId(), respondingParty);
            } else {
                updatedRepresentedRespondentElement = ElementUtils
                    .element(partyDetailsElement.getId(), updPartyDetails);
            }
            caseData.getRespondents().set(partyIndex, updatedRepresentedRespondentElement);
        } else if (CAAPPLICANT.equals(representing)) {
            updatedRepresentedRespondentElement = ElementUtils
                .element(partyDetailsElement.getId(), updPartyDetails);
            caseData.getApplicants().set(partyIndex, updatedRepresentedRespondentElement);
        }
        return caseData;
    }

    private CaseData updateFl401PartyDetails(UserDetails legalRepresentativeSolicitorDetails,
                                             ChangeOrganisationRequest changeOrganisationRequest,
                                             CaseData caseData,
                                             SolicitorRole.Representing representing,
                                             TypeOfNocEventEnum typeOfNocEvent) {
        CaseData updatedCaseData = null;
        log.info("Inside updateFl401PartyDetails");
        if (DAAPPLICANT.equals(representing)) {
            log.info("Inside DAAPPLICANT");
            PartyDetails updPartyDetails = updatePartyDetails(
                legalRepresentativeSolicitorDetails,
                changeOrganisationRequest,
                caseData.getApplicantsFL401(),
                typeOfNocEvent
            );
            updatedCaseData = caseData.toBuilder().applicantsFL401(updPartyDetails).build();
        } else if (DARESPONDENT.equals(representing)) {
            log.info("Inside DARESPONDENT");
            PartyDetails updPartyDetails = updatePartyDetails(
                legalRepresentativeSolicitorDetails,
                changeOrganisationRequest,
                caseData.getRespondentsFL401(),
                typeOfNocEvent
            );
            updatedCaseData = caseData.toBuilder().respondentsFL401(updPartyDetails).build();
        }
        log.info("After updateFl401PartyDetails updatedCaseData ==> " + updatedCaseData);
        return updatedCaseData;
    }

    private static PartyDetails updatePartyDetails(UserDetails legalRepresentativeSolicitorDetails,
                                                   ChangeOrganisationRequest changeOrganisationRequest,
                                                   PartyDetails partyDetails, TypeOfNocEventEnum typeOfNocEvent) {
        return partyDetails.toBuilder()
            .user(partyDetails.getUser().toBuilder()
                      .solicitorRepresented(TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)
                                                ? YesOrNo.Yes : YesOrNo.No)
                      .build())
            .doTheyHaveLegalRepresentation(TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)
                                               ? YesNoDontKnow.yes : YesNoDontKnow.no)
            .solicitorEmail(TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)
                                ? changeOrganisationRequest.getCreatedBy() : null)
            .representativeFirstName(TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)
                                         ? legalRepresentativeSolicitorDetails.getForename() : null)
            .representativeLastName(TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)
                                        ? legalRepresentativeSolicitorDetails.getSurname().orElse(
                "") : null)
            .solicitorOrg(TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)
                              ? changeOrganisationRequest.getOrganisationToAdd() : Organisation.builder().build())
            .build();
    }

    private NoticeOfChangeEvent prepareNoticeOfChangeEvent(CaseData newCaseData,
                                                           Optional<SolicitorRole> solicitorRole,
                                                           String solicitorName,
                                                           String solicitorEmailAddress,
                                                           String typeOfEvent,
                                                           String accessCode) {
        if (solicitorRole.isPresent()) {
            int partyIndex = solicitorRole.get().getIndex();
            return NoticeOfChangeEvent.builder()
                .caseData(newCaseData)
                .solicitorEmailAddress(solicitorEmailAddress)
                .solicitorName(solicitorName)
                .representedPartyIndex(partyIndex)
                .representing(solicitorRole.get().getRepresenting())
                .typeOfEvent(typeOfEvent)
                .accessCode(accessCode)
                .build();

        }
        return null;
    }

    private void generateRequiredOrgPoliciesForNoc(SolicitorRole.Representing
                                                       representing, Map<String, Object> data) {
        List<SolicitorRole> nonSolicitorRoles = SolicitorRole.notMatchingRoles(representing);
        for (int i = 0; i < nonSolicitorRoles.size(); i++) {
            SolicitorRole solicitorRole = nonSolicitorRoles.get(i);
            if (CAAPPLICANT.equals(solicitorRole.getRepresenting()) || CARESPONDENT.equals(solicitorRole.getRepresenting())) {
                OrganisationPolicy organisationPolicy = policyConverter.caGenerate(
                    solicitorRole, Optional.empty());
                data.put(String.format(
                    solicitorRole.getRepresenting().getPolicyFieldTemplate(),
                    (solicitorRole.getIndex() + 1)
                ), organisationPolicy);
            } else if (DAAPPLICANT.equals(solicitorRole.getRepresenting()) || DARESPONDENT.equals(solicitorRole.getRepresenting())) {
                OrganisationPolicy organisationPolicy = policyConverter.daGenerate(
                    solicitorRole, PartyDetails.builder().build());
                data.put(solicitorRole.getRepresenting().getPolicyFieldTemplate(), organisationPolicy);
            }
        }
    }

    public Map<String, Object> populateAboutToStartStopRepresentation(String authorisation,
                                                                      CallbackRequest callbackRequest,
                                                                      List<String> errorList) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        List<Element<PartyDetails>> partyElementList = findSolicitorRepresentedParties(caseData, authorisation);
        DynamicMultiSelectList solicitorRepresentedParties
            = dynamicMultiSelectListService.getSolicitorRepresentedParties(partyElementList);

        if (solicitorRepresentedParties.getListItems().isEmpty()) {
            errorList.add(NO_REPRESENTATION_FOUND_ERROR);
        } else {
            caseDataUpdated.put(SOL_STOP_REP_CHOOSE_PARTIES, solicitorRepresentedParties);
        }
        return caseDataUpdated;
    }

    public Map<String, Object> aboutToSubmitStopRepresenting(String authorisation,
                                                             CallbackRequest callbackRequest,
                                                             List<String> errorList) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails, objectMapper);
        Map<Optional<SolicitorRole>, Element<PartyDetails>> selectedPartyDetailsMap = new HashMap<>();
        log.info("selectedPartyDetailsList size is ::" + selectedPartyDetailsMap.size());
        log.info("selectedPartyDetailsList is ::" + selectedPartyDetailsMap);
        FindUserCaseRolesResponse findUserCaseRolesResponse
            = findUserCaseRoles(String.valueOf(caseDetails.getId()), authorisation);
        Map<String, Object> caseDataUpdated = caseDetails.getData();

        for (CaseUser caseUser : findUserCaseRolesResponse.getCaseUsers()) {
            log.info("caseUser is = " + caseUser + " and roles are " + caseUser.getCaseRole());
            SolicitorRole.fromCaseRoleLabel(caseUser.getCaseRole()).ifPresent(
                x -> {
                    switch (x.getRepresenting()) {
                        case CAAPPLICANT:
                            findMatchingParty(
                                errorList,
                                caseData,
                                caseData.getApplicants().get(x.getIndex()),
                                selectedPartyDetailsMap,
                                x
                            );
                            break;
                        case CARESPONDENT:
                            findMatchingParty(
                                errorList,
                                caseData,
                                caseData.getRespondents().get(x.getIndex()),
                                selectedPartyDetailsMap,
                                x
                            );
                            break;
                        case DAAPPLICANT:
                            selectedPartyDetailsMap.put(
                                Optional.of(SolicitorRole.FL401APPLICANTSOLICITOR),
                                element(
                                    caseData.getApplicantsFL401().getPartyId(),
                                    caseData.getApplicantsFL401()
                                )
                            );
                            break;
                        case DARESPONDENT:
                            selectedPartyDetailsMap.put(
                                Optional.of(SolicitorRole.FL401RESPONDENTSOLICITOR),
                                element(
                                    caseData.getRespondentsFL401().getPartyId(),
                                    caseData.getRespondentsFL401()
                                )
                            );
                            break;
                        default:
                            break;
                    }
                    log.info("Party details found - rest wip:: ", selectedPartyDetailsMap);
                });
        }
        caseDataUpdated = createChangeOrgReqAndRemoveRepresentative(
            authorisation,
            caseDetails,
            selectedPartyDetailsMap,
            caseDataUpdated
        );
        return caseDataUpdated;
    }

    private Map<String, Object> createChangeOrgReqAndRemoveRepresentative(String authorisation,
                                                                          CaseDetails caseDetails, Map<Optional<SolicitorRole>,
        Element<PartyDetails>> selectedPartyDetailsMap, Map<String, Object> caseDataUpdated) {
        for (var entry : selectedPartyDetailsMap.entrySet()) {
            Optional<SolicitorRole> removeSolicitorRole = entry.getKey();
            Element<PartyDetails> partyDetailsElement = entry.getValue();
            if (null != partyDetailsElement.getValue().getSolicitorOrg() && removeSolicitorRole.isPresent()) {
                log.info("partyDetails ==> " + partyDetailsElement.getValue().getLabelForDynamicList());
                UserDetails userDetails = userService.getUserDetails(authorisation);
                DynamicListElement roleItem = DynamicListElement.builder()
                    .code(removeSolicitorRole.get().getCaseRoleLabel())
                    .label(removeSolicitorRole.get().getCaseRoleLabel())
                    .build();
                ChangeOrganisationRequest changeOrganisationRequest = ChangeOrganisationRequest.builder()
                    .organisationToRemove(partyDetailsElement.getValue().getSolicitorOrg())
                    .createdBy(userDetails.getEmail())
                    .caseRoleId(DynamicList.builder()
                                    .value(roleItem)
                                    .listItems(List.of(roleItem))
                                    .build())
                    .approvalStatus(ChangeOrganisationApprovalStatus.APPROVED)
                    .requestTimestamp(time.now())
                    .build();
                caseDetails.getData()
                    .put("changeOrganisationRequestField", changeOrganisationRequest);
                String userToken = systemUserService.getSysUserToken();
                AboutToStartOrSubmitCallbackResponse response = assignCaseAccessClient.applyDecision(
                    userToken,
                    tokenGenerator.generate(),
                    decisionRequest(caseDetails)
                );
                if (null != response && null != response.getData()) {
                    log.info("applyDecision response ==> " + response.getData());
                    caseDetails = caseDetails.toBuilder().data(response.getData()).build();
                    caseDataUpdated = response.getData();
                }
            }
        }
        return caseDataUpdated;
    }

    public void submittedStopRepresenting(CallbackRequest callbackRequest) {
        CaseData newCaseData = getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        DynamicMultiSelectList solStopRepChooseParties = newCaseData.getSolStopRepChooseParties();
        Map<Optional<SolicitorRole>, Element<PartyDetails>> selectedPartyDetailsMap = new HashMap<>();
        selectedPartyDetailsMap = getSelectedPartyDetailsMap(
            newCaseData,
            solStopRepChooseParties,
            selectedPartyDetailsMap
        );
        sendEmailAndUpdateCaseData(selectedPartyDetailsMap, String.valueOf(newCaseData.getId()));
    }

    private void sendEmailAndUpdateCaseData(Map<Optional<SolicitorRole>, Element<PartyDetails>> selectedPartyDetailsMap, String caseId) {
        String systemAuthorisation = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(systemAuthorisation);
        EventRequestData allTabsUpdateEventRequestData = ccdCoreCaseDataService.eventRequest(
            CaseEvent.UPDATE_ALL_TABS,
            systemUpdateUserId
        );
        StartEventResponse allTabsUpdateStartEventResponse =
            ccdCoreCaseDataService.startUpdate(
                systemAuthorisation,
                allTabsUpdateEventRequestData,
                caseId,
                true
            );

        CaseData allTabsUpdateCaseData = CaseUtils.getCaseDataFromStartUpdateEventResponse(
            allTabsUpdateStartEventResponse,
            objectMapper
        );
        for (var entry : selectedPartyDetailsMap.entrySet()) {
            Optional<SolicitorRole> removeSolicitorRole = entry.getKey();
            Element<PartyDetails> newPartyDetailsElement = entry.getValue();
            if (removeSolicitorRole.isPresent() && null != newPartyDetailsElement.getValue().getSolicitorOrg()) {
                allTabsUpdateCaseData = updateAccessCode(allTabsUpdateCaseData, removeSolicitorRole, newPartyDetailsElement);

                DynamicListElement roleItem = DynamicListElement.builder()
                    .code(removeSolicitorRole.get().getCaseRoleLabel())
                    .label(removeSolicitorRole.get().getCaseRoleLabel())
                    .build();

                ChangeOrganisationRequest changeOrganisationRequest = ChangeOrganisationRequest.builder()
                    .caseRoleId(DynamicList.builder()
                                    .value(roleItem)
                                    .listItems(List.of(roleItem))
                                    .build())
                    .build();

                allTabsUpdateCaseData = updateRepresentedPartyDetails(
                    changeOrganisationRequest,
                    allTabsUpdateCaseData,
                    null,
                    TypeOfNocEventEnum.removeLegalRepresentation
                );
            }
        }
        if (null != allTabsUpdateCaseData) {
            tabService.updateAllTabsIncludingConfTabRefactored(
                systemAuthorisation,
                String.valueOf(allTabsUpdateCaseData.getId()),
                allTabsUpdateStartEventResponse,
                allTabsUpdateEventRequestData,
                allTabsUpdateCaseData
            );
            eventPublisher.publishEvent(new CaseDataChanged(allTabsUpdateCaseData));
        }
        for (var entry : selectedPartyDetailsMap.entrySet()) {
            Optional<SolicitorRole> removeSolicitorRole = entry.getKey();
            Element<PartyDetails> newPartyDetailsElement = entry.getValue();
            sendEmailOnRemovalOfLegalRepresentation(
                null,
                newPartyDetailsElement,
                removeSolicitorRole,
                systemAuthorisation,
                String.valueOf(allTabsUpdateCaseData.getId())
            );
        }
    }

    private CaseData updateAccessCode(CaseData allTabsUpdateCaseData,
                                      Optional<SolicitorRole> removeSolicitorRole,
                                      Element<PartyDetails> newPartyDetailsElement) {
        List<Element<CaseInvite>> caseInvites = allTabsUpdateCaseData.getCaseInvites() != null
            ? allTabsUpdateCaseData.getCaseInvites() : new ArrayList<>();
        String accessCode = getAccessCode(allTabsUpdateCaseData, newPartyDetailsElement);
        if (accessCode.equalsIgnoreCase(BLANK_STRING)) {
            generateNewAccessCode(
                allTabsUpdateCaseData,
                newPartyDetailsElement,
                removeSolicitorRole, caseInvites, accessCode);
        } else {
            log.info("Set existing pin citizen after removing legal representation");
        }
        allTabsUpdateCaseData = allTabsUpdateCaseData.toBuilder().caseInvites(caseInvites).build();
        return allTabsUpdateCaseData;
    }

    private Map<Optional<SolicitorRole>, Element<PartyDetails>> getSelectedPartyDetailsMap(CaseData newCaseData,
                                                                                           DynamicMultiSelectList solStopRepChooseParties,
                                                                                           Map<Optional<SolicitorRole>,
                                                                                               Element<PartyDetails>> removeSolPartyDetailsMap) {
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(newCaseData))) {
            solStopRepChooseParties.getValue().stream().forEach(solStopRepChoosePartyElement -> getRemovedSolicitorRolesForC100(
                newCaseData,
                solStopRepChoosePartyElement,
                removeSolPartyDetailsMap
            ));
        } else {
            solStopRepChooseParties.getValue().stream().forEach(solStopRepChoosePartyElement -> getRemovedSolicitorRolesForFl401(
                newCaseData,
                solStopRepChoosePartyElement,
                removeSolPartyDetailsMap
            ));
        }

        return removeSolPartyDetailsMap;
    }

    private static void getRemovedSolicitorRolesForFl401(CaseData newCaseData, DynamicMultiselectListElement solStopRepChoosePartyElement,
                                                         Map<Optional<SolicitorRole>, Element<PartyDetails>> removeSolPartyDetailsMap) {
        Optional<SolicitorRole> removeSolicitorRole;
        if (solStopRepChoosePartyElement.getCode().equalsIgnoreCase(newCaseData.getApplicantsFL401().getPartyId().toString())) {
            removeSolicitorRole = Optional.of(SolicitorRole.FL401APPLICANTSOLICITOR);
            removeSolPartyDetailsMap.put(removeSolicitorRole, element(
                newCaseData.getApplicantsFL401().getPartyId(),
                newCaseData.getApplicantsFL401()
            ));
        } else if (solStopRepChoosePartyElement.getCode().equalsIgnoreCase(newCaseData.getRespondentsFL401().getPartyId().toString())) {
            removeSolicitorRole = Optional.of(SolicitorRole.FL401RESPONDENTSOLICITOR);
            removeSolPartyDetailsMap.put(removeSolicitorRole, element(
                newCaseData.getRespondentsFL401().getPartyId(),
                newCaseData.getRespondentsFL401()
            ));
        }
    }

    private static void getRemovedSolicitorRolesForC100(CaseData newCaseData, DynamicMultiselectListElement solStopRepChoosePartyElement,
                                                        Map<Optional<SolicitorRole>, Element<PartyDetails>> removeSolPartyDetailsMap) {
        Optional<SolicitorRole> removeSolicitorRole;
        boolean matched = false;
        int partyIndex;
        for (Element<PartyDetails> partyDetailsElement : newCaseData.getApplicants()) {
            if (solStopRepChoosePartyElement.getCode().equalsIgnoreCase(partyDetailsElement.getId().toString())) {
                partyIndex = newCaseData.getApplicants().indexOf(partyDetailsElement);
                removeSolicitorRole = SolicitorRole.fromRepresentingAndIndex(CAAPPLICANT, partyIndex + 1);
                removeSolPartyDetailsMap.put(removeSolicitorRole, partyDetailsElement);
                matched = true;
                break;
            }
        }
        if (!matched) {
            for (Element<PartyDetails> partyDetailsElement : newCaseData.getRespondents()) {
                if (solStopRepChoosePartyElement.getCode().equalsIgnoreCase(partyDetailsElement.getId().toString())) {
                    partyIndex = newCaseData.getRespondents().indexOf(partyDetailsElement);
                    removeSolicitorRole = SolicitorRole.fromRepresentingAndIndex(CARESPONDENT, partyIndex + 1);
                    removeSolPartyDetailsMap.put(removeSolicitorRole, partyDetailsElement);
                    break;
                }
            }
        }
    }

    private void findMatchingParty(List<String> errorList,
                                   CaseData caseData,
                                   Element<PartyDetails> partyDetailsElement,
                                   Map<Optional<SolicitorRole>, Element<PartyDetails>> selectedPartyDetailsMap,
                                   SolicitorRole role) {
        Optional<DynamicMultiselectListElement> match = caseData.getSolStopRepChooseParties()
            .getValue()
            .stream()
            .filter(value ->
                        partyDetailsElement.getId().toString().equalsIgnoreCase(value.getCode()))
            .findFirst();

        if (match.isPresent()) {
            selectedPartyDetailsMap.put(Optional.of(role), partyDetailsElement);
        }
    }

    public void updateLegalRepresentation(CallbackRequest callbackRequest, String authorisation, CaseData caseData) {
        if ("amendRespondentsDetails".equalsIgnoreCase(callbackRequest.getEventId())) {
            CaseData oldCaseData = objectMapper.convertValue(
                callbackRequest.getCaseDetailsBefore().getData(),
                CaseData.class
            );
            caseData.getRespondents().stream().forEach(newRepresentedPartyElement -> {
                int respondentIndex = caseData.getRespondents().indexOf(newRepresentedPartyElement);
                Element<PartyDetails> oldRepresentedPartyElement = oldCaseData.getRespondents().get(respondentIndex);
                if (YesNoDontKnow.no.equals(newRepresentedPartyElement.getValue().getDoTheyHaveLegalRepresentation())
                    && YesNoDontKnow.yes.equals(oldRepresentedPartyElement.getValue().getDoTheyHaveLegalRepresentation())) {
                    log.info("oldRespondent ==> " + oldRepresentedPartyElement);
                    UserDetails userDetails = userService.getUserDetails(authorisation);
                    DynamicListElement roleItem = DynamicListElement.builder()
                        .code(CaseRole.C100RESPONDENTSOLICITOR1.formattedName())
                        .label(CaseRole.C100RESPONDENTSOLICITOR1.formattedName())
                        .build();
                    ChangeOrganisationRequest changeOrganisationRequest = ChangeOrganisationRequest.builder()
                        .organisationToRemove(oldRepresentedPartyElement.getValue().getSolicitorOrg())
                        .createdBy(userDetails.getEmail())
                        .caseRoleId(DynamicList.builder()
                                        .value(roleItem)
                                        .listItems(List.of(roleItem))
                                        .build())
                        .approvalStatus(ChangeOrganisationApprovalStatus.APPROVED)
                        .requestTimestamp(time.now())
                        .build();
                    log.info("changeOrganisationRequest ==> " + changeOrganisationRequest);
                    callbackRequest.getCaseDetails().getData()
                        .put("changeOrganisationRequestField", changeOrganisationRequest);
                    /*AboutToStartOrSubmitCallbackResponse response = assignCaseAccessClient.applyDecision(
                        authorisation,
                        tokenGenerator.generate(),
                        decisionRequest(callbackRequest.getCaseDetails())
                    );
                    log.info("applyDecision response ==> " + response);
                    if (changeOrganisationRequest != null) {
                        sendEmailOnRemovalOfLegalRepresentation(
                            caseData,
                            oldRepresentedPartyElement,
                            newRepresentedPartyElement,
                            changeOrganisationRequest
                        );
                    }*/
                }
            });
        }
    }

    private String getAccessCode(CaseData caseData, Element<PartyDetails> partyDetails) {
        if (CollectionUtils.isNotEmpty(caseData.getCaseInvites())) {
            if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                for (Element<CaseInvite> caseInviteElement : caseData.getCaseInvites()) {
                    if (partyDetails.getId().equals(caseInviteElement.getValue().getPartyId())) {
                        return caseInviteElement.getValue().getAccessCode();
                    }
                }
            } else {
                for (Element<CaseInvite> caseInviteElement : caseData.getCaseInvites()) {
                    if (partyDetails.getValue().getEmail().equals(caseInviteElement.getValue().getCaseInviteEmail())) {
                        return caseInviteElement.getValue().getAccessCode();
                    }
                }
            }
        }
        return "";
    }

    private void sendEmailOnRemovalOfLegalRepresentation(Element<PartyDetails> oldPartyDetails,
                                                                              Element<PartyDetails> newPartyDetails,
                                                                              Optional<SolicitorRole> solicitorRole,
                                                                              String systemAuthorisation,
                                                                              String caseId) {
        CaseDetails caseDetails = ccdCoreCaseDataService.findCaseById(systemAuthorisation, caseId);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        String solicitorName = null != oldPartyDetails ? oldPartyDetails.getValue().getRepresentativeFirstName()
            + " " + oldPartyDetails.getValue().getRepresentativeLastName() : newPartyDetails.getValue().getRepresentativeFirstName()
            + " " + newPartyDetails.getValue().getRepresentativeLastName();
        List<Element<CaseInvite>> caseInvites = caseData.getCaseInvites() != null ? caseData.getCaseInvites() : new ArrayList<>();
        String accessCode = getAccessCode(caseData, newPartyDetails);
        NoticeOfChangeEvent noticeOfChangeEvent = prepareNoticeOfChangeEvent(
            caseData,
            solicitorRole,
            solicitorName,
            null != oldPartyDetails
                ? oldPartyDetails.getValue().getSolicitorEmail() : newPartyDetails.getValue().getSolicitorEmail(),
            TypeOfNocEventEnum.removeLegalRepresentation.getDisplayedValue(),
            accessCode
        );
        log.info("noticeOfChangeEvent remove ==> " + noticeOfChangeEvent);
        eventPublisher.publishEvent(noticeOfChangeEvent);

    }

    private String generateNewAccessCode(CaseData caseData, Element<PartyDetails> newPartyDetails,
                                         Optional<SolicitorRole> solicitorRole,
                                         List<Element<CaseInvite>> caseInvites, String accessCode) {
        CaseInvite caseInvite = caseInviteManager.generatePinAfterLegalRepresentationRemoved(
            caseData,
            newPartyDetails,
            solicitorRole.get()
        );
        if (null != caseInvite) {
            log.info("New pin generated for citizen after removing legal representation");
            accessCode = caseInvite.getAccessCode();
            caseInvites.add(element(caseInvite));
        }
        return accessCode;
    }

    private List<Element<PartyDetails>> findSolicitorRepresentedParties(CaseData caseData, String authorisation) {
        List<Element<PartyDetails>> solicitorRepresentedParties = new ArrayList<>();
        FindUserCaseRolesResponse findUserCaseRolesResponse
            = findUserCaseRoles(String.valueOf(caseData.getId()), authorisation);

        if (findUserCaseRolesResponse != null) {
            log.info("findUserCaseRolesResponse is not null ");
            solicitorRepresentedParties = getSolicitorRepresentedParties(
                caseData,
                findUserCaseRolesResponse
            );
        }
        return solicitorRepresentedParties;
    }

    private List<Element<PartyDetails>> getSolicitorRepresentedParties(CaseData caseData, FindUserCaseRolesResponse findUserCaseRolesResponse) {
        List<Element<PartyDetails>> solicitorRepresentedParties = new ArrayList<>();
        for (CaseUser caseUser : findUserCaseRolesResponse.getCaseUsers()) {
            log.info("caseUser is = " + caseUser + " and roles are " + caseUser.getCaseRole());
            SolicitorRole.fromCaseRoleLabel(caseUser.getCaseRole()).ifPresent(
                x -> {
                    switch (x.getRepresenting()) {
                        case CAAPPLICANT:
                            solicitorRepresentedParties.add(caseData.getApplicants().get(x.getIndex()));
                            break;
                        case CARESPONDENT:
                            solicitorRepresentedParties.add(caseData.getRespondents().get(x.getIndex()));
                            break;
                        case DAAPPLICANT:
                            solicitorRepresentedParties.add(ElementUtils.element(
                                caseData.getApplicantsFL401().getPartyId(),
                                caseData.getApplicantsFL401()
                            ));
                            break;
                        case DARESPONDENT:
                            solicitorRepresentedParties.add(ElementUtils.element(
                                caseData.getRespondentsFL401().getPartyId(),
                                caseData.getRespondentsFL401()
                            ));
                            break;
                        default:
                            break;
                    }
                }
            );
        }
        log.info("finding solicitorRepresentedParties Party " + solicitorRepresentedParties);
        return solicitorRepresentedParties;
    }

    private FindUserCaseRolesResponse findUserCaseRoles(String caseId, String authorisation) {
        log.info("findUserCaseRoles : caseId is:: " + caseId);
        FindUserCaseRolesResponse findUserCaseRolesResponse = userDataStoreService.findUserCaseRoles(
            caseId,
            authorisation
        );
        log.info("findUserCaseRolesResponse = " + findUserCaseRolesResponse);
        return findUserCaseRolesResponse;
    }

    public Map<String, Object> populateAboutToStartAdminRemoveLegalRepresentative(String authorisation,
                                                                                  CallbackRequest callbackRequest,
                                                                                  List<String> errorList) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        DynamicMultiSelectList removeLegalRepAndPartiesList
            = dynamicMultiSelectListService.getRemoveLegalRepAndPartiesList(caseData);

        if (removeLegalRepAndPartiesList.getListItems().isEmpty()) {
            errorList.add(CASE_NOT_REPRESENTED_BY_SOLICITOR_ERROR);
        } else {
            caseDataUpdated.put(REMOVE_LEGAL_REPRESENTATIVE_AND_PARTIES_LIST, removeLegalRepAndPartiesList);
        }
        return caseDataUpdated;
    }

    public Map<String, Object> aboutToSubmitAdminRemoveLegalRepresentative(String authorisation,
                                                                           CallbackRequest callbackRequest,
                                                                           List<String> errorList) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails, objectMapper);
        Map<Optional<SolicitorRole>, Element<PartyDetails>> selectedPartyDetailsMap = new HashMap<>();
        DynamicMultiSelectList removeLegalRepAndPartiesList = caseData.getRemoveLegalRepAndPartiesList();
        Map<String, Object> caseDataUpdated = caseDetails.getData();
        selectedPartyDetailsMap = getSelectedPartyDetailsMap(
            caseData,
            removeLegalRepAndPartiesList,
            selectedPartyDetailsMap
        );

        caseDataUpdated = createChangeOrgReqAndRemoveRepresentative(
            authorisation,
            caseDetails,
            selectedPartyDetailsMap,
            caseDataUpdated
        );
        return caseDataUpdated;
    }

    public SubmittedCallbackResponse submittedAdminRemoveLegalRepresentative(CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        DynamicMultiSelectList removeLegalRepAndPartiesList = caseData.getRemoveLegalRepAndPartiesList();
        Map<Optional<SolicitorRole>, Element<PartyDetails>> selectedPartyDetailsMap = new HashMap<>();
        selectedPartyDetailsMap = getSelectedPartyDetailsMap(
            caseData,
            removeLegalRepAndPartiesList,
            selectedPartyDetailsMap
        );
        sendEmailAndUpdateCaseData(selectedPartyDetailsMap, String.valueOf(caseData.getId()));

        return prepareSubmittedCallbackResponse(selectedPartyDetailsMap);
    }

    private static SubmittedCallbackResponse prepareSubmittedCallbackResponse(Map<Optional<SolicitorRole>,
        Element<PartyDetails>> selectedPartyDetailsMap) {
        StringBuilder legalRepAndLipNames = new StringBuilder();
        Map<String, List<String>> legalRepAndLipNameMapping = new HashMap<>();
        selectedPartyDetailsMap.forEach((key, value) -> {
            String representedSolicitorName = value.getValue().getRepresentativeFirstName() + EMPTY_SPACE_STRING
                + value.getValue().getRepresentativeLastName();
            String partyName = value.getValue().getFirstName() + EMPTY_SPACE_STRING
                + value.getValue().getLastName();
            List<String> clientNames = new ArrayList<>();
            if (legalRepAndLipNameMapping.containsKey(representedSolicitorName)) {
                clientNames = legalRepAndLipNameMapping.get(representedSolicitorName);
            }
            clientNames.add(partyName);
            legalRepAndLipNameMapping.put(representedSolicitorName, clientNames);
        });

        legalRepAndLipNameMapping.forEach((key, value) -> legalRepAndLipNames.append(key)
            .append(" is no longer representing ")
            .append(String.join(", ", value))
            .append(" in this case. All other parties have been notified about this change\n\n")
        );
        String representativeRemovedBodyPrefix = legalRepAndLipNames.append(REPRESENTATIVE_REMOVED_STATUS_LABEL).toString();
        return SubmittedCallbackResponse.builder().confirmationHeader(
            REPRESENTATIVE_REMOVED_LABEL).confirmationBody(
            representativeRemovedBodyPrefix
        ).build();
    }
}
