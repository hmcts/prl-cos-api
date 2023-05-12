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
import uk.gov.hmcts.reform.prl.enums.noticeofchange.CaseRole;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.noticeofchange.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.prl.models.noticeofchange.NoticeOfChangeParties;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessClient;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
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

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DARESPONDENT;
import static uk.gov.hmcts.reform.prl.models.noticeofchange.DecisionRequest.decisionRequest;
import static uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService.NoticeOfChangeAnswersPopulationStrategy.BLANK;
import static uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService.NoticeOfChangeAnswersPopulationStrategy.POPULATE;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getCaseData;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class NoticeOfChangePartiesService {
    public static final String NO_REPRESENTATION_FOUND_ERROR = "You do not represent anyone in this case.";
    public static final String SOL_STOP_REP_CHOOSE_PARTIES = "solStopRepChooseParties";
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

        String solicitorName = legalRepresentativeSolicitorDetails.getFullName();

        if (changeOrganisationRequest != null) {
            NoticeOfChangeEvent noticeOfChangeEvent = prepareNoticeOfChangeEvent(
                newCaseData,
                solicitorRole,
                solicitorName,
                changeOrganisationRequest.getCreatedBy(),
                "add"
            );
            eventPublisher.publishEvent(noticeOfChangeEvent);
        }

        eventPublisher.publishEvent(new CaseDataChanged(newCaseData));
    }

    private CaseData getRepresentedPartyDetails(ChangeOrganisationRequest changeOrganisationRequest,
                                                CaseData caseData,
                                                UserDetails legalRepresentativeSolicitorDetails) {
        Optional<SolicitorRole> solicitorRole = getSolicitorRole(changeOrganisationRequest);
        if (solicitorRole.isPresent()) {
            int partyIndex = solicitorRole.get().getIndex();
            if (CARESPONDENT.equals(solicitorRole.get().getRepresenting())
                && C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                List<Element<PartyDetails>> respondents = CARESPONDENT.getCaTarget().apply(caseData);
                return updateC100PartyDetails(partyIndex, respondents, legalRepresentativeSolicitorDetails,
                                              changeOrganisationRequest, caseData, CARESPONDENT
                );
            } else if (CAAPPLICANT.equals(solicitorRole.get().getRepresenting())
                && C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {

                List<Element<PartyDetails>> applicants = CAAPPLICANT.getCaTarget().apply(caseData);
                return updateC100PartyDetails(partyIndex, applicants, legalRepresentativeSolicitorDetails,
                                              changeOrganisationRequest, caseData, CAAPPLICANT
                );
            } else if (DAAPPLICANT.equals(solicitorRole.get().getRepresenting())
                && FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                return updateFl401PartyDetails(legalRepresentativeSolicitorDetails,
                                               changeOrganisationRequest, caseData,
                                               DAAPPLICANT
                );
            } else if (DARESPONDENT.equals(solicitorRole.get().getRepresenting())
                && FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                return updateFl401PartyDetails(legalRepresentativeSolicitorDetails,
                                               changeOrganisationRequest, caseData,
                                               DARESPONDENT
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
                                            SolicitorRole.Representing representing) {
        Element<PartyDetails> partyDetailsElement = parties.get(partyIndex);
        PartyDetails updPartyDetails = partyDetailsElement.getValue().toBuilder()
            .user(partyDetailsElement.getValue().getUser().toBuilder()
                      .solicitorRepresented(YesOrNo.Yes)
                      .build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorEmail(changeOrganisationRequest.getCreatedBy())
            .representativeFirstName(legalRepresentativeSolicitorDetails.getFullName())
            .representativeLastName(legalRepresentativeSolicitorDetails.getSurname().orElse(""))
            .solicitorOrg(changeOrganisationRequest.getOrganisationToAdd())
            .build();

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
                                             SolicitorRole.Representing representing) {
        CaseData updatedCaseData = null;

        if (DAAPPLICANT.equals(representing)) {
            PartyDetails updPartyDetails = caseData.getApplicantsFL401().toBuilder()
                .user(caseData.getApplicantsFL401().getUser().toBuilder()
                          .solicitorRepresented(YesOrNo.Yes)
                          .build())
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .solicitorEmail(changeOrganisationRequest.getCreatedBy())
                .representativeFirstName(legalRepresentativeSolicitorDetails.getFullName())
                .representativeLastName(legalRepresentativeSolicitorDetails.getSurname().orElse(""))
                .solicitorOrg(changeOrganisationRequest.getOrganisationToAdd())
                .build();
            updatedCaseData = caseData.toBuilder().applicantsFL401(updPartyDetails).build();
        } else if (DARESPONDENT.equals(representing)) {
            PartyDetails updPartyDetails = caseData.getRespondentsFL401().toBuilder()
                .user(caseData.getRespondentsFL401().getUser().toBuilder()
                          .solicitorRepresented(YesOrNo.Yes)
                          .build())
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .solicitorEmail(changeOrganisationRequest.getCreatedBy())
                .representativeFirstName(legalRepresentativeSolicitorDetails.getFullName())
                .representativeLastName(legalRepresentativeSolicitorDetails.getSurname().orElse(""))
                .solicitorOrg(changeOrganisationRequest.getOrganisationToAdd())
                .build();
            updatedCaseData = caseData.toBuilder().respondentsFL401(updPartyDetails).build();
        }

        return updatedCaseData;
    }

    private NoticeOfChangeEvent prepareNoticeOfChangeEvent(CaseData newCaseData,
                                                           Optional<SolicitorRole> solicitorRole,
                                                           String solicitorName,
                                                           String solicitorEmailAddress,
                                                           String typeOfEvent) {
        if (solicitorRole.isPresent()) {
            int partyIndex = solicitorRole.get().getIndex();
            return NoticeOfChangeEvent.builder()
                .caseData(newCaseData)
                .solicitorEmailAddress(solicitorEmailAddress)
                .solicitorName(solicitorName)
                .representedPartyIndex(partyIndex)
                .representing(solicitorRole.get().getRepresenting())
                .typeOfEvent(typeOfEvent)
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
        DynamicMultiSelectList solicitorRepresentedParties
            = dynamicMultiSelectListService.getSolicitorRepresentedParties(
            authorisation, getCaseData(callbackRequest.getCaseDetails(), objectMapper));

        if (solicitorRepresentedParties.getListItems().isEmpty()) {
            errorList.add(NO_REPRESENTATION_FOUND_ERROR);
        } else {
            caseDataUpdated.put(SOL_STOP_REP_CHOOSE_PARTIES, solicitorRepresentedParties);
        }
        return caseDataUpdated;
    }

    public Map<String, Object> aboutToSubmitStopRepresenting(String authorisation, CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        List<Optional<PartyDetails>> selectedPartyDetailsList = new ArrayList<>();
        log.info("selectedPartyDetailsList size is ::" + selectedPartyDetailsList.size());
        log.info("selectedPartyDetailsList is ::" + selectedPartyDetailsList);
        caseData.getSolStopRepChooseParties().getValue().forEach(value -> {
            log.info("list code is ::" + value.getCode());
            log.info("list code label is ::" + value.getLabel());
            if (null != caseData.getRespondents()) {
                log.info("C100 Respondent selected");
                selectedPartyDetailsList.add(caseData.getRespondents()
                                                 .stream()
                                                 .filter(element -> element.getId().toString().equalsIgnoreCase(value.getCode()))
                                                 .map(Element::getValue)
                                                 .findFirst()
                );
                log.info("selectedPartyDetailsList size is ::" + selectedPartyDetailsList.size());
                log.info("selectedPartyDetailsList is ::" + selectedPartyDetailsList);
            }
            if (null != caseData.getApplicants()) {
                log.info("C100 applicant selected");
                selectedPartyDetailsList.add(caseData.getApplicants()
                                                 .stream()
                                                 .filter(element -> element.getId().toString().equalsIgnoreCase(value.getCode()))
                                                 .map(Element::getValue)
                                                 .findFirst()
                );
                log.info("selectedPartyDetailsList size is ::" + selectedPartyDetailsList.size());
                log.info("selectedPartyDetailsList is ::" + selectedPartyDetailsList);
            }
            if (null != caseData.getRespondentsFL401()
                && value.getCode().equalsIgnoreCase(String.valueOf(caseData.getRespondentsFL401().getPartyId()))) {
                log.info("FL401 Respondent selected");
                selectedPartyDetailsList.add(Optional.ofNullable(caseData.getRespondentsFL401()));
                log.info("selectedPartyDetailsList size is ::" + selectedPartyDetailsList.size());
                log.info("selectedPartyDetailsList is ::" + selectedPartyDetailsList);
            }
            if (null != caseData.getApplicantsFL401()
                && value.getCode().equalsIgnoreCase(String.valueOf(caseData.getApplicantsFL401().getPartyId()))) {
                log.info("FL401 Applicant selected");
                selectedPartyDetailsList.add(Optional.ofNullable(caseData.getApplicantsFL401()));
                log.info("selectedPartyDetailsList size is ::" + selectedPartyDetailsList.size());
                log.info("selectedPartyDetailsList is ::" + selectedPartyDetailsList);
            }
        });

        log.info("Party details found - rest wip:: ", selectedPartyDetailsList);
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        return caseDataUpdated;
    }

    public void updateLegalRepresentation(CallbackRequest callbackRequest, String authorisation, CaseDetails caseDetails, CaseData caseData) {
        if ("amendRespondentsDetails".equalsIgnoreCase(callbackRequest.getEventId())) {
            CaseData oldCaseData = objectMapper.convertValue(callbackRequest.getCaseDetailsBefore().getData(), CaseData.class);
            caseData.getRespondents().stream().forEach(partyDetailsElement -> {
                if (YesNoDontKnow.no.equals(partyDetailsElement.getValue().getDoTheyHaveLegalRepresentation())) {
                    int respondentIndex = caseData.getRespondents().indexOf(partyDetailsElement);
                    PartyDetails oldRespondent = oldCaseData.getRespondents().get(respondentIndex).getValue();
                    log.info("oldRespondent ==> " + oldRespondent);
                    UserDetails userDetails = userService.getUserDetails(authorisation);
                    DynamicListElement roleItem = DynamicListElement.builder()
                        .code(CaseRole.C100RESPONDENTSOLICITOR1.formattedName())
                        .label(CaseRole.C100RESPONDENTSOLICITOR1.formattedName())
                        .build();
                    ChangeOrganisationRequest changeOrganisationRequest = ChangeOrganisationRequest.builder()
                        .organisationToRemove(oldRespondent.getSolicitorOrg())
                        .createdBy(userDetails.getEmail())
                        .caseRoleId(DynamicList.builder()
                                        .value(roleItem)
                                        .listItems(List.of(roleItem))
                                        .build())
                        .approvalStatus(ChangeOrganisationApprovalStatus.APPROVED)
                        .requestTimestamp(time.now())
                        .build();
                    log.info("changeOrganisationRequest ==> " + changeOrganisationRequest);
                    caseDetails.getData()
                        .put("changeOrganisationRequestField", changeOrganisationRequest);
                    /*AboutToStartOrSubmitCallbackResponse response = assignCaseAccessClient.applyDecision(
                        authorisation,
                        tokenGenerator.generate(),
                        decisionRequest(caseDetails)
                    );
                    log.info("applyDecision response ==> " + response);*/
                    Optional<SolicitorRole> solicitorRole = getSolicitorRole(changeOrganisationRequest);
                    String solicitorName = oldRespondent.getFirstName() + " " + oldRespondent.getLastName();
                    if (changeOrganisationRequest != null) {
                        NoticeOfChangeEvent noticeOfChangeEvent = prepareNoticeOfChangeEvent(
                            caseData,
                            solicitorRole,
                            solicitorName,
                            oldRespondent.getSolicitorEmail(),
                            "remove"
                        );
                        log.info("noticeOfChangeEvent remove ==> " + noticeOfChangeEvent);
                        eventPublisher.publishEvent(noticeOfChangeEvent);
                    }
                }
            });
        }
    }
}
