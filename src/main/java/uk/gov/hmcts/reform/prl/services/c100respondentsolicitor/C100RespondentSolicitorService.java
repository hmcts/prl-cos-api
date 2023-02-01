package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseaccess.CaseUser;
import uk.gov.hmcts.reform.prl.models.caseaccess.FindUserCaseRolesResponse;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.ResSolInternationalElements;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarmData;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorKeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorMiam;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators.ResponseSubmitChecker;
import uk.gov.hmcts.reform.prl.services.caseaccess.CcdDataStoreService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.SUBMIT;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor
public class C100RespondentSolicitorService {
    public static final String CHOOSE_RESPONDENT_DYNAMIC_LIST = "chooseRespondentDynamicList";
    public static final String RESPONDENTS = "respondents";
    public static final String NO_ACTIVE_RESPONDENT_ERR_MSG
        = "You must select an active respondent from the list to start representing through 'Select Respondent' event";
    private final CcdDataStoreService ccdDataStoreService;

    @Autowired
    private final RespondentSolicitorMiamService miamService;

    @Autowired
    private final ResponseSubmitChecker responseSubmitChecker;

    @Autowired
    private final ObjectMapper objectMapper;

    public Map<String, Object> populateAboutToStartCaseData(CallbackRequest callbackRequest, String authorisation, List<String> errorList) {
        log.info("Inside prePopulateAboutToStartCaseData");
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(
            caseDataUpdated,
            CaseData.class
        );
        Optional<Element<PartyDetails>> getActiveRespondent = findActiveRespondent(caseData, authorisation);

        getActiveRespondent.ifPresentOrElse(x -> retrieveExistingResponseForSolicitor(
            callbackRequest,
            caseDataUpdated,
            x), () -> errorList.add(
            "You must select a respondent to represent through 'Respond to application' event"));

        String activeRespondentName = getActiveRespondent.isPresent()
            ? (getActiveRespondent.get().getValue().getFirstName() + " "
            + getActiveRespondent.get().getValue().getLastName()) : null;

        caseDataUpdated.put("respondentNameForResponse", activeRespondentName);
        return caseDataUpdated;
    }

    private void retrieveExistingResponseForSolicitor(CallbackRequest callbackRequest, Map<String, Object> caseDataUpdated, Element<PartyDetails> x) {
        log.info("finding respondentParty is present ");
        RespondentSolicitorEvents.getCaseFieldName(callbackRequest.getEventId()).ifPresent(event -> {
            switch (event) {
                case CONSENT:
                    caseDataUpdated.put(
                        event.getCaseFieldName(),
                        x.getValue().getResponse().getConsent()
                    );
                    break;
                case KEEP_DETAILS_PRIVATE:
                    String[] keepDetailsPrivateFields = event.getCaseFieldName().split(",");
                    caseDataUpdated.put(keepDetailsPrivateFields[0], x.getValue().getResponse()
                        .getSolicitorKeepDetailsPriate().getRespKeepDetailsPrivate());
                    caseDataUpdated.put(keepDetailsPrivateFields[1], x.getValue().getResponse()
                        .getSolicitorKeepDetailsPriate().getRespKeepDetailsPrivateConfidentiality());
                    break;
                case CONFIRM_EDIT_CONTACT_DETAILS:
                    caseDataUpdated.put(
                        event.getCaseFieldName(),
                        x.getValue().getResponse().getCitizenDetails()
                    );
                    break;
                case ATTENDING_THE_COURT:
                    caseDataUpdated.put(
                        event.getCaseFieldName(),
                        x.getValue().getResponse().getAttendToCourt()
                    );
                    break;
                case MIAM:
                    String[] miamFields = event.getCaseFieldName().split(",");
                    log.info("MIAM fields, :::{}", (Object) miamFields);
                    caseDataUpdated.put(miamFields[0], x.getValue().getResponse().getSolicitorMiam().getRespSolHaveYouAttendedMiam());
                    caseDataUpdated.put(miamFields[1], x.getValue().getResponse().getSolicitorMiam().getRespSolWillingnessToAttendMiam());
                    caseDataUpdated.put(miamFields[2], miamService.getCollapsableOfWhatIsMiamPlaceHolder());
                    caseDataUpdated.put(
                        miamFields[3],
                        miamService.getCollapsableOfHelpMiamCostsExemptionsPlaceHolder()
                    );
                    break;
                case CURRENT_OR_PREVIOUS_PROCEEDINGS:
                    String[] proceedingsFields = event.getCaseFieldName().split(",");
                    caseDataUpdated.put(
                        proceedingsFields[0],
                        x.getValue().getResponse().getCurrentOrPastProceedingsForChildren()
                    );
                    caseDataUpdated.put(
                        proceedingsFields[1],
                        x.getValue().getResponse().getRespondentExistingProceedings()
                    );
                    break;
                case ALLEGATION_OF_HARM:
                    String[] allegationsOfHarmFields = event.getCaseFieldName().split(",");
                    caseDataUpdated.put(
                        allegationsOfHarmFields[0],
                        x.getValue().getResponse().getRespondentAllegationsOfHarmData().getRespAohYesOrNo()
                    );
                    caseDataUpdated.put(
                        allegationsOfHarmFields[1],
                        x.getValue().getResponse().getRespondentAllegationsOfHarmData().getRespAllegationsOfHarmInfo()
                    );
                    caseDataUpdated.put(
                        allegationsOfHarmFields[2],
                        x.getValue().getResponse().getRespondentAllegationsOfHarmData().getRespDomesticAbuseInfo()
                    );
                    caseDataUpdated.put(
                        allegationsOfHarmFields[3],
                        x.getValue().getResponse().getRespondentAllegationsOfHarmData().getRespChildAbuseInfo()
                    );
                    caseDataUpdated.put(
                        allegationsOfHarmFields[4],
                        x.getValue().getResponse().getRespondentAllegationsOfHarmData().getRespChildAbductionInfo()
                    );
                    caseDataUpdated.put(
                        allegationsOfHarmFields[5],
                        x.getValue().getResponse().getRespondentAllegationsOfHarmData().getRespOtherConcernsInfo()
                    );
                    break;
                case INTERNATIONAL_ELEMENT:
                    String[] internationalElementFields = event.getCaseFieldName().split(",");
                    caseDataUpdated.put(
                        internationalElementFields[0],
                        x.getValue().getResponse().getResSolInternationalElements().getInternationalElementChildInfo()
                    );
                    caseDataUpdated.put(
                        internationalElementFields[1],
                        x.getValue().getResponse().getResSolInternationalElements().getInternationalElementParentInfo()
                    );
                    caseDataUpdated.put(
                        internationalElementFields[2],
                        x.getValue().getResponse().getResSolInternationalElements().getInternationalElementJurisdictionInfo()
                    );
                    caseDataUpdated.put(
                        internationalElementFields[3],
                        x.getValue().getResponse().getResSolInternationalElements().getInternationalElementRequestInfo()
                    );
                    break;
                case ABILITY_TO_PARTICIPATE:
                    String[] abilityToParticipateFields = event.getCaseFieldName().split(",");
                    caseDataUpdated.put(
                        abilityToParticipateFields[0],
                        x.getValue().getResponse().getAbilityToParticipate()
                    );
                    break;
                case VIEW_DRAFT_RESPONSE:
                case SUBMIT:
                default:
                    break;
            }
        });
    }

    public Map<String, Object> populateAboutToSubmitCaseData(CallbackRequest callbackRequest, String authorisation, List<String> errorList) {
        log.info("Inside populateAboutToSubmitCaseData");
        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(
            updatedCaseData,
            CaseData.class
        );

        List<Element<PartyDetails>> respondents = caseData.getRespondents();

        findActiveRespondent(caseData, authorisation).ifPresentOrElse(
            x -> respondents.stream()
                .filter(party -> Objects.equals(party.getId(), x.getId()))
                .findFirst()
                .ifPresent(party -> {
                    log.info("finding respondentParty is present ");
                    RespondentSolicitorEvents.getCaseFieldName(callbackRequest.getEventId())
                        .ifPresent(event -> buildResponseForRespondent(caseData, respondents, party, event));
                }), () -> errorList.add(NO_ACTIVE_RESPONDENT_ERR_MSG));
        updatedCaseData.put(RESPONDENTS, respondents);
        return updatedCaseData;
    }

    private void buildResponseForRespondent(CaseData caseData,
                                            List<Element<PartyDetails>> respondents,
                                            Element<PartyDetails> party,
                                            RespondentSolicitorEvents event) {
        Response buildResponseForRespondent = party.getValue().getResponse();
        switch (event) {
            case CONSENT:
                Consent respondentConsentToApplication = caseData.getRespondentConsentToApplication();
                buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                    .consent(buildResponseForRespondent.getConsent().toBuilder()
                                 .consentToTheApplication(respondentConsentToApplication.getConsentToTheApplication())
                                 .noConsentReason(respondentConsentToApplication.getNoConsentReason())
                                 .applicationReceivedDate(respondentConsentToApplication.getApplicationReceivedDate())
                                 .courtOrderDetails(respondentConsentToApplication.getCourtOrderDetails())
                                 .permissionFromCourt(respondentConsentToApplication.getPermissionFromCourt())
                                 .build()).build();
                break;
            case KEEP_DETAILS_PRIVATE:
                buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                    .solicitorKeepDetailsPriate(SolicitorKeepDetailsPrivate.builder()
                                                    .respKeepDetailsPrivate(caseData.getKeepContactDetailsPrivate())
                                                    .respKeepDetailsPrivateConfidentiality(caseData.getKeepContactDetailsPrivateOther())
                                                    .build())

                    .build();
                break;
            case CONFIRM_EDIT_CONTACT_DETAILS:
                CitizenDetails citizenDetails = caseData.getResSolConfirmEditContactDetails();
                buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                    .citizenDetails(buildResponseForRespondent.getCitizenDetails().toBuilder()
                                        .firstName(citizenDetails.getFirstName())
                                        .lastName(citizenDetails.getLastName())
                                        .dateOfBirth(citizenDetails.getDateOfBirth())
                                        .previousName(citizenDetails.getPreviousName())
                                        .placeOfBirth(citizenDetails.getPlaceOfBirth())
                                        .address(citizenDetails.getAddress())
                                        .addressHistory(citizenDetails.getAddressHistory())
                                        .contact(citizenDetails.getContact())
                                        .build())
                    .build();
                break;
            case ATTENDING_THE_COURT:
                buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                    .attendToCourt(caseData.getRespondentAttendingTheCourt())
                    .build();
                break;
            case MIAM:
                buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                    .solicitorMiam(SolicitorMiam.builder()
                                       .respSolHaveYouAttendedMiam(caseData.getRespondentSolicitorHaveYouAttendedMiam())
                                       .respSolWillingnessToAttendMiam(caseData.getRespondentSolicitorWillingnessToAttendMiam())
                              .build())
                    .build();
                break;
            case CURRENT_OR_PREVIOUS_PROCEEDINGS:
                buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                    .currentOrPastProceedingsForChildren(caseData.getCurrentOrPastProceedingsForChildren())
                    .respondentExistingProceedings(caseData.getRespondentExistingProceedings())
                    .build();
                break;
            case ALLEGATION_OF_HARM:
                buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                    .respondentAllegationsOfHarmData(RespondentAllegationsOfHarmData.builder()
                                                         .respAohYesOrNo(caseData.getRespondentAohYesNo())
                                                         .respAllegationsOfHarmInfo(caseData.getRespondentAllegationsOfHarm())
                                                         .respDomesticAbuseInfo(caseData.getRespondentDomesticAbuseBehaviour())
                                                         .respChildAbuseInfo(caseData.getRespondentChildAbuseBehaviour())
                                                         .respChildAbductionInfo(caseData.getRespondentChildAbduction())
                                                         .respOtherConcernsInfo(caseData.getRespondentOtherConcerns())
                                                         .build())
                    .build();
                break;
            case INTERNATIONAL_ELEMENT:
                buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                    .resSolInternationalElements(ResSolInternationalElements.builder()
                                                     .internationalElementChildInfo(caseData.getInternationalElementChild())
                                                     .internationalElementParentInfo(caseData.getInternationalElementParent())
                                                     .internationalElementJurisdictionInfo(caseData.getInternationalElementJurisdiction())
                                                     .internationalElementRequestInfo(caseData.getInternationalElementRequest())
                                                     .build())
                    .build();
                break;
            case ABILITY_TO_PARTICIPATE:
                buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                    .abilityToParticipate(caseData.getAbilityToParticipateInProceedings())
                    .build();
                break;
            case VIEW_DRAFT_RESPONSE:
            case SUBMIT:

            default:
                break;
        }
        PartyDetails amended = party.getValue().toBuilder()
            .response(buildResponseForRespondent).build();
        respondents.set(respondents.indexOf(party), element(party.getId(), amended));
    }

    private Optional<Element<PartyDetails>> findActiveRespondent(CaseData caseData, String authorisation) {
        Optional<Element<PartyDetails>> activeRespondent = Optional.empty();
        List<Element<PartyDetails>> solicitorRepresentedRespondents
            = findSolicitorRepresentedRespondents(caseData, authorisation);

        if (solicitorRepresentedRespondents != null && !solicitorRepresentedRespondents.isEmpty()) {
            activeRespondent = solicitorRepresentedRespondents
                .stream()
                .filter(x -> YesOrNo.Yes.equals(x.getValue().getResponse().getActiveRespondent()))
                .findFirst();
        }
        return activeRespondent;
    }

    private List<Element<PartyDetails>> findSolicitorRepresentedRespondents(CaseData caseData, String authorisation) {
        List<Element<PartyDetails>> solicitorRepresentedRespondents = new ArrayList<>();
        FindUserCaseRolesResponse findUserCaseRolesResponse = findUserCaseRoles(caseData, authorisation);

        if (findUserCaseRolesResponse != null) {
            log.info("findUserCaseRolesResponse is not null ");
            solicitorRepresentedRespondents = getSolicitorRepresentedRespondents(
                caseData,
                findUserCaseRolesResponse
            );
        }
        return solicitorRepresentedRespondents;
    }

    private List<Element<PartyDetails>> getSolicitorRepresentedRespondents(CaseData caseData, FindUserCaseRolesResponse findUserCaseRolesResponse) {
        List<Element<PartyDetails>> solicitorRepresentedParties = new ArrayList<>();
        for (CaseUser caseUser : findUserCaseRolesResponse.getCaseUsers()) {
            SolicitorRole.from(caseUser.getCaseRole()).ifPresent(
                x -> {
                    Element<PartyDetails> respondent;
                    respondent = caseData.getRespondents().get(x.getIndex());
                    if (respondent.getValue().getResponse() != null
                        && !(YesOrNo.Yes.equals(respondent.getValue().getResponse().getC7ResponseSubmitted()))) {
                        solicitorRepresentedParties.add(respondent);
                    } else if (respondent.getValue().getResponse() == null) {
                        solicitorRepresentedParties.add(respondent);
                    }
                });
        }
        return solicitorRepresentedParties;
    }

    private FindUserCaseRolesResponse findUserCaseRoles(CaseData caseData, String authorisation) {
        log.info("findUserCaseRoles : caseId is:: " + caseData.getId());
        return ccdDataStoreService.findUserCaseRoles(
            String.valueOf(caseData.getId()),
            authorisation
        );
    }

    public Map<String, Object> populateSolicitorRespondentList(CallbackRequest callbackRequest, String authorisation) {
        Map<String, Object> headerMap = new HashMap<>();
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        headerMap.put(CHOOSE_RESPONDENT_DYNAMIC_LIST, ElementUtils.asDynamicList(
            findSolicitorRepresentedRespondents(caseData, authorisation),
            null,
            PartyDetails::getLabelForDynamicList
        ));
        return headerMap;
    }

    public Map<String, Object> updateActiveRespondentSelectionBySolicitor(CallbackRequest callbackRequest, String authorisation) {
        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(
            updatedCaseData,
            CaseData.class
        );

        UUID selectedRespondentId = caseData.getChooseRespondentDynamicList().getValueCodeAsUuid();
        log.info("updateRespondents:: selectedRespondentId" + selectedRespondentId);
        List<Element<PartyDetails>> respondents = caseData.getRespondents();

        respondents.stream()
            .filter(party -> Objects.equals(party.getId(), selectedRespondentId))
            .findFirst()
            .ifPresent(party -> {
                PartyDetails amended = party.getValue().toBuilder()
                    .response(party.getValue().getResponse().toBuilder().activeRespondent(YesOrNo.Yes).build())
                    .build();
                if (callbackRequest.getEventId().equalsIgnoreCase(SUBMIT.getEventId())) {
                    amended = party.getValue().toBuilder()
                        .response(party.getValue().getResponse().toBuilder().c7ResponseSubmitted(YesOrNo.Yes).build())
                        .build();
                }
                respondents.set(respondents.indexOf(party), element(party.getId(), amended));
            });

        findSolicitorRepresentedRespondents(caseData, authorisation)
            .forEach(solicitorRepresentedParty ->
                respondents.stream()
                    .filter(party -> Objects.equals(party.getId(), solicitorRepresentedParty.getId())
                        && !Objects.equals(party.getId(), selectedRespondentId))
                    .forEach(party -> {
                        PartyDetails amended = party.getValue().toBuilder()
                            .response(party.getValue().getResponse().toBuilder().activeRespondent(YesOrNo.No).build())
                            .build();

                        respondents.set(respondents.indexOf(party), element(party.getId(), amended));
                    })
            );
        updatedCaseData.put(RESPONDENTS, respondents);
        return updatedCaseData;
    }

    public Map<String, Object> generateConfidentialityDynamicSelectionDisplay(CallbackRequest callbackRequest) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        StringBuilder selectedList = new StringBuilder();

        selectedList.append("<ul>");
        for (ConfidentialityListEnum confidentiality: caseData.getKeepContactDetailsPrivateOther()
            .getConfidentialityList()) {
            selectedList.append("<li>");
            selectedList.append(confidentiality.getDisplayedValue());
            selectedList.append("</li>");
        }
        selectedList.append("</ul>");

        Map<String, Object> keepDetailsPrivateList = new HashMap<>();
        keepDetailsPrivateList.put("confidentialListDetails", selectedList);
        return keepDetailsPrivateList;
    }

    public Map<String, Object> validateActiveRespondentResponse(CallbackRequest callbackRequest, List<String> errorList) {

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(
            caseDataUpdated,
            CaseData.class
        );
        log.info("Event name:::{}", callbackRequest.getEventId());
        boolean mandatoryFinished = false;

        mandatoryFinished = responseSubmitChecker.hasMandatoryCompleted(caseData);
        if (!mandatoryFinished) {
            errorList.add(
                "Response submission is not allowed for this case unless you finish all the mandatory information");
        }
        //Todo final C7 Document generation
        return caseDataUpdated;
    }

    public Map<String, Object> submitC7ResponseForActiveRespondent(CallbackRequest callbackRequest, String authorisation, List<String> errorList) {
        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(
            updatedCaseData,
            CaseData.class
        );

        UUID selectedRespondentId = caseData.getChooseRespondentDynamicList().getValueCodeAsUuid();
        log.info("updateRespondents:: selectedRespondentId" + selectedRespondentId);
        List<Element<PartyDetails>> respondents = caseData.getRespondents();

        respondents.stream()
            .filter(party -> Objects.equals(party.getId(), selectedRespondentId))
            .findFirst()
            .ifPresent(party -> {
                PartyDetails amended = party.getValue().toBuilder()
                        .response(party.getValue().getResponse().toBuilder().c7ResponseSubmitted(YesOrNo.Yes).build())
                        .build();

                respondents.set(respondents.indexOf(party), element(party.getId(), amended));
            });

        respondents.stream()
            .filter(party -> Objects.equals(party.getId(), selectedRespondentId))
            .findFirst()
            .ifPresent(party -> {
                PartyDetails amended = party.getValue().toBuilder()
                    .response(party.getValue().getResponse().toBuilder().activeRespondent(YesOrNo.No).build())
                    .build();

                respondents.set(respondents.indexOf(party), element(party.getId(), amended));
            });

        updatedCaseData.put(RESPONDENTS, respondents);
        return updatedCaseData;
    }
}
