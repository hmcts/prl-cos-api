package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.enums.citizen.AttendingToCourtEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.CourtComfortEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.CourtHearingEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.DocsSupportEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.HelpCommunicationEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.LanguageRequirementsEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.ReasonableAdjustmentsEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.SafetyArrangementsEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.TravellingToCourtEnum;
import uk.gov.hmcts.reform.prl.enums.managedocuments.DocumentPartyEnum;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.enums.respondentsolicitor.RespondentWelshNeedsListEnum;
import uk.gov.hmcts.reform.prl.exception.RespondentSolicitorException;
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
import uk.gov.hmcts.reform.prl.mapper.welshlang.WelshLangMapper;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.ContactInformation;
import uk.gov.hmcts.reform.prl.models.DxAddress;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.AddressHistory;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.Contact;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.DocumentDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.abilitytoparticipate.AbilityToParticipate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.internationalelements.CitizenInternationalElements;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.miam.Miam;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.proceedings.Proceedings;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.supportyouneed.ReasonableAdjustmentsSupport;
import uk.gov.hmcts.reform.prl.models.complextypes.refuge.RefugeConfidentialDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.respondentsolicitor.documents.RespondentDocs;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.AttendToCourt;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarmData;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.ResponseToAllegationsOfHarm;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RespChildAbuseBehaviour;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityC8RefugeService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.RespondentAllegationOfHarmService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators.ResponseSubmitChecker;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_C1A_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_RESPONDENT_TABLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_RESP_FINAL_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_DATA_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CHILDREN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COMMA;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_SEAL_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUE_DATE_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.NEW_CHILDREN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESP_CHILD_ABUSES_DOCMOSIS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_FINAL_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_WELSH_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_WELSH_FINAL_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C7_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C7_FINAL_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.THIS_INFORMATION_IS_CONFIDENTIAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YES;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.citizen.DocsSupportEnum.docsprint;
import static uk.gov.hmcts.reform.prl.enums.citizen.DocsSupportEnum.largeprintdocs;
import static uk.gov.hmcts.reform.prl.enums.citizen.DocsSupportEnum.other;
import static uk.gov.hmcts.reform.prl.enums.citizen.HelpCommunicationEnum.signlanguage;
import static uk.gov.hmcts.reform.prl.enums.citizen.ReasonableAdjustmentsEnum.commhelp;
import static uk.gov.hmcts.reform.prl.enums.citizen.ReasonableAdjustmentsEnum.docsformat;
import static uk.gov.hmcts.reform.prl.enums.citizen.ReasonableAdjustmentsEnum.hearingcomfort;
import static uk.gov.hmcts.reform.prl.enums.citizen.ReasonableAdjustmentsEnum.hearingsupport;
import static uk.gov.hmcts.reform.prl.enums.citizen.ReasonableAdjustmentsEnum.nosupport;
import static uk.gov.hmcts.reform.prl.enums.citizen.ReasonableAdjustmentsEnum.travellinghelp;
import static uk.gov.hmcts.reform.prl.enums.citizen.SafetyArrangementsEnum.noSafetyrequirements;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper.COMMA_SEPARATOR;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class C100RespondentSolicitorService {
    public static final String RESPONDENTS = "respondents";
    public static final String RESPONDENT_NAME_FOR_RESPONSE = "respondentNameForResponse";
    public static final String TECH_ERROR = "This event cannot be started. Please contact support team";
    public static final String RESPONSE_ALREADY_SUBMITTED_ERROR = "This event cannot be started as the response has already been submitted.";
    public static final String SOLICITOR = " (Solicitor)";
    public static final String RESPONDENT_DOCS_LIST = "respondentDocsList";
    public static final String RESPONDENT_CONFIDENTIAL_DETAILS = "respondentConfidentialDetails";
    public static final String IS_CONFIDENTIAL_DATA_PRESENT = "isConfidentialDataPresent";
    public static final String EMAIL = "email";
    public static final String PHONE = "phone";
    public static final String ADDRESS = "address";
    public static final String TASK_LIST_VERSION = "taskListVersion";
    private static final String COLON = ";";
    public static final String RESPONDENT_APPLICATION_CATEGORY = "Respondent Application";
    public static final String RESPONDENT_C1A_APPLICATION_CATEGORY = "Respondent C1A Application";
    public static final String ATTENDING_THE_COURT = "attendingTheCourt";
    public static final String ATTENDING_THE_COURT_WELSH = "attendingTheCourtWelsh";
    private final RespondentSolicitorMiamService miamService;
    private final ObjectMapper objectMapper;
    private final DocumentGenService documentGenService;
    private final ResponseSubmitChecker responseSubmitChecker;
    private final ApplicationsTabService applicationsTabService;
    private final ManageOrderService manageOrderService;
    private final SystemUserService systemUserService;
    private final ConfidentialDetailsMapper confidentialDetailsMapper;
    private final OrganisationService organisationService;
    private final RespondentAllegationOfHarmService respondentAllegationOfHarmService;
    private final ManageDocumentsService manageDocumentsService;
    private final UserService userService;
    private final DocumentLanguageService documentLanguageService;
    private final ConfidentialityC8RefugeService confidentialityC8RefugeService;

    public static final String RESPONSE_SUBMITTED_LABEL = "# Ymateb wedi'i gyflwyno<br/>Response Submitted";
    public static final String CONTACT_LOCAL_COURT_LABEL_WELSH = """
        ### Mae eich ymateb wedi ei gyflwyno.<br/>Your response is now submitted.


        Gallwch gysylltu â’ch llys lleol yn\s""";

    public static final String CONTACT_LOCAL_COURT_LABEL_ENG = "<br/>You can contact your local court at\s";

    private static final String OPEN_BRACKET = "(";
    private static final String CLOSE_BRACKET = ")";
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public Map<String, Object> populateAboutToStartCaseData(CallbackRequest callbackRequest, String language) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        Optional<SolicitorRole> solicitorRole = getSolicitorRole(callbackRequest);
        Element<PartyDetails> solicitorRepresentedRespondent = null;
        if (solicitorRole.isPresent()) {
            solicitorRepresentedRespondent = findSolicitorRepresentedRespondents(callbackRequest, solicitorRole.get());
        }
        if (solicitorRepresentedRespondent != null && solicitorRepresentedRespondent.getValue() != null) {
            retrieveExistingResponseForSolicitor(
                callbackRequest,
                caseDataUpdated,
                solicitorRepresentedRespondent,
                language
            );
            String representedRespondentName = solicitorRepresentedRespondent.getValue().getFirstName() + " "
                    + solicitorRepresentedRespondent.getValue().getLastName();

            caseDataUpdated.put(RESPONDENT_NAME_FOR_RESPONSE, representedRespondentName);
        }
        return caseDataUpdated;
    }

    private void retrieveExistingResponseForSolicitor(CallbackRequest callbackRequest, Map<String,
            Object> caseDataUpdated, Element<PartyDetails> solicitorRepresentedRespondent, String language) {
        String invokedEvent = callbackRequest.getEventId().substring(0, callbackRequest.getEventId().length() - 1);
        RespondentSolicitorEvents.getCaseFieldName(invokedEvent).ifPresent(event -> {
            switch (event) {
                case CONSENT:
                    caseDataUpdated.put(
                            event.getCaseFieldName(),
                            solicitorRepresentedRespondent.getValue().getResponse().getConsent()
                    );
                    break;
                case KEEP_DETAILS_PRIVATE:
                    caseDataUpdated.put(
                            event.getCaseFieldName(),
                            solicitorRepresentedRespondent.getValue().getResponse().getKeepDetailsPrivate()
                    );
                    break;
                case CONFIRM_EDIT_CONTACT_DETAILS:
                    CitizenDetails citizenDetails = solicitorRepresentedRespondent.getValue().getResponse().getCitizenDetails();
                    PartyDetails partyDetails = solicitorRepresentedRespondent.getValue();
                    caseDataUpdated.put(
                            event.getCaseFieldName(),
                            CitizenDetails.builder()
                                    .address(ofNullable(citizenDetails.getAddress()).orElse(partyDetails.getAddress()))
                                    .addressHistory(ofNullable(citizenDetails.getAddressHistory()).orElse(
                                            AddressHistory.builder().isAtAddressLessThan5Years(partyDetails.getIsAtAddressLessThan5Years())
                                                    .build()
                                    ))
                                    .contact(ofNullable(citizenDetails.getContact()).orElse(Contact.builder()
                                            .phoneNumber(
                                                    partyDetails
                                                            .getPhoneNumber())
                                            .email(partyDetails.getEmail())
                                            .build()))
                                    .dateOfBirth(ofNullable(citizenDetails.getDateOfBirth()).orElse(partyDetails.getDateOfBirth()))
                                    .firstName(ofNullable(citizenDetails.getFirstName()).orElse(partyDetails.getFirstName()))
                                    .lastName(ofNullable(citizenDetails.getLastName()).orElse(partyDetails.getLastName()))
                                    .placeOfBirth(ofNullable(citizenDetails.getPlaceOfBirth()).orElse(partyDetails.getPlaceOfBirth()))
                                    .liveInRefuge(ofNullable(citizenDetails.getLiveInRefuge()).orElse(partyDetails.getLiveInRefuge()))
                                    .refugeConfidentialityC8Form(ofNullable(citizenDetails.getRefugeConfidentialityC8Form())
                                                                     .orElse(partyDetails.getRefugeConfidentialityC8Form()))
                                    .previousName(ofNullable(citizenDetails.getPreviousName()).orElse(partyDetails.getPreviousName()))
                                    .build()
                    );
                    break;
                case ATTENDING_THE_COURT:
                    caseDataUpdated.put(
                            event.getCaseFieldName(),
                            solicitorRepresentedRespondent.getValue().getResponse().getAttendToCourt()
                    );
                    break;
                case MIAM:
                    String[] miamFields = event.getCaseFieldName().split(COMMA);
                    //PRL-4588 - align miam screen as per figma
                    Miam existingMiam = solicitorRepresentedRespondent.getValue().getResponse().getMiam();
                    if (null != existingMiam) {
                        caseDataUpdated.put(miamFields[0], existingMiam.getAttendedMiam());
                        caseDataUpdated.put(miamFields[1], existingMiam.getWillingToAttendMiam());
                        caseDataUpdated.put(miamFields[2], existingMiam.getReasonNotAttendingMiam());
                    }
                    if (PrlAppsConstants.WELSH.equals(language)) {
                        caseDataUpdated.put(miamFields[3], miamService.getCollapsableOfWhatIsMiamPlaceHolderWelsh());
                        caseDataUpdated.put(miamFields[4], miamService.getCollapsableOfHelpMiamCostsExemptionsPlaceHolderWelsh());
                    } else {
                        caseDataUpdated.put(miamFields[3], miamService.getCollapsableOfWhatIsMiamPlaceHolder());
                        caseDataUpdated.put(miamFields[4], miamService.getCollapsableOfHelpMiamCostsExemptionsPlaceHolder());
                    }
                    break;
                case OTHER_PROCEEDINGS:
                    String[] proceedingsFields = event.getCaseFieldName().split(",");
                    caseDataUpdated.put(
                            proceedingsFields[0],
                            solicitorRepresentedRespondent.getValue().getResponse().getCurrentOrPastProceedingsForChildren()
                    );
                    caseDataUpdated.put(
                            proceedingsFields[1],
                            solicitorRepresentedRespondent.getValue().getResponse().getRespondentExistingProceedings()
                    );
                    break;
                case ALLEGATION_OF_HARM:
                    Map<String, Object> unmodifiedCaseData = callbackRequest.getCaseDetails().getData();
                    CaseData caseData = objectMapper.convertValue(
                            unmodifiedCaseData,
                            CaseData.class
                    );

                    RespondentAllegationsOfHarmData solicitorRepresentedRespondentAllegationsOfHarmData = solicitorRepresentedRespondent.getValue()
                            .getResponse().getRespondentAllegationsOfHarmData();
                    Map<String, Object> data = objectMapper
                            .convertValue(solicitorRepresentedRespondentAllegationsOfHarmData,new TypeReference<Map<String, Object>>() {});
                    caseDataUpdated.putAll(data);
                    respondentAllegationOfHarmService.prePopulatedChildData(caseData,
                            caseDataUpdated,solicitorRepresentedRespondentAllegationsOfHarmData);

                    break;
                case RESPOND_ALLEGATION_OF_HARM:
                    if (null != solicitorRepresentedRespondent.getValue().getResponse().getResponseToAllegationsOfHarm()) {
                        String[] respondToAllegationOfHarmFields = event.getCaseFieldName().split(",");
                        caseDataUpdated.put(
                                respondToAllegationOfHarmFields[0],
                                solicitorRepresentedRespondent.getValue().getResponse().getResponseToAllegationsOfHarm()
                                        .getResponseToAllegationsOfHarmYesOrNoResponse()
                        );
                        caseDataUpdated.put(
                                respondToAllegationOfHarmFields[1],
                                solicitorRepresentedRespondent.getValue().getResponse().getResponseToAllegationsOfHarm()
                                        .getResponseToAllegationsOfHarmDocument()
                        );
                    }
                    break;
                case INTERNATIONAL_ELEMENT:
                    String[] internationalElementFields = event.getCaseFieldName().split(",");
                    caseDataUpdated.put(
                            internationalElementFields[0],
                            solicitorRepresentedRespondent.getValue().getResponse().getCitizenInternationalElements()
                    );
                    break;
                case ABILITY_TO_PARTICIPATE:
                    String[] abilityToParticipateFields = event.getCaseFieldName().split(",");
                    caseDataUpdated.put(
                            abilityToParticipateFields[0],
                            solicitorRepresentedRespondent.getValue().getResponse().getAbilityToParticipate()
                    );
                    break;
                case VIEW_DRAFT_RESPONSE:
                    String[] viewDraftDocumentFields = event.getCaseFieldName().split(",");
                    caseDataUpdated.put(
                            viewDraftDocumentFields[0],
                            " "
                    );
                    break;
                case SUBMIT:
                default:
                    break;
            }
        });
    }

    public Map<String, Object> populateAboutToSubmitCaseData(CallbackRequest callbackRequest) {
        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(
                updatedCaseData,
                CaseData.class
        );
        List<Element<PartyDetails>> respondents = caseData.getRespondents();
        Optional<SolicitorRole> solicitorRole = getSolicitorRole(callbackRequest);
        Element<PartyDetails> solicitorRepresentedRespondent = null;
        if (solicitorRole.isPresent()) {
            solicitorRepresentedRespondent = findSolicitorRepresentedRespondents(callbackRequest, solicitorRole.get());
        }
        String invokingEvent = callbackRequest.getEventId().substring(0, callbackRequest.getEventId().length() - 1);
        Element<PartyDetails> finalSolicitorRepresentedRespondent = solicitorRepresentedRespondent;
        List<Element<PartyDetails>> finalRespondents = respondents;
        RespondentSolicitorEvents.getCaseFieldName(invokingEvent)
                .ifPresent(event -> buildResponseForRespondent(
                        caseData,
                        finalRespondents,
                        finalSolicitorRepresentedRespondent,
                        event
                ));

        if (RespondentSolicitorEvents.CONFIRM_EDIT_CONTACT_DETAILS.getEventId().equalsIgnoreCase(invokingEvent)
            || RespondentSolicitorEvents.KEEP_DETAILS_PRIVATE.getEventId().equalsIgnoreCase(invokingEvent)) {
            CaseData caseDataTemp = confidentialDetailsMapper.mapConfidentialData(caseData, false);
            updatedCaseData.put(RESPONDENT_CONFIDENTIAL_DETAILS, caseDataTemp.getRespondentConfidentialDetails());
        }

        updatedCaseData.put(RESPONDENT_DOCS_LIST, caseData.getRespondentDocsList());
        updatedCaseData.put(C100_RESPONDENT_TABLE, applicationsTabService.getRespondentsTable(caseData));
        updatedCaseData.put(RESPONDENTS, respondents);
        Map<String, Object> data = objectMapper
                .convertValue(RespondentAllegationsOfHarmData.builder().build(),new TypeReference<Map<String, Object>>() {});

        /**
         * Deleting the document from the casedata for fixing the
         * duplication issue of Response to Allegation of Harm
         * Document in the case file view. The same document
         * will flow to quarantine docs list upon Respondent task
         * lost submission
         */
        if (ofNullable(updatedCaseData.getOrDefault("responseToAllegationsOfHarmDocument", Optional.empty())).isPresent()) {
            updatedCaseData.remove("responseToAllegationsOfHarmDocument");
        }
        updatedCaseData.putAll(data);
        //PRL-4588 - cleanup
        cleanUpRespondentTasksFieldOptions(updatedCaseData);

        return updatedCaseData;
    }

    private static void cleanUpRespondentTasksFieldOptions(Map<String, Object> updatedCaseData) {
        //miam
        for (String field : RespondentSolicitorEvents.MIAM.getCaseFieldName().split(COMMA)) {
            updatedCaseData.remove(field);
        }
        for (String field : RespondentSolicitorEvents.OTHER_PROCEEDINGS.getCaseFieldName().split(COMMA)) {
            updatedCaseData.remove(field);
        }
    }

    private void buildResponseForRespondent(CaseData caseData,
                                            List<Element<PartyDetails>> respondents,
                                            Element<PartyDetails> party,
                                            RespondentSolicitorEvents event) {
        Response buildResponseForRespondent = party.getValue().getResponse();
        String solicitor = party.getValue().getRepresentativeFullNameForCaseFlags();

        switch (event) {
            case CONSENT:
                Consent respondentConsentToApplication = isNotEmpty(caseData.getRespondentSolicitorData())
                    ? caseData.getRespondentSolicitorData().getRespondentConsentToApplication() : null;
                respondentConsentToApplication = optimiseConsent(respondentConsentToApplication);
                buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                        .consent(respondentConsentToApplication).build();
                break;
            case KEEP_DETAILS_PRIVATE:
                buildResponseForRespondent = isNotEmpty(caseData.getRespondentSolicitorData())
                    ? buildKeepYourDetailsPrivateResponse(
                        caseData,
                        buildResponseForRespondent,
                        party
                ) : null;
                break;
            case CONFIRM_EDIT_CONTACT_DETAILS:
                buildResponseForRespondent = isNotEmpty(caseData.getRespondentSolicitorData())
                    ? buildCitizenDetailsResponse(caseData, buildResponseForRespondent, party) : null;
                break;
            case ATTENDING_THE_COURT:
                AttendToCourt attendToCourt = isNotEmpty(caseData.getRespondentSolicitorData())
                    ? optimiseAttendingCourt(caseData.getRespondentSolicitorData()
                        .getRespondentAttendingTheCourt()) : null;
                buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                        .attendToCourt(attendToCourt)
                        .build();
                break;
            case MIAM:
                buildResponseForRespondent = isNotEmpty(caseData.getRespondentSolicitorData())
                    ? buildMiamResponse(caseData, buildResponseForRespondent) : null;
                break;
            case OTHER_PROCEEDINGS:
                buildResponseForRespondent = isNotEmpty(caseData.getRespondentSolicitorData())
                    ? buildOtherProceedingsResponse(
                        caseData,
                        buildResponseForRespondent
                ) : null;
                break;
            case ALLEGATION_OF_HARM:
                buildResponseForRespondent = isNotEmpty(caseData.getRespondentSolicitorData())
                    ? buildAoHResponse(caseData, buildResponseForRespondent, solicitor) : null;
                break;
            case RESPOND_ALLEGATION_OF_HARM:
                buildResponseForRespondent = isNotEmpty(caseData.getRespondentSolicitorData())
                    ? buildRespondAllegationOfHarm(caseData, buildResponseForRespondent) : null;
                break;
            case INTERNATIONAL_ELEMENT:
                buildResponseForRespondent = isNotEmpty(caseData.getRespondentSolicitorData())
                    ? buildInternationalElementResponse(caseData, buildResponseForRespondent) : null;
                break;
            case ABILITY_TO_PARTICIPATE:
                buildResponseForRespondent = isNotEmpty(caseData.getRespondentSolicitorData())
                    ? buildAbilityToParticipateResponse(caseData, buildResponseForRespondent) : null;
                break;
            case VIEW_DRAFT_RESPONSE, SUBMIT:
            default:
                break;
        }
        PartyDetails amended = party.getValue().toBuilder()
                .response(buildResponseForRespondent).build();
        Optional<Element<PartyDetails>> partyElement = respondents.stream().filter(element -> element.getId()
                .equals(party.getId())).findFirst();
        if (partyElement.isPresent()) {
            int index = respondents.indexOf(partyElement.get());
            if (index != -1) {
                respondents.set(index, element(party.getId(), amended));
            }
        }
    }

    private Response buildRespondAllegationOfHarm(CaseData caseData, Response buildResponseForRespondent) {
        ResponseToAllegationsOfHarm responseToAllegationsOfHarm = optimiseResponseToAllegationsOfHarm(caseData.getRespondentSolicitorData()
                .getResponseToAllegationsOfHarm());
        return buildResponseForRespondent.toBuilder()
                .responseToAllegationsOfHarm(responseToAllegationsOfHarm)
                .build();
    }

    private ResponseToAllegationsOfHarm optimiseResponseToAllegationsOfHarm(ResponseToAllegationsOfHarm responseToAllegationsOfHarm) {
        if (null != responseToAllegationsOfHarm) {
            if (responseToAllegationsOfHarm.getResponseToAllegationsOfHarmYesOrNoResponse().equals(Yes)) {
                return responseToAllegationsOfHarm.toBuilder()
                        .responseToAllegationsOfHarmYesOrNoResponse(responseToAllegationsOfHarm.getResponseToAllegationsOfHarmYesOrNoResponse())
                        .responseToAllegationsOfHarmDocument(responseToAllegationsOfHarm.getResponseToAllegationsOfHarmDocument())
                        .build();
            } else {
                return responseToAllegationsOfHarm.toBuilder()
                        .responseToAllegationsOfHarmYesOrNoResponse(responseToAllegationsOfHarm.getResponseToAllegationsOfHarmYesOrNoResponse())
                        .responseToAllegationsOfHarmDocument(null)
                        .build();
            }
        }
        return null;
    }

    private Response buildOtherProceedingsResponse(CaseData caseData, Response buildResponseForRespondent) {
        List<Element<RespondentProceedingDetails>> respondentExistingProceedings
                = YesNoDontKnow.yes.equals(caseData.getRespondentSolicitorData()
                .getCurrentOrPastProceedingsForChildren())
                ? caseData.getRespondentSolicitorData()
                .getRespondentExistingProceedings() : null;

        return buildResponseForRespondent.toBuilder()
                .currentOrPastProceedingsForChildren(caseData.getRespondentSolicitorData()
                        .getCurrentOrPastProceedingsForChildren())
                .respondentExistingProceedings(respondentExistingProceedings)
                .build();
    }

    private Response buildAbilityToParticipateResponse(CaseData caseData, Response buildResponseForRespondent) {
        buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                .abilityToParticipate(AbilityToParticipate.builder()
                        .detailsOfReferralOrAssessment(caseData.getRespondentSolicitorData()
                                .getAbilityToParticipateInProceedings()
                                .getDetailsOfReferralOrAssessment())
                        .giveDetailsAffectingLitigationCapacity(caseData.getRespondentSolicitorData()
                                .getAbilityToParticipateInProceedings()
                                .getGiveDetailsAffectingLitigationCapacity())
                        .factorsAffectingAbilityToParticipate(caseData.getRespondentSolicitorData()
                                .getAbilityToParticipateInProceedings()
                                .getFactorsAffectingAbilityToParticipate())
                        .provideDetailsForFactorsAffectingAbilityToParticipate(
                                Yes.equals(
                                        caseData.getRespondentSolicitorData()
                                                .getAbilityToParticipateInProceedings().getFactorsAffectingAbilityToParticipate())
                                        ? caseData.getRespondentSolicitorData()
                                        .getAbilityToParticipateInProceedings()
                                        .getProvideDetailsForFactorsAffectingAbilityToParticipate()
                                        : null)
                        .build())
                .build();
        return buildResponseForRespondent;
    }

    private Response buildInternationalElementResponse(CaseData caseData, Response buildResponseForRespondent) {
        buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                .citizenInternationalElements(CitizenInternationalElements
                        .builder()
                        .childrenLiveOutsideOfEnWl(caseData
                                .getRespondentSolicitorData()
                                .getInternationalElementChild()
                                .getChildrenLiveOutsideOfEnWl())
                        .childrenLiveOutsideOfEnWlDetails(Yes
                                .equals(caseData
                                        .getRespondentSolicitorData()
                                        .getInternationalElementChild()
                                        .getChildrenLiveOutsideOfEnWl()
                                ) ? caseData
                                .getRespondentSolicitorData()
                                .getInternationalElementChild()
                                .getChildrenLiveOutsideOfEnWlDetails() : null)
                        .parentsAnyOneLiveOutsideEnWl(caseData
                                .getRespondentSolicitorData()
                                .getInternationalElementChild()
                                .getParentsAnyOneLiveOutsideEnWl())
                        .parentsAnyOneLiveOutsideEnWlDetails(Yes
                                .equals(caseData
                                        .getRespondentSolicitorData()
                                        .getInternationalElementChild()
                                        .getParentsAnyOneLiveOutsideEnWl()
                                ) ? caseData
                                .getRespondentSolicitorData()
                                .getInternationalElementChild()
                                .getParentsAnyOneLiveOutsideEnWlDetails() : null
                        )
                        .anotherPersonOrderOutsideEnWl(caseData
                                .getRespondentSolicitorData()
                                .getInternationalElementChild()
                                .getAnotherPersonOrderOutsideEnWl())
                        .anotherPersonOrderOutsideEnWlDetails(Yes
                                .equals(caseData
                                        .getRespondentSolicitorData()
                                        .getInternationalElementChild()
                                        .getAnotherPersonOrderOutsideEnWl()
                                ) ? caseData
                                .getRespondentSolicitorData()
                                .getInternationalElementChild()
                                .getAnotherPersonOrderOutsideEnWlDetails() : null
                        )
                        .anotherCountryAskedInformation(caseData
                                .getRespondentSolicitorData()
                                .getInternationalElementChild()
                                .getAnotherCountryAskedInformation())
                        .anotherCountryAskedInformationDetaails(Yes
                                .equals(caseData
                                        .getRespondentSolicitorData()
                                        .getInternationalElementChild()
                                        .getAnotherCountryAskedInformation()
                                ) ? caseData
                                .getRespondentSolicitorData()
                                .getInternationalElementChild()
                                .getAnotherCountryAskedInformationDetaails() : null)
                        .build())
                .build();
        return buildResponseForRespondent;
    }

    public Response buildAoHResponse(CaseData caseData, Response buildResponseForRespondent, String solicitor) {
        RespondentAllegationsOfHarmData respondentAllegationsOfHarmData
                = caseData.getRespondentSolicitorData().getRespondentAllegationsOfHarmData();
        if (isNotEmpty(respondentAllegationsOfHarmData)) {
            if (null != respondentAllegationsOfHarmData.getRespOrdersUndertakingInPlaceDocument()) {
                buildRespondentDocs(
                    caseData,
                    caseData.getRespondentSolicitorData().getRespondentNameForResponse(),
                    solicitor + SOLICITOR,
                    respondentAllegationsOfHarmData.getRespOrdersUndertakingInPlaceDocument()
                );
            }

            if (null != respondentAllegationsOfHarmData.getRespOrdersForcedMarriageProtectionDocument()) {
                buildRespondentDocs(
                    caseData,
                    caseData.getRespondentSolicitorData().getRespondentNameForResponse(),
                    solicitor + SOLICITOR,
                    respondentAllegationsOfHarmData.getRespOrdersForcedMarriageProtectionDocument()
                );
            }
            if (null != respondentAllegationsOfHarmData.getRespOrdersNonMolestationDocument()) {
                buildRespondentDocs(
                    caseData,
                    caseData.getRespondentSolicitorData().getRespondentNameForResponse(),
                    solicitor + SOLICITOR,
                    respondentAllegationsOfHarmData.getRespOrdersNonMolestationDocument()
                );
            }

            if (null != respondentAllegationsOfHarmData.getRespOrdersOccupationDocument()) {
                buildRespondentDocs(
                    caseData,
                    caseData.getRespondentSolicitorData().getRespondentNameForResponse(),
                    solicitor + SOLICITOR,
                    respondentAllegationsOfHarmData.getRespOrdersOccupationDocument()
                );
            }

            if (null != respondentAllegationsOfHarmData.getRespOrdersOtherInjunctiveDocument()) {
                buildRespondentDocs(
                    caseData,
                    caseData.getRespondentSolicitorData().getRespondentNameForResponse(),
                    solicitor + SOLICITOR,
                    respondentAllegationsOfHarmData.getRespOrdersOtherInjunctiveDocument()
                );
            }

            if (null != respondentAllegationsOfHarmData.getRespOrdersRestrainingDocument()) {
                buildRespondentDocs(
                    caseData,
                    caseData.getRespondentSolicitorData().getRespondentNameForResponse(),
                    solicitor + SOLICITOR,
                    respondentAllegationsOfHarmData.getRespOrdersRestrainingDocument()
                );
            }
        }


        buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                .respondentAllegationsOfHarmData(
                        respondentAllegationsOfHarmData)
                .build();
        return buildResponseForRespondent;
    }

    private void buildRespondentDocs(CaseData caseData, String respondentName, String solicitorName, Document document) {
        RespondentDocs respondentDocs = RespondentDocs.builder()
                .otherDocuments(List.of(element(ResponseDocuments
                        .builder()
                        .partyName(respondentName)
                        .createdBy(solicitorName)
                        .dateCreated(LocalDate.now())
                        .citizenDocument(document)
                        .build())))
                .build();

        if (CollectionUtils.isNotEmpty(caseData.getRespondentDocsList())) {
            ArrayList<Element<RespondentDocs>> docList = new ArrayList<>(caseData.getRespondentDocsList());
            docList.add(element(respondentDocs));
            caseData.setRespondentDocsList(docList);
        } else {
            caseData.setRespondentDocsList(List.of(element(respondentDocs)));
        }
    }

    private Response buildMiamResponse(CaseData caseData, Response buildResponseForRespondent) {
        return buildResponseForRespondent.toBuilder()
                .miam(Miam.builder()
                        .attendedMiam(caseData.getRespondentSolicitorData().getHasRespondentAttendedMiam())
                        .willingToAttendMiam(caseData.getRespondentSolicitorData().getRespondentWillingToAttendMiam())
                        .reasonNotAttendingMiam(caseData.getRespondentSolicitorData().getRespondentReasonNotAttendingMiam())
                        .build())
                .build();
    }

    private Response buildCitizenDetailsResponse(CaseData caseData,
                                                 Response buildResponseForRespondent,
                                                 Element<PartyDetails> respondent) {
        CitizenDetails citizenDetails = caseData.getRespondentSolicitorData().getResSolConfirmEditContactDetails();
        buildResponseForRespondent = buildResponseForRespondent
            .toBuilder().citizenDetails(
                buildResponseForRespondent.getCitizenDetails()
                    .toBuilder()
                    .firstName(isNotEmpty(citizenDetails) ? citizenDetails.getFirstName() : null)
                    .lastName(isNotEmpty(citizenDetails) ? citizenDetails.getLastName() : null)
                    .dateOfBirth(isNotEmpty(citizenDetails) ? citizenDetails.getDateOfBirth() : null)
                    .previousName(isNotEmpty(citizenDetails) ? citizenDetails.getPreviousName() : null)
                    .placeOfBirth(isNotEmpty(citizenDetails) ? citizenDetails.getPlaceOfBirth() : null)
                    .liveInRefuge(isNotEmpty(citizenDetails) ? citizenDetails.getLiveInRefuge() : null)
                    .refugeConfidentialityC8Form(YesOrNo.Yes.equals(isNotEmpty(citizenDetails) ? citizenDetails.getLiveInRefuge() : null)
                                                     ? citizenDetails.getRefugeConfidentialityC8Form() : null)
                    .address(isNotEmpty(citizenDetails) ? citizenDetails.getAddress() : null)
                    .addressHistory(isNotEmpty(citizenDetails) ? citizenDetails.getAddressHistory() : null)
                    .contact(isNotEmpty(citizenDetails) ? citizenDetails.getContact() : null)
                    .build())
            .build();

        if (YesOrNo.Yes.equals(isNotEmpty(citizenDetails) ? citizenDetails.getLiveInRefuge() : null)) {
            buildResponseForRespondent = buildResponseForRespondent
                .toBuilder().keepDetailsPrivate(buildResponseForRespondent.getKeepDetailsPrivate().toBuilder()
                                                    .confidentiality(Yes)
                                                    .confidentialityList(Arrays.asList(
                                                        ConfidentialityListEnum.email,
                                                        ConfidentialityListEnum.address,
                                                        ConfidentialityListEnum.phoneNumber
                                                    )).build())
                .build();

            setRespondentConfidentiality(caseData, respondent);
        }
        return buildResponseForRespondent;
    }

    private Response buildKeepYourDetailsPrivateResponse(CaseData caseData, Response buildResponseForRespondent,
                                                         Element<PartyDetails> respondent) {
        if (null != caseData.getRespondentSolicitorData().getResSolConfirmEditContactDetails()
            && Yes.equals(caseData.getRespondentSolicitorData().getResSolConfirmEditContactDetails().getLiveInRefuge())) {
            buildResponseForRespondent = buildKeepDetailsPrivateForRefuge(caseData, buildResponseForRespondent, respondent);
        } else {
            buildResponseForRespondent = buildKeepDetailsPrivateForNonRefuge(caseData, buildResponseForRespondent, respondent);
        }

        return buildResponseForRespondent;
    }

    static Response buildKeepDetailsPrivateForRefuge(CaseData caseData,
                                                             Response buildResponseForRespondent,
                                                             Element<PartyDetails> respondent) {
        setRespondentConfidentiality(caseData, respondent);

        List<ConfidentialityListEnum> confList = new ArrayList<>();
        confList.add(ConfidentialityListEnum.address);
        confList.add(ConfidentialityListEnum.email);
        confList.add(ConfidentialityListEnum.phoneNumber);

        buildResponseForRespondent = buildResponseForRespondent.toBuilder()
            .keepDetailsPrivate(KeepDetailsPrivate.builder()
                .otherPeopleKnowYourContactDetails(
                    caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate() != null
                        ? caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate()
                        .getOtherPeopleKnowYourContactDetails() : null)
                .confidentiality(Yes)
                .confidentialityList(confList)
                .build()).build();

        return  buildResponseForRespondent;
    }

    static Response buildKeepDetailsPrivateForNonRefuge(CaseData caseData,
                                                                Response buildResponseForRespondent,
                                                                Element<PartyDetails> respondent) {
        setRespondentConfidentiality(caseData, respondent);

        List<ConfidentialityListEnum> confList = caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate().getConfidentialityList();
        buildResponseForRespondent = buildResponseForRespondent.toBuilder()
            .keepDetailsPrivate(KeepDetailsPrivate.builder()
                                    .otherPeopleKnowYourContactDetails(
                                        caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate() != null
                                            ? caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate()
                                            .getOtherPeopleKnowYourContactDetails() : null)
                                    .confidentiality(caseData
                                                         .getRespondentSolicitorData()
                                                         .getKeepContactDetailsPrivate()
                                                         .getConfidentiality())
                                    .confidentialityList(confList)
                                    .build()).build();
        return buildResponseForRespondent;
    }

    private static void setRespondentConfidentiality(CaseData caseData,
                                                                      Element<PartyDetails> respondent) {
        if (isNotEmpty(caseData.getRespondentSolicitorData()) && null != caseData.getRespondentSolicitorData().getResSolConfirmEditContactDetails()
            && Yes.equals(caseData.getRespondentSolicitorData().getResSolConfirmEditContactDetails().getLiveInRefuge())) {
            respondent.getValue().setIsAddressConfidential(Yes);
            respondent.getValue().setIsEmailAddressConfidential(Yes);
            respondent.getValue().setIsPhoneNumberConfidential(Yes);
        } else if (isNotEmpty(caseData.getRespondentSolicitorData()) && null != caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate()
            && YesOrNo.Yes.equals(caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate().getConfidentiality())) {
            List<ConfidentialityListEnum> confList = caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate().getConfidentialityList();
            if (confList.contains(ConfidentialityListEnum.address)) {
                respondent.getValue().setIsAddressConfidential(Yes);
            } else {
                respondent.getValue().setIsAddressConfidential(No);
            }
            if (confList.contains(ConfidentialityListEnum.email)) {
                respondent.getValue().setIsEmailAddressConfidential(Yes);
            } else {
                respondent.getValue().setIsEmailAddressConfidential(No);
            }
            if (confList.contains(ConfidentialityListEnum.phoneNumber)) {
                respondent.getValue().setIsPhoneNumberConfidential(Yes);
            } else {
                respondent.getValue().setIsPhoneNumberConfidential(No);
            }
        } else if (isNotEmpty(caseData.getRespondentSolicitorData()) && null != caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate()
            && YesOrNo.No.equals(caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate().getConfidentiality())) {
            respondent.getValue().setIsAddressConfidential(No);
            respondent.getValue().setIsEmailAddressConfidential(No);
            respondent.getValue().setIsPhoneNumberConfidential(No);
        }
    }

    private AttendToCourt optimiseAttendingCourt(AttendToCourt attendToCourt) {
        return attendToCourt.toBuilder()
                .respondentWelshNeedsList(YesOrNo.No.equals(attendToCourt.getRespondentWelshNeeds()) ? null
                        : attendToCourt.getRespondentWelshNeedsList())
                .respondentInterpreterNeeds(YesOrNo.No.equals(attendToCourt.getIsRespondentNeededInterpreter()) ? null
                        : attendToCourt.getRespondentInterpreterNeeds())
                .disabilityNeeds(YesOrNo.No.equals(attendToCourt.getHaveAnyDisability()) ? null
                        : attendToCourt.getDisabilityNeeds())
                .respondentSpecialArrangementDetails(YesOrNo.No.equals(attendToCourt.getRespondentSpecialArrangements()) ? null
                        : attendToCourt.getRespondentSpecialArrangementDetails())
                .respondentIntermediaryNeedDetails(YesOrNo.No.equals(attendToCourt.getRespondentIntermediaryNeeds()) ? null
                        : attendToCourt.getRespondentIntermediaryNeedDetails())
                .build();
    }

    private Consent optimiseConsent(Consent consent) {
        String noConsentReason = consent.getNoConsentReason();
        String courtOrderDetails = consent.getCourtOrderDetails();
        if (YesOrNo.Yes.equals(consent.getConsentToTheApplication())) {
            noConsentReason = null;
        }
        if (YesOrNo.No.equals(consent.getPermissionFromCourt())) {
            courtOrderDetails = null;
        }
        return consent.toBuilder()
            .noConsentReason(noConsentReason)
            .courtOrderDetails(courtOrderDetails)
            .build();
    }

    public Optional<SolicitorRole> getSolicitorRole(CallbackRequest callbackRequest) {
        if (callbackRequest.getEventId().isEmpty()) {
            throw new RespondentSolicitorException(TECH_ERROR);
        } else {
            String invokingSolicitor = callbackRequest.getEventId().substring(callbackRequest.getEventId().length() - 1);

            Optional<SolicitorRole> solicitorRole = SolicitorRole.from(invokingSolicitor);

            if (solicitorRole.isPresent()) {
                return solicitorRole;
            }
        }
        return Optional.empty();
    }

    public Element<PartyDetails> findSolicitorRepresentedRespondents(CallbackRequest callbackRequest, SolicitorRole solicitorRole) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Element<PartyDetails> solicitorRepresentedRespondent = !caseData.getRespondents().isEmpty()
                ? caseData.getRespondents().get(solicitorRole.getIndex()) : null;
        if (solicitorRepresentedRespondent != null
                && solicitorRepresentedRespondent.getValue().getResponse() != null
                && Yes.equals(solicitorRepresentedRespondent.getValue().getResponse().getC7ResponseSubmitted())) {
            throw new RespondentSolicitorException(
                    RESPONSE_ALREADY_SUBMITTED_ERROR);
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
        for (ConfidentialityListEnum confidentiality : caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate()
                .getConfidentialityList()) {
            selectedList.append("<li>");
            selectedList.append(confidentiality.getDisplayedValue());
            selectedList.append("</li>");
        }
        selectedList.append("</ul>");

        Map<String, Object> keepDetailsPrivateList = new HashMap<>();
        keepDetailsPrivateList.put("confidentialListDetails", selectedList);
        if (caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate()
                .getConfidentialityList().contains(ConfidentialityListEnum.address)) {
            keepDetailsPrivateList.put("isAddressConfidential", YesOrNo.Yes);
        }
        if (caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate()
                .getConfidentialityList().contains(ConfidentialityListEnum.phoneNumber)) {
            keepDetailsPrivateList.put("isPhoneNumberConfidential", YesOrNo.Yes);
        }
        if (caseData.getRespondentSolicitorData().getKeepContactDetailsPrivate()
                .getConfidentialityList().contains(ConfidentialityListEnum.email)) {
            keepDetailsPrivateList.put("isEmailAddressConfidential", YesOrNo.Yes);
        }
        keepDetailsPrivateList.put(RESPONDENT_NAME_FOR_RESPONSE, caseData.getRespondentSolicitorData().getRespondentNameForResponse());
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
        boolean mandatoryFinished = false;
        generateDraftDocumentsForRespondent(callbackRequest, authorisation);
        if (!caseData.getRespondents().isEmpty()) {
            Optional<SolicitorRole> solicitorRole = SolicitorRole.from(invokingRespondent);
            if (solicitorRole.isPresent() && caseData.getRespondents().size() > solicitorRole.get().getIndex()) {
                Element<PartyDetails> respondingParty = caseData.getRespondents().get(solicitorRole.get().getIndex());

                if (respondingParty.getValue() != null
                        && respondingParty.getValue().getUser() != null
                        && YesOrNo.Yes.equals(respondingParty.getValue().getUser().getSolicitorRepresented())) {
                    boolean isC1aApplicable = caseData.getC1ADocument() != null;
                    mandatoryFinished = responseSubmitChecker.isFinished(respondingParty.getValue(), isC1aApplicable);
                    caseDataUpdated.put(
                        "respondentSolicitorName",
                        respondingParty.getValue().getRepresentativeFirstName() + " " + respondingParty.getValue().getRepresentativeLastName()
                    );
                }
            }
        }
        if (!mandatoryFinished) {
            errorList.add(
                    "Response submission is not allowed for this case unless you finish all the mandatory information");
        }
        return caseDataUpdated;
    }

    public Map<String, Object> submitC7ResponseForActiveRespondent(String authorisation, CallbackRequest callbackRequest) throws Exception {
        Map<String, Object> updatedCaseData = new HashMap<>(callbackRequest.getCaseDetails().getData());
        updatedCaseData.remove("respondentSolicitorName");

        List<QuarantineLegalDoc> quarantineLegalDocList = new ArrayList<>();
        UserDetails userDetails = userService.getUserDetails(authorisation);

        final String[] surname = {null};
        userDetails.getSurname().ifPresent(snm -> surname[0] = snm);

        UserDetails updatedUserDetails = UserDetails.builder()
            .email(userDetails.getEmail())
            .id(userDetails.getId())
            .surname(surname[0])
            .forename(userDetails.getForename())
            .roles(manageDocumentsService.getLoggedInUserType(authorisation))
            .build();

        CaseData caseData = objectMapper.convertValue(updatedCaseData, CaseData.class);
        Optional<SolicitorRole> solicitorRole = getSolicitorRole(callbackRequest);
        Element<PartyDetails> representedRespondent;

        if (solicitorRole.isPresent()) {
            representedRespondent = findSolicitorRepresentedRespondents(callbackRequest, solicitorRole.get());
        } else {
            representedRespondent = null;
        }

        if (representedRespondent != null && representedRespondent.getValue() != null && PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(
                caseData.getCaseTypeOfApplication())) {

            if (representedRespondent.getValue().getResponse().getResponseToAllegationsOfHarm() != null
                    && representedRespondent.getValue().getResponse().getResponseToAllegationsOfHarm()
                    .getResponseToAllegationsOfHarmDocument() != null) {

                quarantineLegalDocList.add(getQuarantineLegalDocuments(
                    updatedUserDetails,
                        representedRespondent.getValue().getResponse()
                                .getResponseToAllegationsOfHarm().getResponseToAllegationsOfHarmDocument(),
                    "respondentC1AResponse", "Respondent C1A response",
                        representedRespondent.getValue().getLabelForDynamicList(),
                        String.valueOf(representedRespondent.getId())));
            }

            updateListWithPreviousOrderDocuments(updatedUserDetails, quarantineLegalDocList, representedRespondent);
            PartyDetails amended = representedRespondent.getValue().toBuilder()
                    .response(representedRespondent.getValue().getResponse().toBuilder().c7ResponseSubmitted(Yes)
                            .responseToAllegationsOfHarm(ResponseToAllegationsOfHarm.builder()
                                    .responseToAllegationsOfHarmYesOrNoResponse(
                                            representedRespondent.getValue()
                                                    .getResponse().getResponseToAllegationsOfHarm()
                                                    .getResponseToAllegationsOfHarmYesOrNoResponse())
                                    .build())
                              .respondentExistingProceedings(getAmendedProceedings(representedRespondent))
                              .build())
                .build();

            amended = updatedRefugeData(amended);
            amended = updatedContactDetails(amended);
            caseData = updateRefugeDocumentList(caseData, amended);

            String party = representedRespondent.getValue().getLabelForDynamicList();
            caseData.getRespondents().set(
                caseData.getRespondents().indexOf(representedRespondent),
                element(representedRespondent.getId(), amended)
            );

            confidentialityC8RefugeService.processRefugeDocumentsC7ResponseSubmission(
                updatedCaseData,
                representedRespondent.getValue(),
                caseData.getRefugeDocuments(),
                caseData.getHistoricalRefugeDocuments(),
                caseData.getRespondents().indexOf(representedRespondent) + 1
            );
            String createdBy = StringUtils.isEmpty(representedRespondent.getValue().getRepresentativeFullNameForCaseFlags())
                    ? party : representedRespondent.getValue().getRepresentativeFullNameForCaseFlags() + SOLICITOR;
            updatedCaseData.put(RESPONDENTS, caseData.getRespondents());

            Map<String, Object> dataMap = generateRespondentDocsAndUpdateCaseData(
                authorisation,
                callbackRequest,
                caseData,
                representedRespondent,
                quarantineLegalDocList
            );

            generateC8AndUpdateCaseData(
                authorisation,
                updatedCaseData,
                caseData,
                solicitorRole,
                party,
                createdBy,
                dataMap
            );
        }

        moveRespondentDocumentsToQuarantineTab(updatedCaseData, userDetails, quarantineLegalDocList);

        updatedCaseData.put(C100_RESPONDENT_TABLE, applicationsTabService.getRespondentsTable(caseData));

        return updatedCaseData;
    }

    private CaseData updateRefugeDocumentList(CaseData caseData, PartyDetails respondent) {

        if (YesOrNo.Yes.equals(respondent.getLiveInRefuge())
            && respondent.getRefugeConfidentialityC8Form() != null) {
            log.info("Respondent lives in refuge");
            List<Element<RefugeConfidentialDocuments>> refugeDocuments = caseData.getRefugeDocuments();

            RefugeConfidentialDocuments refugeConfidentialDocuments
                = RefugeConfidentialDocuments
                .builder()
                .partyType("Respondent")
                .partyName(respondent.getLabelForDynamicList())
                .documentDetails(DocumentDetails.builder()
                    .documentName(respondent.getRefugeConfidentialityC8Form().getDocumentFileName())
                    .documentUploadedDate(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)).format(
                        dateTimeFormatter)).build())
                .document(respondent.getRefugeConfidentialityC8Form()).build();

            if (refugeDocuments == null) {
                refugeDocuments = new ArrayList<>();
            }
            refugeDocuments.add(ElementUtils.element(refugeConfidentialDocuments));
            caseData = caseData.toBuilder()
                .refugeDocuments(refugeDocuments)
                .build();
        }


        return caseData;
    }


    private PartyDetails updatedRefugeData(PartyDetails respondent) {
        if (null != respondent.getResponse()
            && null != respondent.getResponse().getCitizenDetails()
            && YesOrNo.Yes.equals(respondent.getResponse().getCitizenDetails().getLiveInRefuge())) {
            respondent = respondent.toBuilder()
                .liveInRefuge(Yes)
                .refugeConfidentialityC8Form(respondent
                                                 .getResponse()
                                                 .getCitizenDetails()
                                                 .getRefugeConfidentialityC8Form())
                .build();
        }
        return respondent;
    }

    private PartyDetails updatedContactDetails(PartyDetails respondent) {
        if (null != respondent.getResponse()
            && null != respondent.getResponse().getCitizenDetails()) {
            respondent = respondent.toBuilder()
                .firstName(respondent.getResponse().getCitizenDetails().getFirstName())
                .lastName(respondent.getResponse().getCitizenDetails().getLastName())
                .previousName(respondent.getResponse().getCitizenDetails().getPreviousName())
                .dateOfBirth(respondent.getResponse().getCitizenDetails().getDateOfBirth())
                .placeOfBirth(respondent.getResponse().getCitizenDetails().getPlaceOfBirth())
                .liveInRefuge(respondent.getResponse().getCitizenDetails().getLiveInRefuge())
                .address(respondent.getResponse().getCitizenDetails().getAddress())
                .email(respondent.getResponse().getCitizenDetails().getContact() != null
                        ? respondent.getResponse().getCitizenDetails().getContact().getEmail() : null)
                .phoneNumber(respondent.getResponse().getCitizenDetails().getContact() != null
                        ? respondent.getResponse().getCitizenDetails().getContact().getPhoneNumber() : null)
                .build();
        }
        return respondent;
    }

    private List<Element<RespondentProceedingDetails>> getAmendedProceedings(Element<PartyDetails> representedRespondent) {
        List<Element<RespondentProceedingDetails>> amendedList = new ArrayList<>();
        if (null != representedRespondent.getValue().getResponse().getRespondentExistingProceedings()) {
            representedRespondent.getValue().getResponse().getRespondentExistingProceedings()
                    .forEach(proceeding ->
                            amendedList.add(Element.<RespondentProceedingDetails>builder()
                                    .value(RespondentProceedingDetails.builder()
                                            .caseNumber(proceeding.getValue().getCaseNumber())
                                            .dateStarted(proceeding.getValue().getDateStarted())
                                            .dateEnded(proceeding.getValue().getDateEnded())
                                            .nameAndOffice(proceeding.getValue().getNameAndOffice())
                                            .nameOfChildrenInvolved(proceeding.getValue().getNameOfChildrenInvolved())
                                            .nameOfCourt(proceeding.getValue().getNameOfCourt())
                                            .nameOfGuardian(proceeding.getValue().getNameOfGuardian())
                                            .nameOfJudge(proceeding.getValue().getNameOfJudge())
                                            .previousOrOngoingProceedings(proceeding.getValue()
                                                    .getPreviousOrOngoingProceedings())
                                            .otherTypeOfOrder(proceeding.getValue().getOtherTypeOfOrder())
                                            .typeOfOrder(proceeding.getValue().getTypeOfOrder())
                                            .build())
                                    .build()));
        }

        return  amendedList;
    }

    private void updateListWithPreviousOrderDocuments(UserDetails updatedUserDetails,
                                                      List<QuarantineLegalDoc> quarantineLegalDocList,
                                                      Element<PartyDetails> representedRespondent) {
        if (null != representedRespondent.getValue().getResponse().getRespondentExistingProceedings()) {
            representedRespondent.getValue().getResponse().getRespondentExistingProceedings().stream()
                    .filter(
                            proceedings -> Objects.nonNull(proceedings) && null != proceedings.getValue().getUploadRelevantOrder())
                    .forEach(
                            proceedings -> {
                                quarantineLegalDocList.add(getQuarantineLegalDocuments(
                                        updatedUserDetails,
                                        proceedings.getValue().getUploadRelevantOrder(),
                                        "ordersFromOtherProceedings",
                                        "Orders from other proceedings",
                                        representedRespondent.getValue().getLabelForDynamicList(),
                                        String.valueOf(representedRespondent.getId())
                                ));
                                proceedings.getValue().toBuilder().uploadRelevantOrder(null);
                            });
        }
    }

    private Map<String, Object> generateRespondentDocsAndUpdateCaseData(
            String authorisation,
            CallbackRequest callbackRequest,
            CaseData caseData,
            Element<PartyDetails> representedRespondent,
            List<QuarantineLegalDoc> quarantineLegalDocList
    ) throws Exception {

        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);

        Map<String, Object> dataMap = populateDataMap(callbackRequest, representedRespondent, SOLICITOR);
        UserDetails userDetails = userService.getUserDetails(authorisation);
        final String[] surname = {null};
        userDetails.getSurname().ifPresent(snm -> surname[0] = snm);
        UserDetails updatedUserDetails = UserDetails.builder()
                .email(userDetails.getEmail())
                .id(userDetails.getId())
                .surname(surname[0])
                .forename(userDetails.getForename() != null ? userDetails.getForename() : null)
                .roles(manageDocumentsService.getLoggedInUserType(authorisation))
                .build();

        if (documentLanguage.isGenEng()) {
            Document c7FinalDocument = documentGenService.generateSingleDocument(
                    authorisation,
                    caseData,
                    SOLICITOR_C7_FINAL_DOCUMENT,
                    false,
                    dataMap
            );

            quarantineLegalDocList.add(getQuarantineLegalDocuments(
                    updatedUserDetails,
                    c7FinalDocument,
                    RESPONDENT_APPLICATION,
                    RESPONDENT_APPLICATION_CATEGORY,
                    representedRespondent.getValue().getLabelForDynamicList(),
                    String.valueOf(representedRespondent.getId())
            ));
        }

        if (representedRespondent.getValue().getResponse() != null
                && representedRespondent.getValue().getResponse().getRespondentAllegationsOfHarmData() != null
                && Yes.equals(representedRespondent.getValue().getResponse().getRespondentAllegationsOfHarmData().getRespAohYesOrNo())) {
            if (documentLanguage.isGenEng()) {
                Document c1aFinalDocument = documentGenService.generateSingleDocument(
                        authorisation,
                        caseData,
                        SOLICITOR_C1A_FINAL_DOCUMENT,
                        false,
                        dataMap
                );
                quarantineLegalDocList.add(getQuarantineLegalDocuments(
                        updatedUserDetails,
                        c1aFinalDocument,
                        RESPONDENT_C1A_APPLICATION,
                        RESPONDENT_C1A_APPLICATION_CATEGORY,
                        representedRespondent.getValue().getLabelForDynamicList(),
                        String.valueOf(representedRespondent.getId())
                ));

            }

            if (documentLanguage.isGenWelsh()) {
                dataMap.put(RESP_CHILD_ABUSES_DOCMOSIS, getChildAbuses(representedRespondent));
                Document c1aFinalDocumentWelsh = documentGenService.generateSingleDocument(
                        authorisation,
                        caseData,
                        SOLICITOR_C1A_WELSH_FINAL_DOCUMENT,
                        true,
                        dataMap
                );
                quarantineLegalDocList.add(getQuarantineLegalDocuments(
                        updatedUserDetails,
                        c1aFinalDocumentWelsh,
                        RESPONDENT_C1A_APPLICATION,
                        RESPONDENT_C1A_APPLICATION_CATEGORY,
                        representedRespondent.getValue().getLabelForDynamicList(),
                        String.valueOf(representedRespondent.getId())
                ));
            }
        }

        if (documentLanguage.isGenWelsh()) {
            Document c7WelshFinalDocument = documentGenService.generateSingleDocument(
                    authorisation,
                    caseData,
                    SOLICITOR_C7_FINAL_DOCUMENT,
                    true,
                    dataMap
            );
            quarantineLegalDocList.add(getQuarantineLegalDocuments(
                    updatedUserDetails,
                    c7WelshFinalDocument,
                    RESPONDENT_APPLICATION,
                    RESPONDENT_APPLICATION_CATEGORY,
                    representedRespondent.getValue().getLabelForDynamicList(),
                    String.valueOf(representedRespondent.getId())
            ));
        }
        return dataMap;
    }

    private void generateC8AndUpdateCaseData(
            String authorisation,
            Map<String, Object> updatedCaseData,
            CaseData caseData,
            Optional<SolicitorRole> solicitorRole,
            String party,
            String createdBy,
            Map<String, Object> dataMap
    ) throws Exception {
        Document c8FinalDocument = null;
        Document c8FinalDocumentWel = null;
        if (dataMap.containsKey(IS_CONFIDENTIAL_DATA_PRESENT)) {

            //prl-6790 - getting user-role and adding to datamap
            dataMap.put("loggedInUserRole", manageOrderService.getLoggedInUserType(authorisation));

            DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
            if (documentLanguage.isGenWelsh()) {
                c8FinalDocumentWel = documentGenService.generateSingleDocument(
                        authorisation,
                        caseData,
                        C8_RESP_FINAL_HINT,
                        true,
                        dataMap
                );
            }
            c8FinalDocument = documentGenService.generateSingleDocument(
                    authorisation,
                    caseData,
                    C8_RESP_FINAL_HINT,
                    false,
                    dataMap
            );
        }
        updatedCaseData.put("finalC8ResponseDoc", c8FinalDocument);

        if (null != c8FinalDocument && solicitorRole.isPresent()) {
            updatedCaseData.put(
                    getKeyForDoc(solicitorRole.get()).get(0),
                    ResponseDocuments.builder()
                            .partyName(party)
                            .createdBy(createdBy)
                            .dateCreated(LocalDate.now())
                            .dateTimeCreated(LocalDateTime.now())
                            .citizenDocument(c8FinalDocument)
                            .respondentC8DocumentWelsh(c8FinalDocumentWel)
                            .build()
            );
        }
    }

    private List<String> getKeyForDoc(SolicitorRole solicitorRole) {
        String c8Key;
        switch (solicitorRole.getEventId()) {
            case "A":
                c8Key = "respondentAc8";
                break;
            case "B":
                c8Key = "respondentBc8";
                break;
            case "C":
                c8Key = "respondentCc8";
                break;
            case "D":
                c8Key = "respondentDc8";
                break;
            case "E":
                c8Key = "respondentEc8";
                break;
            default:
                c8Key = "";
                break;
        }
        return List.of(c8Key);
    }

    public Map<String, Object> populateDataMap(CallbackRequest callbackRequest,
                                               Element<PartyDetails> solicitorRepresentedRespondent,
                                               String requestOriginatedFrom) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(COURT_NAME_FIELD, callbackRequest.getCaseDetails().getData().get(COURT_NAME));
        dataMap.put(CASE_DATA_ID, callbackRequest.getCaseDetails().getId());
        dataMap.put(ISSUE_DATE_FIELD, callbackRequest.getCaseDetails().getData().get(ISSUE_DATE_FIELD));
        dataMap.put(COURT_SEAL_FIELD,
                callbackRequest.getCaseDetails().getData().get(COURT_SEAL_FIELD) == null ? "[userImage:familycourtseal.png]"
                        : callbackRequest.getCaseDetails().getData().get(COURT_SEAL_FIELD));
        if (callbackRequest.getCaseDetails().getData().get(TASK_LIST_VERSION) != null
                && (TASK_LIST_VERSION_V2.equalsIgnoreCase(String.valueOf(callbackRequest
                .getCaseDetails().getData().get(
                        TASK_LIST_VERSION)))
                || TASK_LIST_VERSION_V3.equalsIgnoreCase(String.valueOf(callbackRequest
                .getCaseDetails().getData().get(
                        TASK_LIST_VERSION))))) {
            List<Element<ChildDetailsRevised>> listOfChildren = (List<Element<ChildDetailsRevised>>) callbackRequest
                    .getCaseDetails().getData().get(
                            NEW_CHILDREN);
            dataMap.put(CHILDREN, listOfChildren);
        } else {
            List<Element<Child>> listOfChildren = (List<Element<Child>>) callbackRequest.getCaseDetails().getData().get(
                    CHILDREN);
            dataMap.put(CHILDREN, listOfChildren);

        }

        if (solicitorRepresentedRespondent == null) {
            Optional<SolicitorRole> solicitorRole = getSolicitorRole(callbackRequest);
            if (solicitorRole.isPresent()) {
                solicitorRepresentedRespondent = findSolicitorRepresentedRespondents(
                        callbackRequest,
                        solicitorRole.get()
                );
            }
        }
        populateConfidentialAndMiscDataMap(solicitorRepresentedRespondent, dataMap, requestOriginatedFrom);
        return dataMap;
    }

    public void populateConfidentialAndMiscDataMap(Element<PartyDetails> solicitorRepresentedRespondent,
                                                   Map<String, Object> dataMap, String requestOriginatedFrom) {
        boolean isConfidentialDataPresent = false;
        if (null != solicitorRepresentedRespondent
                && null != solicitorRepresentedRespondent.getValue()) {
            //PRL-6625 - Skip ref data call for citizen cases
            if (isNotEmpty(solicitorRepresentedRespondent.getValue().getSolicitorOrg())
                && isNotEmpty(solicitorRepresentedRespondent.getValue().getSolicitorOrg().getOrganisationID())) {
                getOrganisationAddress(solicitorRepresentedRespondent, dataMap);
            }
            if (!CITIZEN.equalsIgnoreCase(requestOriginatedFrom)) {
                dataMap.put(RESPONDENT, getRespondentPartyDetails(solicitorRepresentedRespondent));
            }
            Response response = solicitorRepresentedRespondent.getValue().getResponse();
            boolean isConfidentialSetByCitizen = isNotEmpty(solicitorRepresentedRespondent.getValue().getResponse())
                    && isNotEmpty(solicitorRepresentedRespondent.getValue().getResponse().getKeepDetailsPrivate())
                    && Yes.equals(solicitorRepresentedRespondent.getValue().getResponse().getKeepDetailsPrivate().getConfidentiality());

            isConfidentialDataPresent = populateEmailConfidentiality(
                    solicitorRepresentedRespondent,
                    isConfidentialSetByCitizen,
                    dataMap,
                    isConfidentialDataPresent,
                    response
            );
            isConfidentialDataPresent = populatePhoneNumberConfidentiality(
                    solicitorRepresentedRespondent,
                    isConfidentialSetByCitizen,
                    dataMap,
                    isConfidentialDataPresent,
                    response
            );
            isConfidentialDataPresent = populateAddressConfidentiality(
                    solicitorRepresentedRespondent,
                    isConfidentialSetByCitizen,
                    dataMap,
                    isConfidentialDataPresent,
                    response,
                    requestOriginatedFrom
            );
            populateRepresentativeDetails(solicitorRepresentedRespondent, dataMap);
            populatePartyDetails(solicitorRepresentedRespondent, response, dataMap);
            populateMiscellaneousDetails(solicitorRepresentedRespondent, dataMap, response, requestOriginatedFrom);
            if (isConfidentialDataPresent) {
                dataMap.put(IS_CONFIDENTIAL_DATA_PRESENT, isConfidentialDataPresent);
            }
        }
    }

    private void populateMiscellaneousDetails(Element<PartyDetails> solicitorRepresentedRespondent,
                                              Map<String, Object> dataMap,
                                              Response response,
                                              String requestOriginatedFrom) {
        populateAohDataMap(response, dataMap);
        populateRespondToAohDataMap(response, dataMap);
        if (null != response.getConsent()) {
            dataMap.put("applicationReceivedDate", response.getConsent().getApplicationReceivedDate());
            dataMap.put(
                "consentToTheApplication",
                getValueForYesOrNoEnum(response.getConsent().getConsentToTheApplication())
            );
            dataMap.put("noConsentReason", response.getConsent().getNoConsentReason());
            dataMap.put("permissionFromCourt", getValueForYesOrNoEnum(response.getConsent().getPermissionFromCourt()));
            dataMap.put("courtOrderDetails", response.getConsent().getCourtOrderDetails());
        }
        //citizen current or previous proceeding data
        if (null != response.getCurrentOrPreviousProceedings()) {
            dataMap.put(
                    "haveChildrenBeenInvolvedInCourtCase",
                    getValueForYesOrNoEnum(response.getCurrentOrPreviousProceedings().getHaveChildrenBeenInvolvedInCourtCase())
            );
            dataMap.put(
                    "courtOrderMadeForProtection",
                    getValueForYesOrNoEnum(response.getCurrentOrPreviousProceedings().getCourtOrderMadeForProtection())
            );
            dataMap.put("proceedingsList", response.getCurrentOrPreviousProceedings().getProceedingsList());
        }
        dataMap.put("signedBy", solicitorRepresentedRespondent.getValue().getLabelForDynamicList());
        dataMap.put("signedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));

        if (null != response.getMiam()) {
            dataMap.put("attendedMiam", getValueForYesOrNoEnum(response.getMiam().getAttendedMiam()));
            dataMap.put("willingToAttendMiam", getValueForYesOrNoEnum(response.getMiam().getWillingToAttendMiam()));
            dataMap.put("reasonNotAttendingMiam", response.getMiam().getReasonNotAttendingMiam());
        }

        if (response.getCitizenInternationalElements() != null) {
            dataMap.put(
                    "reasonForChild",
                    null != response.getCitizenInternationalElements().getChildrenLiveOutsideOfEnWl()
                            ? response.getCitizenInternationalElements().getChildrenLiveOutsideOfEnWl().getDisplayedValue() : null
            );
            dataMap.put(
                    "reasonForChildDetails",
                    response.getCitizenInternationalElements().getChildrenLiveOutsideOfEnWlDetails()
            );
            dataMap.put(
                    "reasonForParent",
                    null != response.getCitizenInternationalElements().getParentsAnyOneLiveOutsideEnWl() ? response
                            .getCitizenInternationalElements().getParentsAnyOneLiveOutsideEnWl().getDisplayedValue() : null
            );
            dataMap.put(
                    "reasonForParentDetails",
                    response.getCitizenInternationalElements().getParentsAnyOneLiveOutsideEnWlDetails()
            );
            dataMap.put(
                    "reasonForJurisdiction",
                    getValueForYesOrNoEnum(response.getCitizenInternationalElements().getAnotherPersonOrderOutsideEnWl())
            );
            dataMap.put(
                    "reasonForJurisdictionDetails",
                    response.getCitizenInternationalElements().getAnotherPersonOrderOutsideEnWlDetails()
            );
            dataMap.put(
                    "requestToAuthority",
                    getValueForYesOrNoEnum(response.getCitizenInternationalElements().getAnotherCountryAskedInformation())
            );
            dataMap.put(
                    "requestToAuthorityDetails",
                    response.getCitizenInternationalElements().getAnotherCountryAskedInformationDetaails()
            );
        }
        dataMap.put(
                "solicitorRepresented",
                null != solicitorRepresentedRespondent.getValue().getUser().getSolicitorRepresented()
                        ? solicitorRepresentedRespondent.getValue().getUser().getSolicitorRepresented() : No
        );
        if (null != response.getSupportYouNeed()) {
            dataMap.put("reasonableAdjustments", response.getSupportYouNeed().getReasonableAdjustments());
        }

        if (CITIZEN.equalsIgnoreCase(requestOriginatedFrom)) {
            populateCitizenAttendingTheHearingDataMap(response, dataMap);
            if (response.getCurrentOrPreviousProceedings() != null) {
                List<Element<Proceedings>> proceedingsList
                        = response.getCurrentOrPreviousProceedings().getProceedingsList();
                dataMap.put("currentOrPreviousProceedings", proceedingsList);
                dataMap.put(
                        "haveChildrenBeenInvolvedInCourtCase",
                        getValueForYesOrNoEnum(response.getCurrentOrPreviousProceedings().getHaveChildrenBeenInvolvedInCourtCase())
                );
                dataMap.put(
                        "courtOrderMadeForProtection",
                        getValueForYesOrNoEnum(response.getCurrentOrPreviousProceedings().getCourtOrderMadeForProtection())
                );
            }
        } else {
            dataMap.put(ATTENDING_THE_COURT, response.getAttendToCourt());
            dataMap.put(ATTENDING_THE_COURT_WELSH, response.getAttendToCourt());
            List<Element<RespondentProceedingDetails>> proceedingsList = response.getRespondentExistingProceedings();
            dataMap.put("respondentsExistingProceedings", proceedingsList);
            dataMap.put(
                    "currentOrPastProceedingsForChildren",
                    response.getCurrentOrPastProceedingsForChildren() != null
                            ? response.getCurrentOrPastProceedingsForChildren().getDisplayedValue() : null
            );
        }
        if (null != response.getResponseToAllegationsOfHarm()
                && null != response.getResponseToAllegationsOfHarm().getResponseToAllegationsOfHarmYesOrNoResponse()) {
            dataMap.put("isRespondToAllegationOfHarm", getValueForYesOrNoEnum(response.getResponseToAllegationsOfHarm()
                    .getResponseToAllegationsOfHarmYesOrNoResponse()));
        }
    }

    private String getValueForYesOrNoEnum(YesOrNo yesOrNo) {
        return null != yesOrNo ? yesOrNo.getDisplayedValue() : null;
    }

    private void populatePartyDetails(Element<PartyDetails> solicitorRepresentedRespondent, Response response, Map<String, Object> dataMap) {
        if (isNotEmpty(response) && isNotEmpty(response.getCitizenDetails()) && null != response.getCitizenDetails().getFirstName()
            && null != response.getCitizenDetails()
                .getLastName()) {
            dataMap.put("fullName", response.getCitizenDetails()
                    .getFirstName() + EMPTY_SPACE_STRING + response.getCitizenDetails()
                    .getLastName());
        } else {
            dataMap.put("fullName", solicitorRepresentedRespondent.getValue()
                    .getFirstName() + EMPTY_SPACE_STRING + solicitorRepresentedRespondent.getValue()
                    .getLastName());
        }
        if (isNotEmpty(response) && isNotEmpty(response.getCitizenDetails()) && null != response.getCitizenDetails().getDateOfBirth()) {
            dataMap.put("dob", response.getCitizenDetails().getDateOfBirth());
        } else {
            dataMap.put("dob", solicitorRepresentedRespondent.getValue().getDateOfBirth());
        }
        if (null != solicitorRepresentedRespondent.getValue().getGender()) {
            dataMap.put("gender", solicitorRepresentedRespondent.getValue().getGender().getDisplayedValue());
        }
        if (null != solicitorRepresentedRespondent.getValue().getAddressLivedLessThan5YearsDetails()) {
            dataMap.put("addressHistory", solicitorRepresentedRespondent.getValue().getAddressLivedLessThan5YearsDetails());
        }
    }

    private void populateRepresentativeDetails(Element<PartyDetails> solicitorRepresentedRespondent, Map<String, Object> dataMap) {
        if (null != solicitorRepresentedRespondent.getValue().getRepresentativeFirstName()
                && null != solicitorRepresentedRespondent.getValue().getRepresentativeLastName()) {
            dataMap.put("repFirstName", solicitorRepresentedRespondent.getValue().getRepresentativeFirstName());
            dataMap.put("repLastName", solicitorRepresentedRespondent.getValue().getRepresentativeLastName());
            dataMap.put("repFullName", solicitorRepresentedRespondent
                    .getValue().getRepresentativeFirstName() + " " + solicitorRepresentedRespondent
                    .getValue().getRepresentativeLastName());
        }
        if (null != solicitorRepresentedRespondent.getValue().getSolicitorAddress()) {
            populateAddressMap(solicitorRepresentedRespondent, dataMap);
        }
        dataMap.put("repEmail", solicitorRepresentedRespondent.getValue().getSolicitorEmail());
        dataMap.put("repTelephone", solicitorRepresentedRespondent.getValue().getSolicitorTelephone());
        if (solicitorRepresentedRespondent.getValue().getDxNumber() != null) {
            dataMap.put("dxNumber", solicitorRepresentedRespondent.getValue().getDxNumber());
        } else {
            if (solicitorRepresentedRespondent.getValue().getOrganisations() != null) {
                for (ContactInformation contactInformationLoop : solicitorRepresentedRespondent
                        .getValue().getOrganisations().getContactInformation()) {
                    for (DxAddress dxAddress : contactInformationLoop.getDxAddress()) {
                        dataMap.put("dxNumber", dxAddress.getDxNumber());
                    }
                }
            }
        }
        dataMap.put("repReference", solicitorRepresentedRespondent.getValue().getSolicitorReference());
    }

    private static boolean populateEmailConfidentiality(Element<PartyDetails> solicitorRepresentedRespondent,
                                                        boolean isConfidentialSetByCitizen,
                                                        Map<String, Object> dataMap,
                                                        boolean isConfidentialDataPresent,
                                                        Response response) {
        if (Yes.equals(solicitorRepresentedRespondent.getValue().getIsEmailAddressConfidential())
            && Yes.equals(solicitorRepresentedRespondent.getValue().getCanYouProvideEmailAddress())
                || (isConfidentialSetByCitizen
                && solicitorRepresentedRespondent.getValue().getResponse().getKeepDetailsPrivate().getConfidentialityList()
                .contains(ConfidentialityListEnum.email))) {
            dataMap.put(EMAIL, THIS_INFORMATION_IS_CONFIDENTIAL);
            isConfidentialDataPresent = true;
        } else if (null != response.getCitizenDetails()
                && null != response.getCitizenDetails().getContact()
                && StringUtils.isNoneEmpty(response.getCitizenDetails().getContact().getEmail())) {
            dataMap.put(EMAIL, response.getCitizenDetails().getContact().getEmail());
        } else {
            dataMap.put(EMAIL, solicitorRepresentedRespondent.getValue().getEmail());
        }
        return isConfidentialDataPresent;
    }

    private static boolean populatePhoneNumberConfidentiality(Element<PartyDetails> solicitorRepresentedRespondent,
                                                              boolean isConfidentialSetByCitizen,
                                                              Map<String, Object> dataMap,
                                                              boolean isConfidentialDataPresent,
                                                              Response response) {
        if (Yes.equals(solicitorRepresentedRespondent.getValue().getIsPhoneNumberConfidential())
            && Yes.equals(solicitorRepresentedRespondent.getValue().getCanYouProvidePhoneNumber())
                || (isConfidentialSetByCitizen
                && solicitorRepresentedRespondent.getValue().getResponse().getKeepDetailsPrivate().getConfidentialityList()
                .contains(ConfidentialityListEnum.phoneNumber))) {
            dataMap.put(PHONE, THIS_INFORMATION_IS_CONFIDENTIAL);
            isConfidentialDataPresent = true;
        } else if (null != response.getCitizenDetails().getContact()
                && StringUtils.isNoneEmpty(response.getCitizenDetails().getContact().getPhoneNumber())) {
            dataMap.put(PHONE, response.getCitizenDetails().getContact().getPhoneNumber());
        } else {
            dataMap.put(PHONE, solicitorRepresentedRespondent.getValue().getPhoneNumber());
        }
        return isConfidentialDataPresent;
    }

    private static boolean populateAddressConfidentiality(Element<PartyDetails> solicitorRepresentedRespondent,
                                                          boolean isConfidentialSetByCitizen,
                                                          Map<String, Object> dataMap,
                                                          boolean isConfidentialDataPresent,
                                                          Response response,
                                                          String requestOriginatedFrom) {
        if (Yes.equals(solicitorRepresentedRespondent.getValue().getIsAddressConfidential())
                && Yes.equals(solicitorRepresentedRespondent.getValue().getIsCurrentAddressKnown())
                || (isConfidentialSetByCitizen
                && solicitorRepresentedRespondent.getValue().getResponse().getKeepDetailsPrivate().getConfidentialityList()
                .contains(ConfidentialityListEnum.address))) {
            dataMap.put(ADDRESS, THIS_INFORMATION_IS_CONFIDENTIAL);
            isConfidentialDataPresent = true;
        } else if (CITIZEN.equalsIgnoreCase(requestOriginatedFrom)) {
            if (null != solicitorRepresentedRespondent.getValue().getAddress()) {
                dataMap.put(ADDRESS, CaseUtils.formatAddress(solicitorRepresentedRespondent.getValue().getAddress()));
            } else if (null != response.getCitizenDetails().getAddress()) {
                dataMap.put(ADDRESS, CaseUtils.formatAddress(response.getCitizenDetails().getAddress()));
            }
        } else {
            if (null != response.getCitizenDetails().getAddress()) {
                dataMap.put(ADDRESS, CaseUtils.formatAddress(response.getCitizenDetails().getAddress()));
            } else if (null != solicitorRepresentedRespondent.getValue().getAddress()) {
                dataMap.put(ADDRESS, CaseUtils.formatAddress(solicitorRepresentedRespondent.getValue().getAddress()));
            }
        }
        return isConfidentialDataPresent;
    }

    private void populateAohDataMap(Response response, Map<String, Object> dataMap) {
        if (isNotEmpty(response) && response.getRespondentAllegationsOfHarmData() != null) {
            RespondentAllegationsOfHarmData allegationsOfHarmData = response.getRespondentAllegationsOfHarmData();
            dataMap.put(RESP_CHILD_ABUSES_DOCMOSIS,respondentAllegationOfHarmService
                    .updateChildAbusesForDocmosis(allegationsOfHarmData));
            dataMap.putAll(objectMapper.convertValue(allegationsOfHarmData,new TypeReference<Map<String, Object>>() {}));

        }
    }

    public void populateAohDataMapForWelsh(Map<String, Object> dataMap) {
        if (dataMap.containsKey(RESP_CHILD_ABUSES_DOCMOSIS)) {
            List<Element<RespChildAbuseBehaviour>> childAbuses = objectMapper.convertValue(dataMap.get(
                    RESP_CHILD_ABUSES_DOCMOSIS), new TypeReference<List<Element<RespChildAbuseBehaviour>>>() {});
            List<Map<String, Object>> childAbusesList = new ArrayList<>();
            for (Element<RespChildAbuseBehaviour> el : childAbuses) {
                childAbusesList.add(objectMapper.convertValue(el, Map.class));
            }
            dataMap.put(RESP_CHILD_ABUSES_DOCMOSIS, childAbusesList);
        }
    }

    private void populateCitizenAttendingTheHearingDataMap(Response response, Map<String, Object> dataMap) {
        if (response.getSupportYouNeed() != null) {
            List<AttendingToCourtEnum> attendingToCourtEnumList = response.getSupportYouNeed().getAttendingToCourt();
            if (attendingToCourtEnumList != null && !attendingToCourtEnumList.isEmpty()) {
                if (attendingToCourtEnumList.contains(AttendingToCourtEnum.videohearings)) {
                    dataMap.put("hearingsByVideo", "Yes");
                }
                if (attendingToCourtEnumList.contains(AttendingToCourtEnum.phonehearings)) {
                    dataMap.put("hearingsByPhone", "Yes");
                }
            }

            AttendToCourt attendToCourt = AttendToCourt.builder().build();
            List<LanguageRequirementsEnum> languageRequirementsEnums = response.getSupportYouNeed().getLanguageRequirements();
            if (languageRequirementsEnums != null && !languageRequirementsEnums.isEmpty()) {
                attendToCourt = attendToCourt.toBuilder()
                        .respondentWelshNeeds(buildIsWelshNeeded(languageRequirementsEnums))
                        .respondentWelshNeedsList(buildRespondentWelshNeedsList(languageRequirementsEnums))
                        .build();
            }

            AttendToCourt attendToCourtWelsh = attendToCourt.toBuilder().build();

            attendToCourtWelsh = buildSafetyArrangementsList(response, attendToCourtWelsh, false);
            attendToCourt = buildSafetyArrangementsList(response, attendToCourt, true);

            attendToCourtWelsh = buildReasonableAdjustmentNeeds(response, dataMap, attendToCourtWelsh, false);
            attendToCourt = buildReasonableAdjustmentNeeds(response, dataMap, attendToCourt, true);

            dataMap.put(ATTENDING_THE_COURT, attendToCourt);
            dataMap.put(ATTENDING_THE_COURT_WELSH, attendToCourtWelsh);
        }
    }

    private static AttendToCourt buildSafetyArrangementsList(Response response, AttendToCourt attendToCourt, boolean isEnglish) {
        List<SafetyArrangementsEnum> safetyArrangementsEnumList = response.getSupportYouNeed().getSafetyArrangements();
        if (safetyArrangementsEnumList != null && !safetyArrangementsEnumList.isEmpty()) {
            attendToCourt = attendToCourt.toBuilder()
                    .respondentSpecialArrangements(buildSpecialArrangementRequired(safetyArrangementsEnumList))
                    .respondentSpecialArrangementDetails(
                            buildSpecialArrangementList(
                                    safetyArrangementsEnumList,
                                    response.getSupportYouNeed().getSafetyArrangementsDetails(),
                                    isEnglish
                            ))
                    .build();
        }
        return attendToCourt;
    }

    private static AttendToCourt buildReasonableAdjustmentNeeds(Response response, Map<String, Object> dataMap,
                                                                AttendToCourt attendToCourt, boolean isEnglish) {
        List<ReasonableAdjustmentsEnum> reasonableAdjustmentsEnumList = response.getSupportYouNeed().getReasonableAdjustments();
        if (reasonableAdjustmentsEnumList != null && !reasonableAdjustmentsEnumList.isEmpty()) {
            attendToCourt = attendToCourt.toBuilder()
                    .haveAnyDisability(buildHaveAnyDisability(reasonableAdjustmentsEnumList))
                    .disabilityNeeds(
                            buildDisabilityNeeds(
                                    response.getSupportYouNeed(), dataMap, isEnglish
                            ))
                    .build();
        }
        return attendToCourt;
    }

    private static YesOrNo buildIsWelshNeeded(List<LanguageRequirementsEnum> languageRequirementsEnums) {
        if (languageRequirementsEnums.contains(LanguageRequirementsEnum.speakwelsh)
                || (languageRequirementsEnums.contains(LanguageRequirementsEnum.readandwritewelsh))) {
            return YesOrNo.Yes;
        }
        return YesOrNo.No;
    }

    private static List<RespondentWelshNeedsListEnum> buildRespondentWelshNeedsList(List<LanguageRequirementsEnum> languageRequirementsEnums) {
        List<RespondentWelshNeedsListEnum> respondentWelshNeedsListEnums = new ArrayList<>();
        if (languageRequirementsEnums.contains(LanguageRequirementsEnum.speakwelsh)) {
            respondentWelshNeedsListEnums.add(RespondentWelshNeedsListEnum.speakWelsh);
        }
        if (languageRequirementsEnums.contains(LanguageRequirementsEnum.readandwritewelsh)) {
            respondentWelshNeedsListEnums.add(RespondentWelshNeedsListEnum.readAndWriteWelsh);
        }

        return respondentWelshNeedsListEnums;
    }

    private static String buildSpecialArrangementList(List<SafetyArrangementsEnum> safetyArrangementsEnumList,
                                                      String otherSubField, boolean isEnglish) {
        String specialArrangement = safetyArrangementsEnumList.stream()
            .map(element -> {
                if (isEnglish) {
                    return SafetyArrangementsEnum.valueOf(element.getId()).getDisplayedValue();
                } else {
                    Map<String, String> welshMap = new HashMap<>();
                    WelshLangMapper.getSpecialArrangementsWelsh(welshMap);
                    return welshMap.get(SafetyArrangementsEnum.valueOf(element.getId()).getDisplayedValue());
                }
            })
            .collect(Collectors.joining(COMMA_SEPARATOR));
        if (StringUtils.isNotEmpty(otherSubField)) {
            return specialArrangement + OPEN_BRACKET + otherSubField + CLOSE_BRACKET;
        }
        return specialArrangement;
    }

    private static YesOrNo buildSpecialArrangementRequired(List<SafetyArrangementsEnum> safetyArrangementsEnumList) {
        Optional<SafetyArrangementsEnum> safetyArrangementsEnum
                = safetyArrangementsEnumList
                .stream()
                .filter(x -> x.getId().equalsIgnoreCase(noSafetyrequirements.getId()))
                .findFirst();
        return safetyArrangementsEnum.isPresent() ? YesOrNo.No : YesOrNo.Yes;
    }

    private static YesOrNo buildHaveAnyDisability(List<ReasonableAdjustmentsEnum> reasonableAdjustmentsEnums) {
        Optional<ReasonableAdjustmentsEnum> reasonableAdjustmentsEnum
                = reasonableAdjustmentsEnums
                .stream()
                .filter(x -> x.getId().equalsIgnoreCase(nosupport.getId()))
                .findFirst();
        return reasonableAdjustmentsEnum.isPresent() ? YesOrNo.No : YesOrNo.Yes;
    }

    private static String buildDisabilityNeeds(ReasonableAdjustmentsSupport supportYouNeed,
                                               Map<String, Object> dataMap, boolean isEnglish) {
        List<ReasonableAdjustmentsEnum> reasonableAdjustmentsEnums = supportYouNeed.getReasonableAdjustments();
        StringBuilder adjustmentRequired = new StringBuilder();
        String documentInformation;
        String communicationHelpDetails;
        String helpTravellingMovingBuildingSupportDetails;
        Map<String, String> welshMapping = new HashMap<>();
        WelshLangMapper.getReasonbleAdjustmentsWelsh(welshMapping);
        if (reasonableAdjustmentsEnums.contains(nosupport)) {
            return isEnglish ? nosupport.getDisplayedValue() : welshMapping.get(nosupport.getDisplayedValue());
        }
        if (reasonableAdjustmentsEnums.contains(docsformat)) {
            documentInformation = buildDocumentInformation(supportYouNeed.getDocsSupport(), supportYouNeed, isEnglish, welshMapping);
            if (!documentInformation.isEmpty()) {
                dataMap.put("documentsInAlternativeFormatNeeded", YES);
                if (isEnglish) {
                    dataMap.put("documentsInAlternativeFormatDetails", documentInformation);
                } else {
                    dataMap.put("documentsInAlternativeFormatDetailsWelsh", documentInformation);
                }
                adjustmentRequired.append(isEnglish ? docsformat.getDisplayedValue() : welshMapping.get(docsformat.getDisplayedValue()))
                    .append(COLON).append(documentInformation);
            }
        }
        if (reasonableAdjustmentsEnums.contains(commhelp)) {
            communicationHelpDetails = buildCommunicationHelp(supportYouNeed.getHelpCommunication(), supportYouNeed,
                                                              isEnglish, welshMapping);
            if (!communicationHelpDetails.isEmpty()) {
                dataMap.put("helpInCommunicationNeeded", YES);
                if (isEnglish) {
                    dataMap.put("helpInCommunicationDetails", communicationHelpDetails);
                } else {
                    dataMap.put("helpInCommunicationDetailsWelsh", communicationHelpDetails);
                }
                adjustmentRequired.append(COMMA_SEPARATOR).append(isEnglish ? commhelp.getDisplayedValue()
                                                                      : welshMapping.get(commhelp.getDisplayedValue()))
                    .append(COLON).append(communicationHelpDetails);
            }
        }
        buildHearingNeeds(supportYouNeed, dataMap, reasonableAdjustmentsEnums, adjustmentRequired, isEnglish, welshMapping);
        if (reasonableAdjustmentsEnums.contains(travellinghelp)) {
            helpTravellingMovingBuildingSupportDetails = buildHelpTravellingMovingBuildingSupport(supportYouNeed.getTravellingToCourt(),
                    supportYouNeed, isEnglish, welshMapping);
            if (!helpTravellingMovingBuildingSupportDetails.isEmpty()) {
                dataMap.put("helpNeededTravellingToNeeded", YES);
                if (isEnglish) {
                    dataMap.put("helpNeededTravellingToDetails", helpTravellingMovingBuildingSupportDetails);
                } else {
                    dataMap.put("helpNeededTravellingToDetailsWelsh", helpTravellingMovingBuildingSupportDetails);
                }
                adjustmentRequired.append(COMMA_SEPARATOR).append(isEnglish ? travellinghelp.getDisplayedValue()
                                                                      : welshMapping.get(travellinghelp.getDisplayedValue()))
                    .append(COLON).append(helpTravellingMovingBuildingSupportDetails);
            }
        }
        return String.valueOf(adjustmentRequired);
    }

    private static void buildHearingNeeds(ReasonableAdjustmentsSupport supportYouNeed,
                                          Map<String, Object> dataMap,
                                          List<ReasonableAdjustmentsEnum> reasonableAdjustmentsEnums,
                                          StringBuilder adjustmentRequired, boolean isEnglish, Map<String, String> welshMapping) {
        String feelComfortableSupportDetails;
        String extraSupportDetails;
        if (reasonableAdjustmentsEnums.contains(hearingsupport)) {
            extraSupportDetails = buildExtraSupport(supportYouNeed.getCourtHearing(), supportYouNeed, isEnglish, welshMapping);
            if (!extraSupportDetails.isEmpty()) {
                dataMap.put("extraSupportNeeded", YES);
                if (isEnglish) {
                    dataMap.put("extraSupportDetails", extraSupportDetails);
                } else {
                    dataMap.put("extraSupportDetailsWelsh", extraSupportDetails);
                }
                adjustmentRequired.append(COMMA_SEPARATOR).append(isEnglish ? hearingsupport.getDisplayedValue()
                        : welshMapping.get(hearingsupport.getDisplayedValue())).append(COLON)
                        .append(extraSupportDetails);
            }
        }
        if (reasonableAdjustmentsEnums.contains(hearingcomfort)) {
            feelComfortableSupportDetails = buildFeelComfortableSupport(
                    supportYouNeed.getCourtComfort(),
                    supportYouNeed, isEnglish, welshMapping
            );
            if (!feelComfortableSupportDetails.isEmpty()) {
                dataMap.put("feelComfortableNeeed", YES);
                if (isEnglish) {
                    dataMap.put("feelComfortableDetails", feelComfortableSupportDetails);
                } else {
                    dataMap.put("feelComfortableDetailsWelsh", feelComfortableSupportDetails);
                }
                adjustmentRequired.append(COMMA_SEPARATOR).append(isEnglish ? hearingcomfort.getDisplayedValue()
                        : welshMapping.get(hearingcomfort.getDisplayedValue())).append(COLON)
                        .append(feelComfortableSupportDetails);
            }
        }
    }

    private static String buildHelpTravellingMovingBuildingSupport(List<TravellingToCourtEnum> travellingToCourtEnums,
                                                                   ReasonableAdjustmentsSupport support, boolean isEnglish,
                                                                   Map<String, String> welshMapping) {
        return travellingToCourtEnums.stream()
                .map(element -> buildTravellingCourtElement(element,
                        support.getParkingDetails(),
                        support.getDifferentChairDetails(),
                        support.getTravellingOtherDetails(), isEnglish, welshMapping))
                .collect(Collectors.joining(COMMA_SEPARATOR));
    }

    private static String buildTravellingCourtElement(TravellingToCourtEnum element, String parkingSpaceSubField,
                                                      String differentTypeChairSubField, String travellingCourtOtherSubField,
                                                      boolean isEnglish, Map<String, String> welshMapping) {
        if (TravellingToCourtEnum.parkingspace.name().equalsIgnoreCase(element.getDisplayedValue())) {
            String parkingSpace = isEnglish ? TravellingToCourtEnum.parkingspace.getDisplayedValue()
                : welshMapping.get(TravellingToCourtEnum.parkingspace.getDisplayedValue());
            return parkingSpace + OPEN_BRACKET + parkingSpaceSubField + CLOSE_BRACKET;
        } else if (TravellingToCourtEnum.differentchair.name().equalsIgnoreCase(element.getDisplayedValue())) {
            String differentChair = isEnglish ? TravellingToCourtEnum.differentchair.getDisplayedValue()
                : welshMapping.get(TravellingToCourtEnum.differentchair.getDisplayedValue());
            return differentChair + OPEN_BRACKET + differentTypeChairSubField + CLOSE_BRACKET;
        } else if (TravellingToCourtEnum.other.name().equalsIgnoreCase(element.getDisplayedValue())) {
            String other = isEnglish ? TravellingToCourtEnum.other.getDisplayedValue()
                : welshMapping.get(TravellingToCourtEnum.other.getDisplayedValue());
            return other + OPEN_BRACKET + travellingCourtOtherSubField + CLOSE_BRACKET;
        } else {
            return isEnglish ? element.getDisplayedValue() : welshMapping.get(element.getDisplayedValue());
        }
    }

    private static String buildFeelComfortableSupport(List<CourtComfortEnum> courtComfortEnums, ReasonableAdjustmentsSupport support,
                                                      boolean isEnglish, Map<String, String> welshMapping) {
        return courtComfortEnums.stream()
                .map(element -> buildFeelComfortableElement(element,
                        support.getLightingDetails(),
                        support.getOtherProvideDetails(), isEnglish, welshMapping))
                .collect(Collectors.joining(COMMA_SEPARATOR));
    }

    private static String buildFeelComfortableElement(CourtComfortEnum element, String appropriateLightingSubField,
                                                      String feelComfortableOtherSubField, boolean isEnglish, Map<String, String> welshMapping) {
        if (CourtComfortEnum.appropriatelighting.name().equalsIgnoreCase(element.getDisplayedValue())) {
            String appropriateLight = isEnglish ? CourtComfortEnum.appropriatelighting.getDisplayedValue()
                : welshMapping.get(CourtComfortEnum.appropriatelighting.getDisplayedValue());
            return appropriateLight + OPEN_BRACKET + appropriateLightingSubField + CLOSE_BRACKET;
        } else if (CourtComfortEnum.other.name().equalsIgnoreCase(element.getDisplayedValue())) {
            String other = isEnglish ? CourtComfortEnum.other.getDisplayedValue() : welshMapping.get(CourtComfortEnum.other.getDisplayedValue());
            return other + OPEN_BRACKET + feelComfortableOtherSubField + CLOSE_BRACKET;
        } else {
            return isEnglish ? element.getDisplayedValue() : welshMapping.get(element.getDisplayedValue());
        }
    }

    private static String buildExtraSupport(List<CourtHearingEnum> courtHearingEnums, ReasonableAdjustmentsSupport support,
                                            boolean isEnglish, Map<String, String> welshMapping) {
        return courtHearingEnums.stream()
                .map(element -> buildSupportCourtElement(element,
                        support.getSupportWorkerDetails(),
                        support.getFamilyProviderDetails(),
                        support.getTherapyDetails(),
                        support.getCommunicationSupportOther(), isEnglish, welshMapping))
                .collect(Collectors.joining(COMMA_SEPARATOR));
    }

    private static String buildSupportCourtElement(CourtHearingEnum element, String supportWorkerCarerSubField,
                                                   String friendFamilyMemberSubField, String therapyAnimalSubField,
                                                   String supportCourtOtherSubField, boolean isEnglish, Map<String, String> welshMapping) {
        if (CourtHearingEnum.supportworker.name().equalsIgnoreCase(element.getDisplayedValue())) {
            String supportWorker = isEnglish ? CourtHearingEnum.supportworker.getDisplayedValue()
                : welshMapping.get(CourtHearingEnum.supportworker.getDisplayedValue());
            return supportWorker + OPEN_BRACKET + supportWorkerCarerSubField + CLOSE_BRACKET;
        } else if (CourtHearingEnum.familymember.name().equalsIgnoreCase(element.getDisplayedValue())) {
            String familyMember = isEnglish ? CourtHearingEnum.familymember.getDisplayedValue()
                : welshMapping.get(CourtHearingEnum.familymember.getDisplayedValue());
            return familyMember + OPEN_BRACKET + friendFamilyMemberSubField + CLOSE_BRACKET;
        } else if (CourtHearingEnum.animal.name().equalsIgnoreCase(element.getDisplayedValue())) {
            String animal = isEnglish ? CourtHearingEnum.animal.getDisplayedValue()
                : welshMapping.get(CourtHearingEnum.animal.getDisplayedValue());
            return animal + OPEN_BRACKET + therapyAnimalSubField + CLOSE_BRACKET;
        } else if (CourtHearingEnum.other.name().equalsIgnoreCase(element.getDisplayedValue())) {
            String other = isEnglish ? CourtHearingEnum.other.getDisplayedValue() : welshMapping.get(CourtHearingEnum.other.getDisplayedValue());
            return other + OPEN_BRACKET + supportCourtOtherSubField + CLOSE_BRACKET;
        } else {
            return isEnglish ? element.getDisplayedValue() : welshMapping.get(element.getDisplayedValue());
        }
    }

    private static String buildCommunicationHelp(List<HelpCommunicationEnum> communicationHelp, ReasonableAdjustmentsSupport
        support, boolean isEnglish, Map<String, String> welshMapping) {
        return communicationHelp.stream()
                .map(element -> buildCommunicationHelpElement(element,
                        support.getSignLanguageDetails(),
                        support.getDescribeOtherNeed(), isEnglish, welshMapping))
                .collect(Collectors.joining(COMMA_SEPARATOR));
    }

    private static String buildCommunicationHelpElement(HelpCommunicationEnum element, String signLanguageInterpreterDetails,
                                                        String communicationHelpOtherDetails, boolean isEnglish,
                                                        Map<String, String> welshMapping) {
        if (signlanguage.name().equalsIgnoreCase(element.getDisplayedValue())) {
            String signLanguage = isEnglish ? signlanguage.getDisplayedValue() : welshMapping.get(signlanguage.getDisplayedValue());
            return signLanguage + OPEN_BRACKET + signLanguageInterpreterDetails + CLOSE_BRACKET;
        } else if (HelpCommunicationEnum.other.name().equalsIgnoreCase(element.getDisplayedValue())) {
            String otherDetails = isEnglish ? HelpCommunicationEnum.other.getDisplayedValue()
                : welshMapping.get(HelpCommunicationEnum.other.getDisplayedValue());
            return otherDetails + OPEN_BRACKET + communicationHelpOtherDetails + CLOSE_BRACKET;
        } else {
            return isEnglish ? element.getDisplayedValue() : welshMapping.get(element.getDisplayedValue());
        }
    }

    private static String buildDocumentInformation(List<DocsSupportEnum> documentInformation, ReasonableAdjustmentsSupport supportYouNeed,
                                                   boolean isEnglish, Map<String, String> welshMapping) {
        return documentInformation.stream()
                .map(element -> buildDocumentInformationElement(element,
                        supportYouNeed.getDocsDetails(),
                        supportYouNeed.getLargePrintDetails(),
                        supportYouNeed.getOtherDetails(), isEnglish, welshMapping))
                .collect(Collectors.joining(COMMA_SEPARATOR));
    }

    private static String buildDocumentInformationElement(DocsSupportEnum element, String specifiedColorDocumentsDetails,
                                                          String largePrintDocumentsDetails, String otherDetails,
                                                          boolean isEnglish, Map<String, String> welshMapping) {
        if (docsprint.name().equalsIgnoreCase(element.getDisplayedValue())) {
            String docPrintDetails = isEnglish ? docsprint.getDisplayedValue() : welshMapping.get(docsprint.getDisplayedValue());
            return docPrintDetails + OPEN_BRACKET + specifiedColorDocumentsDetails + CLOSE_BRACKET;
        } else if (largeprintdocs.name().equalsIgnoreCase(element.getDisplayedValue())) {
            String largePrintDocDetails = isEnglish ? docsprint.getDisplayedValue() : welshMapping.get(largeprintdocs.getDisplayedValue());
            return largePrintDocDetails + OPEN_BRACKET + largePrintDocumentsDetails + CLOSE_BRACKET;
        } else if (other.name().equalsIgnoreCase(element.getDisplayedValue())) {
            String otherDocDetails = isEnglish ? docsprint.getDisplayedValue() : welshMapping.get(other.getDisplayedValue());
            return otherDocDetails + OPEN_BRACKET + otherDetails + CLOSE_BRACKET;
        } else {
            return isEnglish ? element.getDisplayedValue() : welshMapping.get(element.getDisplayedValue());
        }
    }

    private void populateRespondToAohDataMap(Response response, Map<String, Object> dataMap) {
        if (response.getResponseToAllegationsOfHarm() != null) {
            ResponseToAllegationsOfHarm responseToAllegationsOfHarm = response.getResponseToAllegationsOfHarm();
            dataMap.put("responseToAllegationsOfHarmYesOrNoResponse",
                    getValueForYesOrNoEnum(responseToAllegationsOfHarm.getResponseToAllegationsOfHarmYesOrNoResponse()));
            dataMap.put("respondentResponseToAllegationOfHarm",
                    responseToAllegationsOfHarm.getRespondentResponseToAllegationOfHarm());

        }
    }

    private void populateAddressMap(Element<PartyDetails> solicitorRepresentedRespondent, Map<String, Object> dataMap) {
        if (solicitorRepresentedRespondent.getValue().getSolicitorAddress().getAddressLine1() != null) {
            dataMap.put(
                    "repAddressLine1",
                    solicitorRepresentedRespondent.getValue().getSolicitorAddress().getAddressLine1()
            );
        }
        if (solicitorRepresentedRespondent.getValue().getSolicitorAddress().getAddressLine2() != null) {
            dataMap.put(
                    "repAddressLine2",
                    solicitorRepresentedRespondent.getValue().getSolicitorAddress().getAddressLine2()
            );
        }
        if (solicitorRepresentedRespondent.getValue().getSolicitorAddress().getAddressLine3() != null) {
            dataMap.put(
                    "repAddressLine3",
                    solicitorRepresentedRespondent.getValue().getSolicitorAddress().getAddressLine3()
            );
        }
        if (solicitorRepresentedRespondent.getValue().getSolicitorAddress().getPostCode() != null) {
            dataMap.put("repPostcode", solicitorRepresentedRespondent.getValue().getSolicitorAddress().getPostCode());
        }
    }

    public Map<String, Object> generateDraftDocumentsForRespondent(CallbackRequest callbackRequest, String authorisation) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);



        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        Optional<SolicitorRole> solicitorRole = getSolicitorRole(callbackRequest);
        Element<PartyDetails> solicitorRepresentedRespondent = null;
        if (solicitorRole.isPresent()) {
            solicitorRepresentedRespondent = findSolicitorRepresentedRespondents(callbackRequest, solicitorRole.get());
            if (null != solicitorRepresentedRespondent) {
                String representedRespondentName = solicitorRepresentedRespondent.getValue().getFirstName() + " "
                        + solicitorRepresentedRespondent.getValue().getLastName();

                caseDataUpdated.put(RESPONDENT_NAME_FOR_RESPONSE, representedRespondentName);
            }

        }
        Map<String, Object> dataMap = populateDataMap(callbackRequest, solicitorRepresentedRespondent, SOLICITOR);
        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        if (documentLanguage.isGenEng()) {
            Document document = documentGenService.generateSingleDocument(
                    authorisation,
                    caseData,
                    SOLICITOR_C7_DRAFT_DOCUMENT,
                    false,
                    dataMap
            );
            caseDataUpdated.put("draftC7ResponseDoc", document);
        }

        if (solicitorRepresentedRespondent != null && solicitorRepresentedRespondent.getValue().getResponse() != null
                && solicitorRepresentedRespondent.getValue().getResponse().getRespondentAllegationsOfHarmData() != null
                && Yes.equals(solicitorRepresentedRespondent.getValue().getResponse().getRespondentAllegationsOfHarmData().getRespAohYesOrNo())) {
            if (documentLanguage.isGenEng()) {
                Document documentForC1A = documentGenService.generateSingleDocument(
                        authorisation,
                        caseData,
                        SOLICITOR_C1A_DRAFT_DOCUMENT,
                        false,
                        dataMap
                );
                caseDataUpdated.put("draftC1ADoc", documentForC1A);
            }
            if (documentLanguage.isGenWelsh()) {

                dataMap.put(RESP_CHILD_ABUSES_DOCMOSIS,getChildAbuses(solicitorRepresentedRespondent));
                Document documentForC1AWelsh = documentGenService.generateSingleDocument(
                        authorisation,
                        caseData,
                        SOLICITOR_C1A_WELSH_DRAFT_DOCUMENT,
                        true,
                        dataMap
                );
                caseDataUpdated.put("draftC1ADocWelsh", documentForC1AWelsh);
            }
        }

        if (documentLanguage.isGenWelsh()) {
            Document document = documentGenService.generateSingleDocument(
                    authorisation,
                    caseData,
                    SOLICITOR_C7_DRAFT_DOCUMENT,
                    true,
                    dataMap
            );
            caseDataUpdated.put("draftC7WelshResponseDoc", document);
        }

        return caseDataUpdated;
    }

    private PartyDetails getRespondentPartyDetails(Element<PartyDetails> solicitorRepresentedRespondent) {

        if (solicitorRepresentedRespondent.getValue().getResponse().getCitizenDetails() != null) {
            CitizenDetails citizenDetails = solicitorRepresentedRespondent.getValue().getResponse().getCitizenDetails();
            PartyDetails.PartyDetailsBuilder partyDetailsBuilder = solicitorRepresentedRespondent.getValue().toBuilder();
            if (citizenDetails.getAddress() != null && citizenDetails.getAddress().getPostCode() != null) {
                partyDetailsBuilder.address(citizenDetails.getAddress());
                partyDetailsBuilder.isCurrentAddressKnown(Yes);
                boolean isEmail = Yes.equals(solicitorRepresentedRespondent.getValue().getIsAddressConfidential())
                        || (solicitorRepresentedRespondent.getValue().getResponse().getKeepDetailsPrivate().getConfidentialityList() != null
                    && solicitorRepresentedRespondent.getValue().getResponse().getKeepDetailsPrivate().getConfidentialityList()
                        .contains(ConfidentialityListEnum.address));
                partyDetailsBuilder.isAddressConfidential(isEmail ? Yes : No);

                updateRespondentContactDetailsForDoc(solicitorRepresentedRespondent, citizenDetails, partyDetailsBuilder);
            }
            return partyDetailsBuilder.build();
        }
        return solicitorRepresentedRespondent.getValue();
    }

    private static void updateRespondentContactDetailsForDoc(Element<PartyDetails> solicitorRepresentedRespondent,
                                                             CitizenDetails citizenDetails, PartyDetails.PartyDetailsBuilder partyDetailsBuilder) {
        if (citizenDetails.getContact() != null && citizenDetails.getContact().getPhoneNumber() != null) {
            partyDetailsBuilder.phoneNumber(citizenDetails.getContact().getPhoneNumber());
            boolean isPhone = Yes.equals(solicitorRepresentedRespondent.getValue().getIsPhoneNumberConfidential())
                    || (solicitorRepresentedRespondent.getValue().getResponse().getKeepDetailsPrivate().getConfidentialityList() != null
                && solicitorRepresentedRespondent.getValue().getResponse().getKeepDetailsPrivate().getConfidentialityList()
                    .contains(ConfidentialityListEnum.phoneNumber));
            partyDetailsBuilder.isPhoneNumberConfidential(isPhone ? Yes : No);

        }
    }

    private List<Map<String,Object>> getChildAbuses(Element<PartyDetails> solicitorRepresentedRespondent) {

        List<Element<RespChildAbuseBehaviour>> childAbuses = respondentAllegationOfHarmService
                .updateChildAbusesForDocmosis(solicitorRepresentedRespondent.getValue().getResponse().getRespondentAllegationsOfHarmData());

        List<Map<String,Object>> childAbusesList = new ArrayList<>();
        for (Element<RespChildAbuseBehaviour> el:childAbuses) {
            childAbusesList.add(objectMapper.convertValue(el, Map.class));
        }
        return childAbusesList;
    }

    private void getOrganisationAddress(Element<PartyDetails> respondingParty, Map<String, Object> dataMap) {
        Address address = respondingParty.getValue().getSolicitorAddress();
        String orgName = "";
        String systemAuthorisation = systemUserService.getSysUserToken();
        try {
            Organisations orgDetails = organisationService.getOrganisationDetails(
                    systemAuthorisation,
                    respondingParty.getValue()
                            .getSolicitorOrg().getOrganisationID()
            );
            if (null != orgDetails && null != orgDetails.getContactInformation()) {
                address = orgDetails.getContactInformation().get(0).toAddress();
                orgName = orgDetails.getName();
            } else if (null != respondingParty.getValue().getSendSignUpLink()) {
                orgName = respondingParty.getValue().getSendSignUpLink();
            }
        } catch (Exception e) {
            log.error("Error fetching organisation for respondent solicitor {}", e.getMessage());
        }
        dataMap.put("solicitorAddress", address);
        dataMap.put("solicitorOrg", orgName);
    }

    public SubmittedCallbackResponse submittedC7Response(CaseData caseData) {
        String courtName = null != caseData.getCourtName() ? caseData.getCourtName() : "";
        return SubmittedCallbackResponse.builder().confirmationHeader(
                RESPONSE_SUBMITTED_LABEL).confirmationBody(CONTACT_LOCAL_COURT_LABEL_WELSH.concat(courtName)
                                                               .concat(CONTACT_LOCAL_COURT_LABEL_ENG)
                                                               .concat(courtName))
            .build();
    }


    private void moveRespondentDocumentsToQuarantineTab(Map<String, Object> caseDataUpdated,
                                                        UserDetails userDetails, List<QuarantineLegalDoc> quarantineLegalDocList) {
        CaseData parsedCaseData = objectMapper.convertValue(caseDataUpdated, CaseData.class);
        String userRole = CaseUtils.getUserRole(userDetails);
        manageDocumentsService.setFlagsForWaTask(parsedCaseData, caseDataUpdated, userRole, quarantineLegalDocList.get(0));
        for (QuarantineLegalDoc eachDoc : quarantineLegalDocList) {
            manageDocumentsService.moveDocumentsToQuarantineTab(eachDoc, parsedCaseData, caseDataUpdated, userRole);
            parsedCaseData = objectMapper.convertValue(caseDataUpdated, CaseData.class);
        }
    }

    private QuarantineLegalDoc getQuarantineLegalDocuments(UserDetails userDetails, Document document,
                                                           String categoryId, String categoryName,
                                                           String partyName, String partyId) {
        String loggedInUserType = DocumentUtils.getLoggedInUserType(userDetails);
        return QuarantineLegalDoc.builder()
                .documentUploadedDate(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)))
                .categoryId(categoryId)
                .categoryName(categoryName)
                .isConfidential(Yes)
                .fileName(document.getDocumentFileName())
                .uploadedBy(userDetails.getFullName())
                .uploaderRole(loggedInUserType)
                .document(document)
                .documentParty(DocumentPartyEnum.RESPONDENT.getDisplayedValue())
                .solicitorRepresentedPartyName(partyName)
                .solicitorRepresentedPartyId(partyId)
                .build();
    }
}
