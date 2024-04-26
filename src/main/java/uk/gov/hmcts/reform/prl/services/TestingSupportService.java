package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.courtnav.mappers.FL401ApplicationMapper;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.StatementOfTruth;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.DateOfSubmission;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentDto;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.caseinitiation.CaseInitiationService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.courtnav.CourtNavCaseService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;
import uk.gov.hmcts.reform.prl.utils.ResourceLoader;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_DATA_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_DATE_AND_TIME_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_OF_SUBMISSION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL_401_STMT_OF_TRUTH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUE_DATE_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TESTING_SUPPORT_LD_FLAG_ENABLED;
import static uk.gov.hmcts.reform.prl.enums.Event.TS_SOLICITOR_APPLICATION;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TestingSupportService {

    @Autowired
    private final ObjectMapper objectMapper;
    @Autowired
    private final EventService eventPublisher;
    @Autowired
    @Qualifier("allTabsService")
    private final AllTabServiceImpl tabService;
    @Autowired
    private final UserService userService;
    @Autowired
    private final DocumentGenService dgsService;
    @Autowired
    private final CaseWorkerEmailService caseWorkerEmailService;
    @Autowired
    private final AllTabServiceImpl allTabsService;
    private final CaseService citizenCaseService;
    private final C100RespondentSolicitorService c100RespondentSolicitorService;
    private final FL401ApplicationMapper fl401ApplicationMapper;
    private final LaunchDarklyClient launchDarklyClient;
    private final AuthorisationService authorisationService;
    private final CourtNavCaseService courtNavCaseService;
    private final RequestUpdateCallbackService requestUpdateCallbackService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final SystemUserService systemUserService;
    private final PartyLevelCaseFlagsService partyLevelCaseFlagsService;
    private final CaseInitiationService caseInitiationService;
    private final TaskListService taskListService;

    private static final String VALID_C100_DRAFT_INPUT_JSON = "C100_Dummy_Draft_CaseDetails.json";

    private static final String VALID_RESPONDENT_TASKLIST_INPUT_JSON = "Dummy_Respondent_Tasklist_Data.json";

    private static final String VALID_FL401_DRAFT_INPUT_JSON = "FL401_Dummy_Draft_CaseDetails.json";

    private static final String VALID_FL401_COURTNAV_DRAFT_INPUT_JSON = "FL401_CourtNav_Draft_CaseDetails.json";

    private static final String VALID_C100_GATEKEEPING_INPUT_JSON = "C100_Dummy_Gatekeeping_CaseDetails.json";


    private static final String VALID_C100_CITIZEN_INPUT_JSON = "C100_citizen_Dummy_CaseDetails.json";

    private static final String VALID_FL401_GATEKEEPING_INPUT_JSON = "FL401_Dummy_Gatekeeping_CaseDetails.json";

    public Map<String, Object> initiateCaseCreation(String authorisation, CallbackRequest callbackRequest) throws Exception {
        if (isAuthorized(authorisation)) {
            String requestBody;
            CaseDetails initialCaseDetails = callbackRequest.getCaseDetails();
            CaseData initialCaseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            boolean adminCreateApplication = false;
            if (TS_SOLICITOR_APPLICATION.getId().equalsIgnoreCase(callbackRequest.getEventId())) {
                requestBody = loadCaseDetailsInDraftStage(initialCaseData);
            } else {
                requestBody = loadCaseDetailsInGateKeepingStage(initialCaseData);
                adminCreateApplication = true;
            }
            CaseDetails dummyCaseDetails = objectMapper.readValue(requestBody, CaseDetails.class);
            return updateCaseDetails(
                authorisation,
                initialCaseDetails,
                initialCaseData,
                adminCreateApplication,
                dummyCaseDetails
            );
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    public Map<String, Object> initiateCaseCreationForCourtNav(String authorisation, CallbackRequest callbackRequest) throws Exception {
        if (isAuthorized(authorisation)) {
            CaseDetails initialCaseDetails = callbackRequest.getCaseDetails();
            String requestBody = loadCaseDetailsInDraftStageForCourtNav();
            CourtNavFl401 dummyCaseDetails = objectMapper.readValue(requestBody, CourtNavFl401.class);
            return updateCaseDetailsForCourtNav(initialCaseDetails, dummyCaseDetails);
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    public Map<String, Object> initiateRespondentResponseCreation(String authorisation, CallbackRequest callbackRequest) throws Exception {
        if (isAuthorized(authorisation)) {

            CaseData initialCaseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            List<Element<PartyDetails>> respondents = initialCaseData.getRespondents();
            Optional<SolicitorRole> solicitorRole = c100RespondentSolicitorService.getSolicitorRole(callbackRequest);
            Element<PartyDetails> solicitorRepresentedRespondent = null;
            if (solicitorRole.isPresent()) {
                solicitorRepresentedRespondent = c100RespondentSolicitorService
                    .findSolicitorRepresentedRespondents(callbackRequest, solicitorRole.get());
            }

            String requestBody = ResourceLoader.loadJson(VALID_RESPONDENT_TASKLIST_INPUT_JSON);
            Response dummyResponse = objectMapper.readValue(requestBody, Response.class);

            String invokingSolicitor = callbackRequest.getEventId().substring(callbackRequest.getEventId().length() - 1);
            if (SolicitorRole.C100RESPONDENTSOLICITOR2.getEventId().equalsIgnoreCase(invokingSolicitor)) {
                CitizenDetails party = dummyResponse.getCitizenDetails();
                dummyResponse = dummyResponse
                    .toBuilder()
                    .citizenDetails(party.toBuilder()
                                        .firstName("Elise")
                                        .lastName("Lynn")
                                        .build())
                    .build();
            } else if (SolicitorRole.C100RESPONDENTSOLICITOR3.getEventId().equalsIgnoreCase(invokingSolicitor)) {
                CitizenDetails party = dummyResponse.getCitizenDetails();
                dummyResponse = dummyResponse
                    .toBuilder()
                    .citizenDetails(party.toBuilder()
                                        .firstName("David")
                                        .lastName("Carman")
                                        .build())
                    .build();
            }

            if (solicitorRepresentedRespondent != null) {
                PartyDetails amended = solicitorRepresentedRespondent.getValue()
                    .toBuilder().response(dummyResponse).build();
                respondents.set(
                    respondents.indexOf(solicitorRepresentedRespondent),
                    element(solicitorRepresentedRespondent
                                .getId(), amended)
                );
            }
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            caseDataUpdated.put(C100_RESPONDENTS, respondents);

            return caseDataUpdated;
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    public void respondentTaskListRequestSubmitted(CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        eventPublisher.publishEvent(new CaseDataChanged(caseData));
    }

    private Map<String, Object> updateCaseDetails(String authorisation, CaseDetails initialCaseDetails,
                                                  CaseData initialCaseData, boolean adminCreateApplication, CaseDetails dummyCaseDetails) {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        if (dummyCaseDetails != null) {
            CaseDetails updatedCaseDetails = dummyCaseDetails.toBuilder()
                .id(initialCaseDetails.getId())
                .createdDate(initialCaseDetails.getCreatedDate())
                .lastModified(initialCaseDetails.getLastModified())
                .build();
            caseDataUpdated = updatedCaseDetails.getData();
            CaseData updatedCaseData = CaseUtils.getCaseData(updatedCaseDetails, objectMapper);
            caseDataUpdated.put(CASE_DATA_ID, initialCaseDetails.getId());
            if (adminCreateApplication) {
                caseDataUpdated.putAll(updateDateInCase(initialCaseData.getCaseTypeOfApplication(), updatedCaseData));
                caseDataUpdated.putAll(partyLevelCaseFlagsService.generatePartyCaseFlags(updatedCaseData));
                try {
                    caseDataUpdated.putAll(dgsService.generateDocumentsForTestingSupport(
                        authorisation,
                        updatedCaseData
                    ));
                } catch (Exception e) {
                    log.error("Error regenerating the document", e);
                }
            }
        }
        return caseDataUpdated;
    }

    private Map<String, Object> updateCaseDetailsForCourtNav(CaseDetails initialCaseDetails,
                                                             CourtNavFl401 dummyCaseDetails) throws Exception {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        if (dummyCaseDetails != null) {
            CaseData fl401CourtNav = fl401ApplicationMapper.mapCourtNavData(dummyCaseDetails);
            String systemAuthorisation = systemUserService.getSysUserToken();
            CaseDetails caseDetails = courtNavCaseService.createCourtNavCase(
                systemAuthorisation,
                fl401CourtNav
            );
            caseDataUpdated = caseDetails.getData();
            caseDataUpdated.put(CASE_DATA_ID, initialCaseDetails.getId());
            caseDataUpdated.putAll(updateDateInCase(FL401_CASE_TYPE, fl401CourtNav));
        }

        return caseDataUpdated;
    }

    private static String loadCaseDetailsInGateKeepingStage(CaseData initialCaseData) throws Exception {
        String requestBody;
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(initialCaseData.getCaseTypeOfApplication())) {
            requestBody = ResourceLoader.loadJson(VALID_C100_GATEKEEPING_INPUT_JSON);
        } else {
            requestBody = ResourceLoader.loadJson(VALID_FL401_GATEKEEPING_INPUT_JSON);
        }
        return requestBody;
    }

    private static String loadCaseDetailsInDraftStage(CaseData initialCaseData) throws Exception {
        String requestBody;
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(initialCaseData.getCaseTypeOfApplication())) {
            requestBody = ResourceLoader.loadJson(VALID_C100_DRAFT_INPUT_JSON);
        } else {
            requestBody = ResourceLoader.loadJson(VALID_FL401_DRAFT_INPUT_JSON);
        }
        return requestBody;
    }

    private static String loadCaseDetailsInDraftStageForCourtNav() throws Exception {
        return ResourceLoader.loadJson(VALID_FL401_COURTNAV_DRAFT_INPUT_JSON);
    }


    private Map<String, Object> updateDateInCase(String caseTypeOfApplication, CaseData dummyCaseData) {
        Map<String, Object> objectMap = new HashMap<>();
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String dateSubmitted = DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime);
        objectMap.put(DATE_SUBMITTED_FIELD, dateSubmitted);
        objectMap.put(
            CASE_DATE_AND_TIME_SUBMITTED_FIELD,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime)
        );
        objectMap.put(ISSUE_DATE_FIELD, LocalDate.now());
        objectMap.put(
            DATE_OF_SUBMISSION,
            DateOfSubmission.builder().dateOfSubmission(CommonUtils.getIsoDateToSpecificFormat(
                dateSubmitted,
                CommonUtils.DATE_OF_SUBMISSION_FORMAT
            ).replace("-", " ")).build()
        );
        if (FL401_CASE_TYPE.equalsIgnoreCase(caseTypeOfApplication)
            && null != dummyCaseData.getFl401StmtOfTruth()) {
            StatementOfTruth statementOfTruth = dummyCaseData.getFl401StmtOfTruth().toBuilder().date(LocalDate.now()).build();
            objectMap.put(FL_401_STMT_OF_TRUTH, statementOfTruth);
        }
        return objectMap;
    }

    public Map<String, Object> submittedCaseCreation(CallbackRequest callbackRequest, String authorisation) {
        if (isAuthorized(authorisation)) {
            StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
                = tabService.getStartAllTabsUpdate(String.valueOf(callbackRequest.getCaseDetails().getId()));
            CaseData caseData = startAllTabsUpdateDataContent.caseData();
            Map<String, Object> caseDataUpdated = startAllTabsUpdateDataContent.caseDataMap();
            caseData = caseData.toBuilder()
                .c8Document(objectMapper.convertValue(caseDataUpdated.get(DOCUMENT_FIELD_C8), Document.class))
                .c1ADocument(objectMapper.convertValue(caseDataUpdated.get(DOCUMENT_FIELD_C1A), Document.class))
                .c8WelshDocument(objectMapper.convertValue(
                    caseDataUpdated.get(DOCUMENT_FIELD_C8_WELSH),
                    Document.class
                ))
                .finalDocument(objectMapper.convertValue(caseDataUpdated.get(DOCUMENT_FIELD_FINAL), Document.class))
                .finalWelshDocument(objectMapper.convertValue(
                    caseDataUpdated.get(DOCUMENT_FIELD_FINAL_WELSH),
                    Document.class
                ))
                .c1AWelshDocument(objectMapper.convertValue(
                    caseDataUpdated.get(DOCUMENT_FIELD_C1A_WELSH),
                    Document.class
                ))
                .build();
            tabService.mapAndSubmitAllTabsUpdate(
                startAllTabsUpdateDataContent.authorisation(),
                String.valueOf(callbackRequest.getCaseDetails().getId()),
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                caseData
            );
            Map<String, Object> allTabsFields = allTabsService.getAllTabsFields(caseData);
            caseDataUpdated.putAll(allTabsFields);
            AboutToStartOrSubmitCallbackResponse response
                = solicitorSubmittedCaseCreation(callbackRequest, authorisation);
            return response.getData();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    public AboutToStartOrSubmitCallbackResponse solicitorSubmittedCaseCreation(CallbackRequest callbackRequest, String authorisation) {
        if (isAuthorized(authorisation)) {
            try {
                caseInitiationService.handleCaseInitiation(authorisation, callbackRequest);
            } catch (Exception e) {
                log.error("Access grant failed", e);
            }
            return taskListService.updateTaskList(callbackRequest, authorisation);
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    public Map<String, Object> confirmDummyPayment(CallbackRequest callbackRequest, String authorisation) {
        if (isAuthorized(authorisation)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            requestUpdateCallbackService.processCallback(ServiceRequestUpdateDto
                                                             .builder()
                                                             .ccdCaseNumber(String.valueOf(caseData.getId()))
                                                             .payment(PaymentDto.builder().build())
                                                             .serviceRequestReference(caseData.getPaymentServiceRequestReferenceNumber())
                                                             .serviceRequestStatus("Paid")
                                                             .build());
            return coreCaseDataApi.getCase(
                authorisation,
                authTokenGenerator.generate(),
                String.valueOf(caseData.getId())
            ).getData();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    public Map<String, Object> confirmDummyAwPPayment(CallbackRequest callbackRequest, String authorisation) {
        if (isAuthorized(authorisation)) {
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
            requestUpdateCallbackService.processCallback(ServiceRequestUpdateDto
                                                             .builder()
                                                             .serviceRequestReference(caseData.getTsPaymentServiceRequestReferenceNumber())
                                                             .ccdCaseNumber(String.valueOf(caseData.getId()))
                                                             .payment(PaymentDto.builder().build())
                                                             .serviceRequestStatus(caseData.getTsPaymentStatus())
                                                             .build());
            return coreCaseDataApi.getCase(
                authorisation,
                authTokenGenerator.generate(),
                String.valueOf(caseData.getId())
            ).getData();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    public CaseData createDummyLiPC100Case(String authorisation, String s2sToken) throws Exception {
        if (isAuthorized(authorisation, s2sToken)) {
            CaseDetails dummyCaseDetails = objectMapper.readValue(
                ResourceLoader.loadJson(VALID_C100_CITIZEN_INPUT_JSON),
                CaseDetails.class
            );
            CaseDetails caseDetails = citizenCaseService.createCase(CaseUtils.getCaseData(
                dummyCaseDetails,
                objectMapper
            ), authorisation);
            CaseData createdCaseData = CaseUtils.getCaseData(caseDetails, objectMapper);
            return createdCaseData.toBuilder().noOfDaysRemainingToSubmitCase(
                CaseUtils.getRemainingDaysSubmitCase(createdCaseData)).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    private boolean isAuthorized(String authorisation, String s2sToken) {
        return launchDarklyClient.isFeatureEnabled(TESTING_SUPPORT_LD_FLAG_ENABLED)
            && Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation))
            && Boolean.TRUE.equals(authorisationService.authoriseService(s2sToken));
    }

    private boolean isAuthorized(String authorisation) {
        return launchDarklyClient.isFeatureEnabled(TESTING_SUPPORT_LD_FLAG_ENABLED)
            && Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation));
    }
}
