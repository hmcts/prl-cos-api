package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.exception.RespondentSolicitorException;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.ResSolInternationalElements;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarmData;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorKeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorMiam;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators.ResponseSubmitChecker;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C7_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C7_FINAL_DOCUMENT;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor
public class C100RespondentSolicitorService {
    public static final String RESPONDENTS = "respondents";
    public static final String RESPONDENT_NAME_FOR_RESPONSE = "respondentNameForResponse";
    public static final String TECH_ERROR = "This event cannot be started. Please contact support team";
    public static final String RESPONSE_ALREADY_SUBMITTED_ERROR = "This event cannot be started as the response has already been submitted.";

    @Autowired
    private final RespondentSolicitorMiamService miamService;

    @Autowired
    private final ObjectMapper objectMapper;

    @Autowired
    private final DocumentGenService documentGenService;

    @Autowired
    private final ResponseSubmitChecker responseSubmitChecker;

    public Map<String, Object> populateAboutToStartCaseData(CallbackRequest callbackRequest, String authorisation, List<String> errorList) {
        log.info("Inside prePopulateAboutToStartCaseData");
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        Element<PartyDetails> solicitorRepresentedRespondent = findSolicitorRepresentedRespondents(
            callbackRequest
        );

        retrieveExistingResponseForSolicitor(
            callbackRequest,
            caseDataUpdated,
            solicitorRepresentedRespondent
        );

        String representedRespondentName = solicitorRepresentedRespondent.getValue().getFirstName() + " "
            + solicitorRepresentedRespondent.getValue().getLastName();

        caseDataUpdated.put(RESPONDENT_NAME_FOR_RESPONSE, representedRespondentName);
        return caseDataUpdated;
    }

    private void retrieveExistingResponseForSolicitor(CallbackRequest callbackRequest, Map<String, Object> caseDataUpdated, Element<PartyDetails> x) {
        log.info("finding respondentParty is present ");
        String invokedEvent = callbackRequest.getEventId().substring(0, callbackRequest.getEventId().length() - 1);
        RespondentSolicitorEvents.getCaseFieldName(invokedEvent).ifPresent(event -> {
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
                    caseDataUpdated.put(
                        miamFields[0],
                        x.getValue().getResponse().getSolicitorMiam().getRespSolHaveYouAttendedMiam()
                    );
                    caseDataUpdated.put(
                        miamFields[1],
                        x.getValue().getResponse().getSolicitorMiam().getRespSolWillingnessToAttendMiam()
                    );
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
        Element<PartyDetails> solicitorRepresentedRespondent = findSolicitorRepresentedRespondents(callbackRequest);

        String invokingEvent = callbackRequest.getEventId().substring(0, callbackRequest.getEventId().length() - 1);
        RespondentSolicitorEvents.getCaseFieldName(invokingEvent)
            .ifPresent(event -> buildResponseForRespondent(
                caseData,
                respondents,
                solicitorRepresentedRespondent,
                event
            ));

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

    private Element<PartyDetails> findSolicitorRepresentedRespondents(CallbackRequest callbackRequest) {
        Element<PartyDetails> solicitorRepresentedRespondent = null;
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        if (callbackRequest.getEventId().isEmpty()) {
            throw new RespondentSolicitorException(TECH_ERROR);
        } else {
            String invokingSolicitor = callbackRequest.getEventId().substring(callbackRequest.getEventId().length() - 1);
            log.info("****** invokingSolicitor is " + invokingSolicitor);

            Optional<SolicitorRole> solicitorRole = SolicitorRole.from(invokingSolicitor);

            if (solicitorRole.isPresent()) {
                solicitorRepresentedRespondent = caseData.getRespondents().get(solicitorRole.get().getIndex());
                if (solicitorRepresentedRespondent.getValue().getResponse() != null
                    && Yes.equals(solicitorRepresentedRespondent.getValue().getResponse().getC7ResponseSubmitted())) {
                    throw new RespondentSolicitorException(
                        RESPONSE_ALREADY_SUBMITTED_ERROR);
                }
            }
        }
        return solicitorRepresentedRespondent;
    }

    public Map<String, Object> generateConfidentialityDynamicSelectionDisplay(CallbackRequest callbackRequest) {
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        StringBuilder selectedList = new StringBuilder();

        selectedList.append("<ul>");
        for (ConfidentialityListEnum confidentiality : caseData.getKeepContactDetailsPrivateOther()
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

    public Map<String, Object> validateActiveRespondentResponse(CallbackRequest callbackRequest, List<String> errorList,
                                                                String authorisation) throws Exception {

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(
            caseDataUpdated,
            CaseData.class
        );
        String invokingRespondent = callbackRequest.getEventId().substring(callbackRequest.getEventId().length() - 1);
        log.info("Event name:::{}", callbackRequest.getEventId());
        boolean mandatoryFinished = false;

        mandatoryFinished = responseSubmitChecker.isFinished(caseData, invokingRespondent);
        if (!mandatoryFinished) {
            errorList.add(
                "Response submission is not allowed for this case unless you finish all the mandatory information");
        } else {
            Document document = documentGenService.generateSingleDocument(
                authorisation,
                caseData,
                SOLICITOR_C7_FINAL_DOCUMENT,
                false
            );
            caseDataUpdated.put("finalC7ResponseDoc", document);
        }
        return caseDataUpdated;
    }

    public Map<String, Object> submitC7ResponseForActiveRespondent(CallbackRequest callbackRequest, String authorisation, List<String> errorList) {
        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(
            updatedCaseData,
            CaseData.class
        );

        List<Element<PartyDetails>> respondents = caseData.getRespondents();

        Element<PartyDetails> representedRespondent = findSolicitorRepresentedRespondents(callbackRequest);

        PartyDetails amended = representedRespondent.getValue().toBuilder()
            .response(representedRespondent.getValue().getResponse().toBuilder().c7ResponseSubmitted(Yes).build())
            .build();

        respondents.set(respondents.indexOf(representedRespondent), element(representedRespondent.getId(), amended));

        updatedCaseData.put(RESPONDENTS, respondents);
        return updatedCaseData;
    }

    public Map<String, Object> generateDraftDocumentsForRespondent(CallbackRequest callbackRequest, String authorisation) throws Exception {

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Document document = documentGenService.generateSingleDocument(
            authorisation,
            caseData,
            SOLICITOR_C7_DRAFT_DOCUMENT,
            false
        );
        caseDataUpdated.put("draftC7ResponseDoc", document);

        if (Yes.equals(caseData.getRespondentAohYesNo())) {
            Document documentForC1A = documentGenService.generateSingleDocument(
                authorisation,
                caseData,
                SOLICITOR_C1A_DRAFT_DOCUMENT,
                false
            );
            caseDataUpdated.put("draftC1ADoc", documentForC1A);
        }

        return caseDataUpdated;
    }
}
