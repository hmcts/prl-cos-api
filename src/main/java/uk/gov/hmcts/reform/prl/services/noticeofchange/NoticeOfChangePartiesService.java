package uk.gov.hmcts.reform.prl.services.noticeofchange;

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
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.CaseRole;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.TypeOfNocEventEnum;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrgSolicitors;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.SolicitorUser;
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
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessClient;
import uk.gov.hmcts.reform.prl.services.caseaccess.CcdDataStoreService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
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
import java.util.UUID;

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
    public static final String CASE_NOT_REPRESENTED_BY_SOLICITOR_ERROR = "This case is not represented by solicitor anymore.";
    public static final String SOL_STOP_REP_CHOOSE_PARTIES = "solStopRepChooseParties";

    public static final String REMOVE_LEGAL_REPRESENTATIVE_AND_PARTIES_LIST = "removeLegalRepAndPartiesList";
    public static final String IS_NO_LONGER_REPRESENTING = " is no longer representing ";
    public static final String IN_THIS_CASE = " in this case.";
    public static final String ALL_OTHER_PARTIES_HAVE_BEEN_NOTIFIED_ABOUT_THIS_CHANGE = " All other parties have been notified about this change\n\n";
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
    private final OrganisationService organisationService;

    private final CaseEventService caseEventService;
    private final PartyLevelCaseFlagsService partyLevelCaseFlagsService;

    public static final String REPRESENTATIVE_REMOVED_LABEL = "# Representative removed";

    public static final String REPRESENTATIVE_REMOVED_STATUS_LABEL = """
        ### What happens next

        The court will consider your withdrawal request.""";

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

                if (!DAAPPLICANT.equals(representing)) {
                    OrganisationPolicy organisationPolicy = policyConverter.daGenerate(
                        solicitorRole, daElements
                    );
                    data.put(representing.getPolicyFieldTemplate(), organisationPolicy);
                }
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

        return assignCaseAccessClient.applyDecision(
            authorisation,
            tokenGenerator.generate(),
            decisionRequest(caseDetails)
        );
    }

    public void nocRequestSubmitted(CallbackRequest callbackRequest) {
        CaseData oldCaseData = getCaseData(callbackRequest.getCaseDetailsBefore(), objectMapper);

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

        OrgSolicitors orgSolicitors = organisationService.getOrganisationSolicitorDetails(
            systemAuthorisation,
            changeOrganisationRequest.getOrganisationToAdd().getOrganisationID()
        );

        Organisations organisations =
            organisationService.getOrganisationDetails(
                systemAuthorisation,
                changeOrganisationRequest.getOrganisationToAdd().getOrganisationID()
            );

        Optional<SolicitorUser> solicitorDetails = Optional.empty();
        if (null != orgSolicitors
            && null != orgSolicitors.getUsers()
            && !orgSolicitors.getUsers().isEmpty()) {
            solicitorDetails = orgSolicitors.getUsers()
                .stream()
                .filter(x -> changeOrganisationRequest.getCreatedBy().equalsIgnoreCase(
                    x.getEmail()))
                .findFirst();
        }

        if (solicitorDetails.isPresent()) {

            allTabsUpdateCaseData = updateRepresentedPartyDetails(
                changeOrganisationRequest,
                allTabsUpdateCaseData,
                solicitorDetails.get(),
                TypeOfNocEventEnum.addLegalRepresentation,
                organisations
            );
        } else {
            log.error(
                "Notice of change: Solicitor %s does not belong to organisation id %s",
                changeOrganisationRequest.getCreatedBy(),
                changeOrganisationRequest.getOrganisationToAdd().getOrganisationID()
            );
        }

        tabService.updatePartyDetailsForNoc(
            null,
            systemAuthorisation,
            String.valueOf(allTabsUpdateCaseData.getId()),
            allTabsUpdateStartEventResponse,
            allTabsUpdateEventRequestData,
            allTabsUpdateCaseData
        );

        CaseDetails caseDetails = ccdCoreCaseDataService.findCaseById(
            systemAuthorisation,
            String.valueOf(allTabsUpdateCaseData.getId())
        );
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        eventPublisher.publishEvent(new CaseDataChanged(caseData));
        Optional<SolicitorRole> solicitorRole = getSolicitorRole(changeOrganisationRequest);
        sendEmailOnAddLegalRepresentative(
            caseData,
            changeOrganisationRequest,
            solicitorDetails,
            solicitorRole
        );
    }

    private void sendEmailOnAddLegalRepresentative(CaseData caseData,
                                                   ChangeOrganisationRequest changeOrganisationRequest, Optional<SolicitorUser> lrDetails,
                                                   Optional<SolicitorRole> solicitorRole) {

        String solicitorName = lrDetails.isPresent() ? lrDetails.get().getFirstName() + " " + lrDetails.get().getLastName() : "";

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

    private CaseData updateRepresentedPartyDetails(ChangeOrganisationRequest changeOrganisationRequest,
                                                   CaseData caseData,
                                                   SolicitorUser legalRepresentativeSolicitorDetails,
                                                   TypeOfNocEventEnum typeOfNocEvent,
                                                   Organisations organisations) {
        Optional<SolicitorRole> solicitorRole = getSolicitorRole(changeOrganisationRequest);
        if (solicitorRole.isPresent()) {
            int partyIndex = solicitorRole.get().getIndex();
            if (CARESPONDENT.equals(solicitorRole.get().getRepresenting())
                && C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                List<Element<PartyDetails>> respondents = CARESPONDENT.getCaTarget().apply(caseData);
                caseData = updateC100PartyDetails(
                    partyIndex,
                    respondents,
                    legalRepresentativeSolicitorDetails,
                    caseData,
                    CARESPONDENT,
                    typeOfNocEvent,
                    organisations
                );
            } else if (CAAPPLICANT.equals(solicitorRole.get().getRepresenting())
                && C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {

                List<Element<PartyDetails>> applicants = CAAPPLICANT.getCaTarget().apply(caseData);
                caseData = updateC100PartyDetails(
                    partyIndex,
                    applicants,
                    legalRepresentativeSolicitorDetails,
                    caseData,
                    CAAPPLICANT,
                    typeOfNocEvent,
                    organisations
                );
            } else if (DAAPPLICANT.equals(solicitorRole.get().getRepresenting())
                && FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                caseData = updateFl401PartyDetails(legalRepresentativeSolicitorDetails,
                                                   caseData,
                                                   DAAPPLICANT, typeOfNocEvent, organisations
                );
            } else if (DARESPONDENT.equals(solicitorRole.get().getRepresenting())
                && FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                caseData = updateFl401PartyDetails(legalRepresentativeSolicitorDetails,
                                                   caseData,
                                                   DARESPONDENT, typeOfNocEvent, organisations
                );
            }
        }
        return caseData;
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
                                            SolicitorUser legalRepresentativeSolicitorDetails,
                                            CaseData caseData,
                                            SolicitorRole.Representing representing,
                                            TypeOfNocEventEnum typeOfNocEvent,
                                            Organisations organisations) {
        Element<PartyDetails> partyDetailsElement = parties.get(partyIndex);
        PartyDetails updPartyDetails = updatePartyDetails(
            legalRepresentativeSolicitorDetails,
            partyDetailsElement.getValue(),
            typeOfNocEvent,
            organisations
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
            if (TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)) {
                caseData = partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
                    caseData, partyIndex, PartyRole.Representing.CARESPONDENTSOLICITOR, true);
            } else {
                caseData = partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
                    caseData, partyIndex, PartyRole.Representing.CARESPONDENTSOLICITOR, false);
            }
        } else if (CAAPPLICANT.equals(representing)) {
            updatedRepresentedRespondentElement = ElementUtils
                .element(partyDetailsElement.getId(), updPartyDetails);
            caseData.getApplicants().set(partyIndex, updatedRepresentedRespondentElement);
            if (TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)) {
                caseData = partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
                    caseData,
                    partyIndex,
                    PartyRole.Representing.CAAPPLICANTSOLICITOR, true
                );
            } else {
                caseData = partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
                    caseData,
                    partyIndex,
                    PartyRole.Representing.CAAPPLICANTSOLICITOR, false
                );
            }
        }
        return caseData;
    }

    private CaseData updateFl401PartyDetails(SolicitorUser legalRepresentativeSolicitorDetails,
                                             CaseData caseData,
                                             SolicitorRole.Representing representing,
                                             TypeOfNocEventEnum typeOfNocEvent,
                                             Organisations organisations) {
        if (DAAPPLICANT.equals(representing)) {
            PartyDetails updPartyDetails = updatePartyDetails(
                legalRepresentativeSolicitorDetails,
                caseData.getApplicantsFL401(),
                typeOfNocEvent,
                organisations
            );
            caseData = caseData.toBuilder().applicantsFL401(updPartyDetails).build();
            if (TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)) {
                caseData = partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
                    caseData,
                    0,
                    PartyRole.Representing.DAAPPLICANTSOLICITOR, true
                );
            } else {
                caseData = partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
                    caseData,
                    0,
                    PartyRole.Representing.DAAPPLICANTSOLICITOR, false
                );
            }
        } else if (DARESPONDENT.equals(representing)) {
            PartyDetails updPartyDetails = updatePartyDetails(
                legalRepresentativeSolicitorDetails,
                caseData.getRespondentsFL401(),
                typeOfNocEvent,
                organisations
            );
            caseData = caseData.toBuilder().respondentsFL401(updPartyDetails).build();
            if (TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)) {
                caseData = partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
                    caseData,
                    0,
                    PartyRole.Representing.DARESPONDENTSOLICITOR, true
                );
            } else {
                caseData = partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
                    caseData,
                    0,
                    PartyRole.Representing.DARESPONDENTSOLICITOR, false
                );
            }
        }
        return caseData;
    }

    private static PartyDetails updatePartyDetails(SolicitorUser legalRepresentativeSolicitorDetails,
                                                   PartyDetails partyDetails,
                                                   TypeOfNocEventEnum typeOfNocEvent,
                                                   Organisations organisations) {

        partyDetails = partyDetails.toBuilder()
            .user(partyDetails.getUser().toBuilder()
                      .solicitorRepresented(TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)
                                                ? YesOrNo.Yes : YesOrNo.No)
                      .build())
            .doTheyHaveLegalRepresentation(TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)
                                               ? YesNoDontKnow.yes : YesNoDontKnow.no)
            .solicitorEmail(TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)
                                ? legalRepresentativeSolicitorDetails.getEmail() : null)
            .representativeFirstName(TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)
                                         ? legalRepresentativeSolicitorDetails.getFirstName() : null)
            .representativeLastName(TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)
                                        ? legalRepresentativeSolicitorDetails.getLastName() : null)
            .solicitorPartyId(TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)
                                  && null == partyDetails.getSolicitorPartyId() ? UUID.randomUUID()
                                  : partyDetails.getSolicitorPartyId())
            .response(null != partyDetails.getResponse()
                          && YesOrNo.Yes.equals(partyDetails.getResponse().getC7ResponseSubmitted())
                          ? partyDetails.getResponse() : Response.builder().build())
            .isRemoveLegalRepresentativeRequested(TypeOfNocEventEnum.removeLegalRepresentation.equals(typeOfNocEvent)
                                                      && YesOrNo.Yes.equals(partyDetails.getIsRemoveLegalRepresentativeRequested())
                                                      ? YesOrNo.No : partyDetails.getIsRemoveLegalRepresentativeRequested())
            .build();

        if (organisations != null && TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)) {
            partyDetails = partyDetails.toBuilder()
                .solicitorOrg(Organisation.builder().organisationID(organisations.getOrganisationIdentifier()).organisationName(
                    organisations.getName()).build()).build();
        } else if (TypeOfNocEventEnum.removeLegalRepresentation.equals(typeOfNocEvent)) {
            partyDetails = partyDetails.toBuilder().solicitorOrg(Organisation.builder().build()).build();
        }
        return partyDetails;
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
            } else if (DARESPONDENT.equals(solicitorRole.getRepresenting())) {
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
                                                             CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails, objectMapper);
        Map<Optional<SolicitorRole>, Element<PartyDetails>> selectedPartyDetailsMap = new HashMap<>();
        FindUserCaseRolesResponse findUserCaseRolesResponse
            = findUserCaseRoles(String.valueOf(caseDetails.getId()), authorisation);
        Map<String, Object> caseDataUpdated = caseDetails.getData();

        for (CaseUser caseUser : findUserCaseRolesResponse.getCaseUsers()) {
            SolicitorRole.fromCaseRoleLabel(caseUser.getCaseRole()).ifPresent(
                x -> {
                    switch (x.getRepresenting()) {
                        case CAAPPLICANT:
                            findMatchingParty(
                                caseData,
                                caseData.getApplicants().get(x.getIndex()),
                                selectedPartyDetailsMap,
                                x
                            );
                            break;
                        case CARESPONDENT:
                            findMatchingParty(
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
        getSelectedPartyDetailsMap(
            newCaseData,
            solStopRepChooseParties,
            selectedPartyDetailsMap
        );
        sendEmailAndUpdateCaseData(selectedPartyDetailsMap, String.valueOf(newCaseData.getId()));
    }

    private void sendEmailAndUpdateCaseData(
        Map<Optional<SolicitorRole>,
            Element<PartyDetails>> selectedPartyDetailsMap,
        String caseId) {
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
        List<Element<CaseInvite>> caseInvites = new ArrayList<>();
        for (var entry : selectedPartyDetailsMap.entrySet()) {
            Optional<SolicitorRole> removeSolicitorRole = entry.getKey();
            Element<PartyDetails> newPartyDetailsElement = entry.getValue();
            if (removeSolicitorRole.isPresent() && null != newPartyDetailsElement.getValue().getSolicitorOrg()) {
                List<CaseEventDetail> eventsForCase = caseEventService.findEventsForCase(String.valueOf(caseId));
                for (CaseEventDetail eventDetail : eventsForCase) {
                    if (State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue().equalsIgnoreCase(eventDetail.getStateId())) {
                        log.info("Case in hearing state, hence updating access code");
                        caseInvites = updateAccessCode(
                            allTabsUpdateCaseData,
                            removeSolicitorRole,
                            newPartyDetailsElement
                        );
                        break;
                    }
                }


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
                    TypeOfNocEventEnum.removeLegalRepresentation,
                    null
                );
            }
        }

        tabService.updatePartyDetailsForNoc(
            caseInvites,
            systemAuthorisation,
            String.valueOf(allTabsUpdateCaseData.getId()),
            allTabsUpdateStartEventResponse,
            allTabsUpdateEventRequestData,
            allTabsUpdateCaseData
        );
        CaseDetails caseDetails = ccdCoreCaseDataService.findCaseById(systemAuthorisation, caseId);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        eventPublisher.publishEvent(new CaseDataChanged(caseData));

        for (var entry : selectedPartyDetailsMap.entrySet()) {
            Optional<SolicitorRole> removeSolicitorRole = entry.getKey();
            Element<PartyDetails> newPartyDetailsElement = entry.getValue();
            sendEmailOnRemovalOfLegalRepresentation(
                null,
                newPartyDetailsElement,
                removeSolicitorRole,
                caseData
            );
        }
    }

    private List<Element<CaseInvite>> updateAccessCode(CaseData allTabsUpdateCaseData,
                                                       Optional<SolicitorRole> removeSolicitorRole,
                                                       Element<PartyDetails> newPartyDetailsElement) {
        List<Element<CaseInvite>> caseInvites = allTabsUpdateCaseData.getCaseInvites() != null
            ? allTabsUpdateCaseData.getCaseInvites() : new ArrayList<>();
        String accessCode = getAccessCode(allTabsUpdateCaseData, newPartyDetailsElement);
        if (accessCode.equalsIgnoreCase(BLANK_STRING)) {
            generateNewAccessCode(
                allTabsUpdateCaseData,
                newPartyDetailsElement,
                removeSolicitorRole, caseInvites
            );
        } else {
            log.info("Set existing pin citizen after removing legal representation");
        }
        return caseInvites;
    }

    private void getSelectedPartyDetailsMap(CaseData newCaseData,
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

    private void findMatchingParty(CaseData caseData,
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
                    callbackRequest.getCaseDetails().getData()
                        .put("changeOrganisationRequestField", changeOrganisationRequest);
                    AboutToStartOrSubmitCallbackResponse response = assignCaseAccessClient.applyDecision(
                        authorisation,
                        tokenGenerator.generate(),
                        decisionRequest(callbackRequest.getCaseDetails())
                    );
                    log.info("applyDecision response ==> " + response);
                }
            });
        }
    }

    private String getAccessCode(CaseData caseData, Element<PartyDetails> partyDetails) {
        String accessCode = "";
        String caseTypeOfApplication = CaseUtils.getCaseTypeOfApplication(caseData);
        if (CollectionUtils.isNotEmpty(caseData.getCaseInvites())) {
            for (Element<CaseInvite> caseInviteElement : caseData.getCaseInvites()) {
                if ((C100_CASE_TYPE.equalsIgnoreCase(caseTypeOfApplication)
                    && partyDetails.getId().equals(caseInviteElement.getValue().getPartyId()))
                    || (FL401_CASE_TYPE.equalsIgnoreCase(caseTypeOfApplication)
                    && partyDetails.getValue().getEmail().equals(caseInviteElement.getValue().getCaseInviteEmail()))) {
                    accessCode = caseInviteElement.getValue().getAccessCode();
                }
            }
        }
        return accessCode;
    }

    private void sendEmailOnRemovalOfLegalRepresentation(Element<PartyDetails> oldPartyDetails,
                                                         Element<PartyDetails> newPartyDetails,
                                                         Optional<SolicitorRole> solicitorRole,
                                                         CaseData caseData) {


        String solicitorName = null != oldPartyDetails ? oldPartyDetails.getValue().getRepresentativeFirstName()
            + " " + oldPartyDetails.getValue().getRepresentativeLastName() : newPartyDetails.getValue().getRepresentativeFirstName()
            + " " + newPartyDetails.getValue().getRepresentativeLastName();
        String accessCode = getAccessCode(caseData, newPartyDetails);

        List<Element<CaseInvite>> caseInvites = caseData.getCaseInvites() != null
            ? caseData.getCaseInvites() : new ArrayList<>();
        if (accessCode.equalsIgnoreCase(BLANK_STRING)) {
            generateNewAccessCode(
                caseData,
                newPartyDetails,
                solicitorRole, caseInvites
            );
        } else {
            log.info("Set existing pin citizen after removing legal representation");
        }

        NoticeOfChangeEvent noticeOfChangeEvent = prepareNoticeOfChangeEvent(
            caseData,
            solicitorRole,
            solicitorName,
            null != oldPartyDetails
                ? oldPartyDetails.getValue().getSolicitorEmail() : newPartyDetails.getValue().getSolicitorEmail(),
            TypeOfNocEventEnum.removeLegalRepresentation.getDisplayedValue(),
            accessCode
        );
        eventPublisher.publishEvent(noticeOfChangeEvent);

    }

    private void generateNewAccessCode(CaseData caseData, Element<PartyDetails> newPartyDetails,
                                       Optional<SolicitorRole> solicitorRole,
                                       List<Element<CaseInvite>> caseInvites) {
        CaseInvite caseInvite = caseInviteManager.generatePinAfterLegalRepresentationRemoved(
            newPartyDetails,
            solicitorRole.orElse(null)
        );
        if (null != caseInvite) {
            log.info("New pin generated for citizen after removing legal representation");
            caseInvites.add(element(caseInvite));
            caseData.setCaseInvites(caseInvites);
        }
    }

    private List<Element<PartyDetails>> findSolicitorRepresentedParties(CaseData caseData, String authorisation) {
        List<Element<PartyDetails>> solicitorRepresentedParties = new ArrayList<>();
        FindUserCaseRolesResponse findUserCaseRolesResponse
            = findUserCaseRoles(String.valueOf(caseData.getId()), authorisation);

        if (findUserCaseRolesResponse != null) {
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
        return solicitorRepresentedParties;
    }

    private FindUserCaseRolesResponse findUserCaseRoles(String caseId, String authorisation) {
        return userDataStoreService.findUserCaseRoles(
            caseId,
            authorisation
        );
    }

    public Map<String, Object> populateAboutToStartAdminRemoveLegalRepresentative(CallbackRequest callbackRequest,
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
                                                                           CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails, objectMapper);
        Map<Optional<SolicitorRole>, Element<PartyDetails>> selectedPartyDetailsMap = new HashMap<>();
        DynamicMultiSelectList removeLegalRepAndPartiesList = caseData.getRemoveLegalRepAndPartiesList();
        Map<String, Object> caseDataUpdated = caseDetails.getData();
        getSelectedPartyDetailsMap(
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
        getSelectedPartyDetailsMap(
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

        legalRepAndLipNames.append("\n");
        legalRepAndLipNameMapping.forEach((key, value) -> legalRepAndLipNames.append(key)
            .append(IS_NO_LONGER_REPRESENTING)
            .append(String.join(", ", value))
            .append(IN_THIS_CASE)
        );
        String representativeRemovedBodyPrefix = legalRepAndLipNames.append(
                ALL_OTHER_PARTIES_HAVE_BEEN_NOTIFIED_ABOUT_THIS_CHANGE)
            .append(REPRESENTATIVE_REMOVED_STATUS_LABEL).toString();
        return SubmittedCallbackResponse.builder().confirmationHeader(
            REPRESENTATIVE_REMOVED_LABEL).confirmationBody(
            representativeRemovedBodyPrefix
        ).build();
    }
}
