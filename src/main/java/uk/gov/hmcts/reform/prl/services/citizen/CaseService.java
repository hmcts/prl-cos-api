package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.config.citizen.DashboardNotificationsConfig;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaCitizenServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildApplicantDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildRespondentDetailsElements;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.caseflags.request.CitizenPartyFlagsRequest;
import uk.gov.hmcts.reform.prl.models.caseflags.request.FlagDetailRequest;
import uk.gov.hmcts.reform.prl.models.citizen.CaseDataWithHearingResponse;
import uk.gov.hmcts.reform.prl.models.citizen.NotificationNames;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetailsMeta;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.ServedParties;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AdditionalOrderDocument;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.models.dto.citizen.CitizenDocuments;
import uk.gov.hmcts.reform.prl.models.dto.citizen.CitizenDocumentsManagement;
import uk.gov.hmcts.reform.prl.models.dto.citizen.CitizenNotification;
import uk.gov.hmcts.reform.prl.models.dto.citizen.UiCitizenCaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;
import uk.gov.hmcts.reform.prl.repositories.CaseRepository;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.cafcass.HearingService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.ANY_OTHER_DOC;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_C1A_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICATIONS_FROM_OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICATIONS_WITHIN_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.FM5_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.ORDERS_FROM_OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_C1A_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_C1A_RESPONSE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.TRANSCRIPTS_OF_JUDGEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWAITING_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COMMA;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COMPLETED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DD_MMM_YYYY_HH_MM_SS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.D_MMM_YYYY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PARTY_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PARTY_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PARTY_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_CAFCASS_CYMRU;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_BY_EMAIL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_BY_EMAIL_AND_POST;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_BY_POST;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOS_COMPLETED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TEST_UUID;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_UPDATE;
import static uk.gov.hmcts.reform.prl.enums.State.DECISION_OUTCOME;
import static uk.gov.hmcts.reform.prl.enums.State.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.prl.models.dto.citizen.CitizenDocumentsManagement.otherDocumentsCategoriesForUI;
import static uk.gov.hmcts.reform.prl.models.dto.citizen.CitizenDocumentsManagement.redundantDocumentsCategories;
import static uk.gov.hmcts.reform.prl.models.dto.citizen.CitizenDocumentsManagement.unReturnedCategoriesForUI;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.PERSONAL_SERVICE_SERVED_BY_BAILIFF;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.PERSONAL_SERVICE_SERVED_BY_CA;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.UNREPRESENTED_APPLICANT;
import static uk.gov.hmcts.reform.prl.services.citizen.CitizenResponseService.ENGLISH;
import static uk.gov.hmcts.reform.prl.services.citizen.CitizenResponseService.WELSH;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getPartyDetailsMeta;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getStringsSplitByDelimiter;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseService {
    public static final String SYSTEM = "System";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final DateTimeFormatter DATE_FORMATTER_D_MMM_YYYY = DateTimeFormatter.ofPattern(D_MMM_YYYY);
    public static final DateTimeFormatter DATE_FORMATTER_YYYY_MM_DD = DateTimeFormatter.ofPattern(YYYY_MM_DD);
    public static final DateTimeFormatter DATE_TIME_FORMATTER_DD_MMM_YYYY_HH_MM_SS = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS);
    public static final String COVER_LETTER_PREFIX = "cover_letter";
    public static final String IS_NEW = "IS_NEW";
    public static final String IS_FINAL = "IS_FINAL";
    public static final String IS_MULTIPLE = "IS_MULTIPLE";
    public static final String IS_PERSONAL = "IS_PERSONAL";
    public static final String PARTY_NAMES = "PARTY_NAMES";
    private final CoreCaseDataApi coreCaseDataApi;
    private final CaseRepository caseRepository;
    private final IdamClient idamClient;
    private final ObjectMapper objectMapper;
    private final RoleAssignmentService roleAssignmentService;
    private final UserService userService;
    private final CcdCoreCaseDataService ccdCoreCaseDataService;
    private final HearingService hearingService;
    private final DashboardNotificationsConfig notificationsConfig;

    private final PartyLevelCaseFlagsService partyLevelCaseFlagsService;

    public CaseDetails updateCase(CaseData caseData, String authToken,
                                  String caseId, String eventId) throws JsonProcessingException {
        if (CITIZEN_CASE_UPDATE.getValue().equalsIgnoreCase(eventId)
            && isEmpty(caseData.getApplicantCaseName())) {
            caseData = caseData.toBuilder()
                .applicantCaseName(buildApplicantAndRespondentForCaseName(caseData))
                .build();
        }

        return caseRepository.updateCase(authToken, caseId, caseData, CaseEvent.fromValue(eventId));
    }

    public String buildApplicantAndRespondentForCaseName(CaseData caseData) throws JsonProcessingException {
        C100RebuildData c100RebuildData = caseData.getC100RebuildData();
        ObjectMapper mapper = new ObjectMapper();
        C100RebuildApplicantDetailsElements c100RebuildApplicantDetailsElements = null;
        C100RebuildRespondentDetailsElements c100RebuildRespondentDetailsElements = null;
        if (null != c100RebuildData) {
            if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildApplicantDetails())) {
                c100RebuildApplicantDetailsElements = mapper
                    .readValue(c100RebuildData.getC100RebuildApplicantDetails(), C100RebuildApplicantDetailsElements.class);
            }

            if (StringUtils.isNotEmpty(c100RebuildData.getC100RebuildRespondentDetails())) {
                c100RebuildRespondentDetailsElements = mapper
                    .readValue(c100RebuildData.getC100RebuildRespondentDetails(), C100RebuildRespondentDetailsElements.class);
            }
        }
        return buildCaseName(c100RebuildApplicantDetailsElements, c100RebuildRespondentDetailsElements);
    }


    private String buildCaseName(C100RebuildApplicantDetailsElements c100RebuildApplicantDetailsElements,
                                 C100RebuildRespondentDetailsElements c100RebuildRespondentDetailsElements) {
        String caseName = null;
        if (null != c100RebuildApplicantDetailsElements
            && null != c100RebuildRespondentDetailsElements.getRespondentDetails()) {
            caseName = c100RebuildApplicantDetailsElements.getApplicants().get(0).getApplicantLastName() + " V "
                + c100RebuildRespondentDetailsElements.getRespondentDetails().get(0).getLastName();
        }

        return caseName;
    }

    public List<CaseData> retrieveCases(String authToken, String s2sToken) {

        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("sortDirection", "desc");
        searchCriteria.put("page", "1");

        return searchCasesLinkedToUser(authToken, s2sToken, searchCriteria);
    }

    private List<CaseData> searchCasesLinkedToUser(String authToken, String s2sToken,
                                                   Map<String, String> searchCriteria) {

        UserDetails userDetails = idamClient.getUserDetails(authToken);
        List<CaseDetails> caseDetails = new ArrayList<>(performSearch(authToken, userDetails, searchCriteria, s2sToken));
        return caseDetails
            .stream()
            .map(caseDetail -> CaseUtils.getCaseData(caseDetail, objectMapper))
            .toList();
    }

    private List<CaseDetails> performSearch(String authToken, UserDetails user, Map<String, String> searchCriteria,
                                            String serviceAuthToken) {
        List<CaseDetails> result;

        result = coreCaseDataApi.searchForCitizen(
            authToken,
            serviceAuthToken,
            user.getId(),
            JURISDICTION,
            CASE_TYPE,
            searchCriteria
        );

        return result;
    }

    public CaseDetails getCase(String authToken, String caseId) {
        return caseRepository.getCase(authToken, caseId);
    }

    public CaseDetails createCase(CaseData caseData, String authToken) {
        return caseRepository.createCase(authToken, caseData);
    }

    public CaseDataWithHearingResponse getCaseWithHearing(String authorisation, String caseId, String hearingNeeded) {
        CaseDetails caseDetails = ccdCoreCaseDataService.findCaseById(authorisation, caseId);
        return getCaseDataWithHearingResponse(
            authorisation,
            hearingNeeded,
            caseDetails
        );
    }

    public CaseDataWithHearingResponse getCaseDataWithHearingResponse(String authorisation,
                                                                      String hearingNeeded,
                                                                      CaseDetails caseDetails) {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        CaseDataWithHearingResponse caseDataWithHearingResponse = CaseDataWithHearingResponse.builder()
            .caseData(UiCitizenCaseData.builder()
                          .caseData(caseData.toBuilder()
                                        .noOfDaysRemainingToSubmitCase(
                                            CaseUtils.getRemainingDaysSubmitCase(caseData))
                                        .build())
                          //This is a non-persistent view, list of citizen documents, orders & packs
                          .citizenDocumentsManagement(getAllCitizenDocumentsOrders(authorisation, caseData))
                          .build())
            .build();
        if ("Yes".equalsIgnoreCase(hearingNeeded)) {
            caseDataWithHearingResponse =
                caseDataWithHearingResponse.toBuilder().hearings(
                    hearingService.getHearings(authorisation, String
                        .valueOf(caseData.getId()))).build();
        }
        return caseDataWithHearingResponse;
    }

    public Flags getPartyCaseFlags(String authToken, String caseId, String partyId) {
        CaseDetails caseDetails = getCase(authToken, caseId);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        Optional<PartyDetailsMeta> partyDetailsMeta = getPartyDetailsMeta(
            partyId,
            caseData.getCaseTypeOfApplication(),
            caseData
        );

        if (partyDetailsMeta.isPresent()
            && partyDetailsMeta.get().getPartyDetails() != null
            && !StringUtils.isEmpty(partyDetailsMeta.get().getPartyDetails().getLabelForDynamicList())) {
            Optional<String> partyExternalCaseFlagField = getPartyExternalCaseFlagField(
                caseData.getCaseTypeOfApplication(),
                partyDetailsMeta.get().getPartyType(),
                partyDetailsMeta.get().getPartyIndex()
            );

            if (partyExternalCaseFlagField.isPresent()) {
                return objectMapper.convertValue(
                    caseDetails.getData().get(partyExternalCaseFlagField.get()),
                    Flags.class
                );
            }
        }

        return null;
    }

    public ResponseEntity<Object> updateCitizenRAflags(
        String caseId, String eventId, String authToken, CitizenPartyFlagsRequest citizenPartyFlagsRequest) {
        if (StringUtils.isEmpty(citizenPartyFlagsRequest.getPartyIdamId()) || ObjectUtils.isEmpty(
            citizenPartyFlagsRequest.getPartyExternalFlags())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("bad request");
        }

        UserDetails userDetails = idamClient.getUserDetails(authToken);
        CaseEvent caseEvent = CaseEvent.fromValue(eventId);
        EventRequestData eventRequestData = ccdCoreCaseDataService.eventRequest(
            caseEvent,
            userDetails.getId()
        );

        StartEventResponse startEventResponse =
            ccdCoreCaseDataService.startUpdate(
                authToken,
                eventRequestData,
                caseId,
                false
            );

        CaseData caseData = CaseUtils.getCaseData(startEventResponse.getCaseDetails(), objectMapper);
        Optional<PartyDetailsMeta> partyDetailsMeta = getPartyDetailsMeta(
            citizenPartyFlagsRequest.getPartyIdamId(),
            caseData.getCaseTypeOfApplication(),
            caseData
        );

        if (partyDetailsMeta.isEmpty()
            || null == partyDetailsMeta.get().getPartyDetails()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("party details not found");
        }

        Map<String, Object> updatedCaseData = startEventResponse.getCaseDetails().getData();
        Optional<String> partyExternalCaseFlagField = getPartyExternalCaseFlagField(
            caseData.getCaseTypeOfApplication(),
            partyDetailsMeta.get().getPartyType(),
            partyDetailsMeta.get().getPartyIndex()
        );

        if (partyExternalCaseFlagField.isEmpty() || !updatedCaseData.containsKey(partyExternalCaseFlagField.get()) || ObjectUtils.isEmpty(
            updatedCaseData.get(
                partyExternalCaseFlagField.get()))) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("party external flag details not found");
        }

        Flags flags = objectMapper.convertValue(
            updatedCaseData.get(partyExternalCaseFlagField.get()),
            Flags.class
        );
        flags = flags.toBuilder()
            .details(convertFlags(citizenPartyFlagsRequest.getPartyExternalFlags().getDetails()))
            .build();
        Map<String, Object> externalCaseFlagMap = new HashMap<>();
        externalCaseFlagMap.put(partyExternalCaseFlagField.get(), flags);

        CaseDataContent caseDataContent = ccdCoreCaseDataService.createCaseDataContent(
            startEventResponse,
            externalCaseFlagMap
        );

        ccdCoreCaseDataService.submitUpdate(
            authToken,
            eventRequestData,
            caseDataContent,
            caseId,
            false
        );
        return ResponseEntity.status(HttpStatus.OK).body("party flags updated");
    }

    private List<Element<FlagDetail>> convertFlags(List<Element<FlagDetailRequest>> details) {
        List<Element<FlagDetail>> flagDetails = new ArrayList<>();

        for (Element<FlagDetailRequest> detail : details) {
            FlagDetail flagDetail = FlagDetail.builder().name(detail.getValue().getName())
                .name_cy(detail.getValue().getName_cy())
                .subTypeValue(detail.getValue().getSubTypeValue())
                .subTypeValue_cy(detail.getValue().getSubTypeValue_cy())
                .subTypeKey(detail.getValue().getSubTypeKey())
                .otherDescription(detail.getValue().getOtherDescription())
                .otherDescription_cy(detail.getValue().getOtherDescription_cy())
                .flagComment(detail.getValue().getFlagComment())
                .flagComment_cy(detail.getValue().getFlagComment_cy())
                .flagUpdateComment(detail.getValue().getFlagUpdateComment())
                .dateTimeCreated(detail.getValue().getDateTimeCreated())
                .dateTimeModified(detail.getValue().getDateTimeModified())
                .path(detail.getValue().getPath())
                .hearingRelevant(detail.getValue().getHearingRelevant())
                .flagCode(detail.getValue().getFlagCode())
                .status(detail.getValue().getStatus())
                .availableExternally(detail.getValue().getAvailableExternally())
                .build();

            if (null != detail.getId()) {
                flagDetails.add(element(detail.getId(), flagDetail));
            } else {
                flagDetails.add(element(flagDetail));
            }
        }

        return flagDetails;
    }

    public Optional<String> getPartyExternalCaseFlagField(String caseType, PartyEnum partyType, Integer partyIndex) {

        Optional<String> partyExternalCaseFlagField = Optional.empty();
        boolean isC100Case = C100_CASE_TYPE.equalsIgnoreCase(caseType);

        if (PartyEnum.applicant == partyType) {
            partyExternalCaseFlagField
                = Optional.ofNullable(partyLevelCaseFlagsService.getPartyCaseDataExternalField(
                caseType,
                isC100Case ? PartyRole.Representing.CAAPPLICANT : PartyRole.Representing.DAAPPLICANT,
                partyIndex
            ));
        } else if (PartyEnum.respondent == partyType) {
            partyExternalCaseFlagField
                = Optional.ofNullable(partyLevelCaseFlagsService.getPartyCaseDataExternalField(
                caseType,
                isC100Case ? PartyRole.Representing.CARESPONDENT : PartyRole.Representing.DARESPONDENT,
                partyIndex
            ));
        }

        return partyExternalCaseFlagField;
    }

    public Map<String, String> fetchIdamAmRoles(String authorisation, String emailId) {
        return roleAssignmentService.fetchIdamAmRoles(authorisation, emailId);
    }

    public CitizenDocumentsManagement getAllCitizenDocumentsOrders(String authToken,
                                                                   CaseData caseData) {
        UserDetails userDetails = userService.getUserDetails(authToken);
        Map<String, String> partyIdAndType = findPartyIdAndType(caseData, userDetails);

        List<CitizenDocuments> otherDocuments = new ArrayList<>();
        //Retrieve citizen documents with other docs segregated
        List<CitizenDocuments> citizenDocuments = getCitizenDocuments(userDetails, caseData, otherDocuments);
        //Retrieve citizen orders for the party
        List<CitizenDocuments> citizenOrders = getCitizenOrders(userDetails, caseData, partyIdAndType, citizenDocuments);
        //Add additional documents served along with order
        addOrderAdditionalDocumentsToOtherDocuments(caseData, citizenOrders, otherDocuments);

        CitizenDocumentsManagement citizenDocumentsManagement = getCitizenPartySpecificDocuments(citizenDocuments, otherDocuments);

        citizenDocumentsManagement = citizenDocumentsManagement.toBuilder()
            .citizenOrders(citizenOrders)
            .citizenApplicationPacks(getCitizenApplicationPacks(caseData, partyIdAndType))
            .build();

        //Citizen dashboard notification enable/disable flags
        List<CitizenNotification> citizenNotifications =
            getAllCitizenDashboardNotifications(authToken, caseData, citizenDocumentsManagement, userDetails, partyIdAndType);
        if (CollectionUtils.isNotEmpty(citizenNotifications)) {
            citizenDocumentsManagement = citizenDocumentsManagement.toBuilder()
                .citizenNotifications(citizenNotifications)
                .build();
        }

        return citizenDocumentsManagement;
    }

    private List<CitizenDocuments> getCitizenApplicationPacks(CaseData caseData,
                                                              Map<String, String> partyIdAndType) {
        List<CitizenDocuments> citizenDocuments = new ArrayList<>();

        if (PREPARE_FOR_HEARING_CONDUCT_HEARING.equals(caseData.getState())
            || DECISION_OUTCOME.equals(caseData.getState())) {

            if (!partyIdAndType.isEmpty()) {
                citizenDocuments.addAll(fetchSoaPacksForParty(caseData, partyIdAndType));
            }
            return citizenDocuments;
        }
        return Collections.emptyList();
    }

    private List<CitizenDocuments> fetchSoaPacksForParty(CaseData caseData,
                                                         Map<String, String> partyIdAndType) {
        final List<CitizenDocuments>[] citizenDocuments = new List[]{new ArrayList<>()};

        nullSafeCollection(caseData.getFinalServedApplicationDetailsList()).stream()
            .map(Element::getValue)
            .sorted(comparing(ServedApplicationDetails::getServedAt).reversed())
            .forEach(servedApplicationDetails -> {
                if (CollectionUtils.isEmpty(citizenDocuments[0])
                    && servedApplicationDetails.getModeOfService() != null) {
                    switch (servedApplicationDetails.getModeOfService()) {
                        case SOA_BY_EMAIL_AND_POST -> {
                            CitizenDocuments emailSoaPack = retrieveApplicationPackFromEmailNotifications(
                                servedApplicationDetails,
                                caseData.getServiceOfApplication(),
                                partyIdAndType
                            );
                            addSoaPacksToCitizenDocuments(citizenDocuments[0], servedApplicationDetails, emailSoaPack);

                            CitizenDocuments postSoaPack = retreiveApplicationPackFromBulkPrintDetails(
                                servedApplicationDetails,
                                caseData.getServiceOfApplication(),
                                partyIdAndType
                            );
                            addSoaPacksToCitizenDocuments(citizenDocuments[0], servedApplicationDetails, postSoaPack);
                        }
                        case SOA_BY_EMAIL -> {
                            CitizenDocuments emailSoaPack = retrieveApplicationPackFromEmailNotifications(
                                servedApplicationDetails,
                                caseData.getServiceOfApplication(),
                                partyIdAndType
                            );
                            addSoaPacksToCitizenDocuments(citizenDocuments[0], servedApplicationDetails, emailSoaPack);
                        }
                        case SOA_BY_POST -> {
                            CitizenDocuments postSoaPack = retreiveApplicationPackFromBulkPrintDetails(
                                servedApplicationDetails,
                                caseData.getServiceOfApplication(),
                                partyIdAndType
                            );
                            addSoaPacksToCitizenDocuments(citizenDocuments[0], servedApplicationDetails, postSoaPack);
                        }

                        default -> citizenDocuments[0] = null;
                    }
                }
            });
        return citizenDocuments[0];
    }

    private void addSoaPacksToCitizenDocuments(List<CitizenDocuments> citizenDocuments,
                                               ServedApplicationDetails servedApplicationDetails,
                                               CitizenDocuments citizenSoaPack) {
        if (null != citizenSoaPack) {
            //Add personal service & who is responsible to serve
            citizenSoaPack = citizenSoaPack.toBuilder()
                .whoIsResponsible(servedApplicationDetails.getWhoIsResponsible())
                .isPersonalService(isPersonalService(servedApplicationDetails))
                .build();
            citizenDocuments.add(citizenSoaPack);
        }
    }

    private CitizenDocuments retrieveApplicationPackFromEmailNotifications(
        ServedApplicationDetails servedApplicationDetails,
        ServiceOfApplication serviceOfApplication,
        Map<String, String> partyIdAndType) {
        final CitizenDocuments[] citizenDocuments = {null};

        nullSafeCollection(servedApplicationDetails.getEmailNotificationDetails()).stream()
            .map(Element::getValue)
            .sorted(comparing(EmailNotificationDetails::getTimeStamp).reversed())
            .filter(emailNotificationDetails -> getStringsSplitByDelimiter(emailNotificationDetails.getPartyIds(), COMMA)
                .contains(partyIdAndType.get(PARTY_ID)))
            .findFirst()
            .ifPresent(
                emailNotificationDetails -> citizenDocuments[0] = CitizenDocuments.builder()
                    .partyId(partyIdAndType.get(PARTY_ID))
                    .servedParty(emailNotificationDetails.getServedParty())
                    .uploadedDate(LocalDateTime.parse(
                        emailNotificationDetails.getTimeStamp(),
                        DATE_TIME_FORMATTER_DD_MMM_YYYY_HH_MM_SS
                    ))
                    .applicantSoaPack(
                        SERVED_PARTY_APPLICANT.equals(partyIdAndType.get(PARTY_TYPE))
                            ? emailNotificationDetails.getDocs().stream()
                            .map(Element::getValue)
                            .filter(document -> !document.getDocumentFileName().contains(COVER_LETTER_PREFIX))
                            .toList() : null
                    )
                    .respondentSoaPack(
                        SERVED_PARTY_RESPONDENT.equals(partyIdAndType.get(PARTY_TYPE))
                            ? emailNotificationDetails.getDocs().stream()
                            .map(Element::getValue)
                            .filter(document -> !document.getDocumentFileName().contains(COVER_LETTER_PREFIX))
                                .toList()
                            : getUnservedRespondentDocumentList(serviceOfApplication, servedApplicationDetails)
                    )
                    .wasCafcassServed(isCafcassOrCafcassCymruServed(servedApplicationDetails.getEmailNotificationDetails(),
                                                                    SERVED_PARTY_CAFCASS))
                    .wasCafcassCymruServed(isCafcassOrCafcassCymruServed(servedApplicationDetails.getEmailNotificationDetails(),
                                                                         SERVED_PARTY_CAFCASS_CYMRU))
                    .build()
        );
        return citizenDocuments[0];
    }

    private static List<Document> getUnservedRespondentDocumentList(ServiceOfApplication serviceOfApplication,
                                                                    ServedApplicationDetails servedApplicationDetails) {
        //populate respondent unserved packs only in case of personal service by unrepresented lip
        return UNREPRESENTED_APPLICANT.equals(servedApplicationDetails.getWhoIsResponsible())
            && null != serviceOfApplication.getUnservedCitizenRespondentPack()
            && CollectionUtils.isNotEmpty(serviceOfApplication.getUnservedCitizenRespondentPack().getPackDocument())
            ? serviceOfApplication.getUnservedCitizenRespondentPack()
            .getPackDocument().stream()
            .map(Element::getValue)
            .toList() : null;
    }


    private CitizenDocuments retreiveApplicationPackFromBulkPrintDetails(
        ServedApplicationDetails servedApplicationDetails,
        ServiceOfApplication serviceOfApplication,
        Map<String, String> partyIdAndType) {

        final CitizenDocuments[] citizenDocuments = {null};

        nullSafeCollection(servedApplicationDetails.getBulkPrintDetails()).stream()
            .map(Element::getValue)
            .sorted(comparing(BulkPrintDetails::getTimeStamp).reversed())
            .filter(bulkPrintDetails -> getStringsSplitByDelimiter(bulkPrintDetails.getPartyIds(), COMMA)
                .contains(partyIdAndType.get(PARTY_ID)))
            .findFirst()
            .ifPresent(
                bulkPrintDetails -> citizenDocuments[0] = CitizenDocuments.builder()
                    .partyId(partyIdAndType.get(PARTY_ID))
                    .servedParty(bulkPrintDetails.getServedParty())
                    .uploadedDate(LocalDateTime.parse(
                        bulkPrintDetails.getTimeStamp(),
                        DATE_TIME_FORMATTER_DD_MMM_YYYY_HH_MM_SS
                    ))
                    .applicantSoaPack(
                        SERVED_PARTY_APPLICANT.equals(partyIdAndType.get(PARTY_TYPE))
                            ? bulkPrintDetails.getPrintDocs().stream()
                            .map(Element::getValue)
                            .filter(document -> !document.getDocumentFileName().contains(COVER_LETTER_PREFIX))
                            .toList() : null
                    )
                    .respondentSoaPack(
                        SERVED_PARTY_RESPONDENT.equals(partyIdAndType.get(PARTY_TYPE))
                            ? bulkPrintDetails.getPrintDocs().stream()
                            .map(Element::getValue)
                            .filter(document -> !document.getDocumentFileName().contains(COVER_LETTER_PREFIX))
                                .toList()
                            : getUnservedRespondentDocumentList(serviceOfApplication, servedApplicationDetails)
                    )
                    .build()
        );
        return citizenDocuments[0];
    }

    private CitizenDocumentsManagement getCitizenPartySpecificDocuments(List<CitizenDocuments> citizenDocuments,
                                                                        List<CitizenDocuments> otherDocuments) {
        List<CitizenDocuments> applicantDocuments = new ArrayList<>();
        List<CitizenDocuments> respondentDocuments = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(citizenDocuments)) {
            citizenDocuments.stream()
                .filter(citizenDoc -> !redundantDocumentsCategories.contains(citizenDoc.getCategoryId()))
                .forEach(citizenDoc -> {
                    //Other documents categories
                    if (otherDocumentsCategoriesForUI.contains(citizenDoc.getCategoryId())) {
                        otherDocuments.add(citizenDoc);
                    } else if (SERVED_PARTY_APPLICANT.equalsIgnoreCase(citizenDoc.getPartyType())) {
                        applicantDocuments.add(citizenDoc);
                    } else if (SERVED_PARTY_RESPONDENT.equalsIgnoreCase(citizenDoc.getPartyType())) {
                        respondentDocuments.add(citizenDoc);
                    } else {
                        //if not applicant/respondent add to other docs
                        otherDocuments.add(citizenDoc);
                    }
                });
        }

        //Sort documents based on uploaded date
        applicantDocuments.sort(comparing(CitizenDocuments::getUploadedDate).reversed());
        respondentDocuments.sort(comparing(CitizenDocuments::getUploadedDate).reversed());

        return CitizenDocumentsManagement.builder()
            .citizenDocuments(citizenDocuments)
            .applicantDocuments(applicantDocuments)
            .respondentDocuments(respondentDocuments)
            .citizenOtherDocuments(otherDocuments.stream()
                                .filter(citDoc -> !unReturnedCategoriesForUI.contains(citDoc.getCategoryId()))
                                .sorted(comparing(CitizenDocuments::getUploadedDate).reversed())
                                .toList())
            .build();
    }

    private List<CitizenDocuments> getCitizenDocuments(UserDetails userDetails,
                                                       CaseData caseData,
                                                       List<CitizenDocuments> otherDocuments) {
        List<CitizenDocuments> citizenDocuments = new ArrayList<>();

        if (null != caseData.getReviewDocuments()) {
            //add solicitor uploaded docs
            citizenDocuments.addAll(addCitizenDocuments(caseData.getReviewDocuments().getLegalProfUploadDocListDocTab()));
            //add cafacss uploaded docs
            otherDocuments.addAll(addCitizenDocuments(caseData.getReviewDocuments().getCafcassUploadDocListDocTab()));
            //add court staff uploaded docs
            citizenDocuments.addAll(addCitizenDocuments(caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab()));
            //add citizen uploaded docs
            citizenDocuments.addAll(addCitizenDocuments(caseData.getReviewDocuments().getCitizenUploadedDocListDocTab()));
            //add bulk scan uploaded docs
            otherDocuments.addAll(addCitizenDocuments(caseData.getReviewDocuments().getBulkScannedDocListDocTab()));

            citizenDocuments = citizenDocuments.stream().filter(citizenDocuments1 -> !unReturnedCategoriesForUI.contains(
                    citizenDocuments1.getCategoryId()))
                .collect(Collectors.toList());

            //confidential docs uploaded by citizen
            citizenDocuments.addAll(filterCitizenUploadedDocuments(
                caseData.getReviewDocuments().getConfidentialDocuments(),
                userDetails
            ));
            //restricted docs uploaded by citizen
            citizenDocuments.addAll(filterCitizenUploadedDocuments(
                caseData.getReviewDocuments().getRestrictedDocuments(),
                userDetails
            ));
        }

        //add citizen uploaded docs pending review
        if (null != caseData.getDocumentManagementDetails()) {
            citizenDocuments.addAll(filterCitizenUploadedDocuments(
                caseData.getDocumentManagementDetails().getCitizenQuarantineDocsList(),
                userDetails
            ));
        }

        //C100 & FL401 application final document
        citizenDocuments.addAll(getAllSystemGeneratedDocuments(caseData));

        //Applicant & respondent orders from previous proceedings documents
        citizenDocuments.addAll(getAllOrdersFromPreviousProceedings(caseData));

        //Applications within proceedings
        otherDocuments.addAll(getAllApplicationWithinProceedingsDocuments(caseData));

        //C9/FL415 - Statement of service documents
        otherDocuments.addAll(getAllStatementOfServiceDocuments(caseData));

        return citizenDocuments;
    }

    private List<CitizenDocuments> filterCitizenUploadedDocuments(List<Element<QuarantineLegalDoc>> quarantineDocsList,
                                                                  UserDetails userDetails) {
        return nullSafeCollection(quarantineDocsList).stream()
            .map(Element::getValue)
            .filter(qDoc -> CITIZEN.equalsIgnoreCase(qDoc.getUploaderRole()))
            .filter(qDoc -> null != userDetails
                && userDetails.getId().equalsIgnoreCase(qDoc.getUploadedByIdamId()))
            .map(this::createCitizenDocument)
            .toList();
    }

    private List<CitizenDocuments> addCitizenDocuments(List<Element<QuarantineLegalDoc>> quarantineDocsList) {
        return nullSafeCollection(quarantineDocsList).stream()
                                      .map(Element::getValue)
                                      .map(this::createCitizenDocument)
                                      .toList();
    }

    private CitizenDocuments createCitizenDocument(QuarantineLegalDoc quarantineDoc) {
        Document existingDocument;
        // If the quarantine doc is from Quarantine List then send the citizen document object
        if (quarantineDoc.getCitizenQuarantineDocument() != null && quarantineDoc.getCitizenQuarantineDocument().getDocumentUrl() != null) {
            existingDocument = quarantineDoc.getCitizenQuarantineDocument();
        } else {
            String attributeName = DocumentUtils.populateAttributeNameFromCategoryId(
                quarantineDoc.getCategoryId(),
                null
            );
            existingDocument = objectMapper.convertValue(
                objectMapper.convertValue(quarantineDoc, Map.class).get(attributeName),
                Document.class
            );
        }
        return CitizenDocuments.builder()
            .partyId(quarantineDoc.getUploadedByIdamId())//BETTER TO HAVE SEPARATE FIELD FOR IDAMID
            .partyType(quarantineDoc.getDocumentParty())
            .partyName(quarantineDoc.getSolicitorRepresentedPartyName() != null
                           ? quarantineDoc.getSolicitorRepresentedPartyName()
                           : quarantineDoc.getUploadedBy())//CAN NOT BE UPLOADEDBY, SHOULD BE PARTY NAME
            .categoryId(quarantineDoc.getCategoryId())
            .uploadedBy(quarantineDoc.getUploadedBy())
            .uploadedDate(quarantineDoc.getDocumentUploadedDate())
            .document(existingDocument)
            .documentLanguage(quarantineDoc.getDocumentLanguage())
            .createdDate(quarantineDoc.getDocumentUploadedDate().toLocalDate())
            .build();
    }

    private List<CitizenDocuments> getCitizenOrders(UserDetails userDetails,
                                                    CaseData caseData,
                                                    Map<String, String> partyIdAndType,
                                                    List<CitizenDocuments> citizenDocuments) {
        List<CitizenDocuments> citizenOrders = new ArrayList<>();

        if (!partyIdAndType.isEmpty()) {
            citizenOrders.addAll(getCitizenOrdersForParty(caseData, partyIdAndType, userDetails.getId()));
        }

        //Transcripts of judgements
        citizenOrders.addAll(citizenDocuments.stream()
                                 .filter(citDoc -> TRANSCRIPTS_OF_JUDGEMENTS.equals(citDoc.getCategoryId()))
                                 .toList());

        return citizenOrders;
    }

    private List<CitizenDocuments> getCitizenOrdersForParty(CaseData caseData,
                                                            Map<String, String> partyIdAndType,
                                                            String idamId) {
        return nullSafeCollection(caseData.getOrderCollection()).stream()
            .map(Element::getValue)
            .filter(order -> isOrderServedForParty(order, partyIdAndType.get(PARTY_ID)))
            .map(order -> createCitizenOrder(order, idamId, partyIdAndType))
            .toList();
    }

    private CitizenDocuments createCitizenOrder(OrderDetails order,
                                                String idamId,
                                                Map<String, String> partyIdAndType) {
        return CitizenDocuments.builder()
            .partyId(idamId)
            .partyType(partyIdAndType.get(PARTY_TYPE))
            .partyName(partyIdAndType.get(PARTY_NAME))
            .orderType(order.getOrderTypeId())
            .uploadedBy(order.getOtherDetails().getCreatedBy())
            .createdDate(getOrderCreatedDate(order))
            .madeDate(getOrderMadeDate(order))
            .servedDateTime(getServedDateTime(order, partyIdAndType.get(PARTY_ID)))
            .document(order.getOrderDocument())
            .documentWelsh(order.getOrderDocumentWelsh())
            .isNew(!isFinalOrder(order))
            .isFinal(isFinalOrder(order))
            .wasCafcassServed(isCafcassServed(order))
            .wasCafcassCymruServed(isCafcassCymruServed(order))
            .isPersonalService(isPersonalService(order))
            .whoIsResponsible(null != order.getServeOrderDetails()
                                  ? order.getServeOrderDetails().getWhoIsResponsibleToServe() : null)
            .isMultiple(null != order.getServeOrderDetails()
                            && YesOrNo.Yes.equals(order.getServeOrderDetails().getMultipleOrdersServed()))
            .isSosCompleted(SOS_COMPLETED.equals(order.getSosStatus()))
            .build();
    }

    private boolean isPersonalService(OrderDetails order) {
        return null != order.getServeOrderDetails()
            && YesOrNo.Yes.equals(order.getServeOrderDetails().getServeOnRespondent())
            && SoaCitizenServingRespondentsEnum.unrepresentedApplicant.getId()
            .equals(order.getServeOrderDetails().getWhoIsResponsibleToServe());
    }

    private boolean isPersonalService(ServedApplicationDetails servedAppPack) {
        return UNREPRESENTED_APPLICANT.equals(servedAppPack.getWhoIsResponsible())
            || PERSONAL_SERVICE_SERVED_BY_CA.equals(servedAppPack.getWhoIsResponsible())
            || PERSONAL_SERVICE_SERVED_BY_BAILIFF.equals(servedAppPack.getWhoIsResponsible());
    }

    private LocalDate getOrderCreatedDate(OrderDetails order) {
        if (null != order.getOtherDetails()
            && null != order.getOtherDetails().getOrderCreatedDate()) {
            return LocalDate.parse(
                order.getOtherDetails().getOrderCreatedDate(),
                DATE_FORMATTER_D_MMM_YYYY
            );
        }
        return null;
    }

    private LocalDate getOrderMadeDate(OrderDetails order) {
        if (null != order.getOtherDetails()
            && null != order.getOtherDetails().getOrderMadeDate()) {
            return LocalDate.parse(
                order.getOtherDetails().getOrderMadeDate(),
                DATE_FORMATTER_D_MMM_YYYY
            );
        } else if (null != order.getDateCreated()) {
            //If order made date is not available then fallback to order created date.
            return order.getDateCreated().toLocalDate();
        }
        return null;
    }

    private LocalDateTime getServedDateTime(OrderDetails order,
                                            String partyId) {

        return nullSafeCollection(order.getServeOrderDetails().getServedParties())
            .stream()
            .map(Element::getValue)
            .filter(servedParty -> servedParty.getPartyId().equalsIgnoreCase(partyId))
            .min(comparing(
                ServedParties::getServedDateTime,
                Comparator.nullsLast(Comparator.reverseOrder())
            )).map(ServedParties::getServedDateTime)
            .orElse(null);
    }

    private boolean isCafcassServed(OrderDetails order) {
        return null != order.getServeOrderDetails()
            && (YesOrNo.Yes.equals(order.getServeOrderDetails().getCafcassServed()));
    }

    private boolean isCafcassCymruServed(OrderDetails order) {
        return null != order.getServeOrderDetails()
            && YesOrNo.Yes.equals(order.getServeOrderDetails().getCafcassCymruServed());
    }

    private boolean isCafcassOrCafcassCymruServed(List<Element<EmailNotificationDetails>> emailNotificationDetailsList,
                                                  String cafcassOrCafcassCymru) {
        return nullSafeCollection(emailNotificationDetailsList).stream()
            .map(Element::getValue)
            .anyMatch(emailNotificationDetails ->
                          cafcassOrCafcassCymru.equalsIgnoreCase(emailNotificationDetails.getServedParty()));
    }

    private boolean isFinalOrder(OrderDetails order) {
        return StringUtils.equals(
            SelectTypeOfOrderEnum.finl.getDisplayedValue(),
            order.getTypeOfOrder());
    }

    private boolean isOrderServedForParty(OrderDetails order,
                                          String partyId) {
        return nullSafeCollection(order.getServeOrderDetails().getServedParties()).stream()
            .map(Element::getValue)
            .anyMatch(servedParty -> servedParty.getPartyId().equalsIgnoreCase(partyId));
    }

    private Map<String, String> findPartyIdAndType(CaseData caseData,
                                       UserDetails userDetails) {
        Map<String, String> partyIdAndTypeMap = new HashMap<>();
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            Optional<Element<PartyDetails>> applicantOptional = getParty(caseData.getApplicants(), userDetails);
            if (applicantOptional.isPresent()) {
                partyIdAndTypeMap.put(PARTY_ID, String.valueOf(applicantOptional.get().getId()));
                partyIdAndTypeMap.put(PARTY_TYPE, SERVED_PARTY_APPLICANT);
                partyIdAndTypeMap.put(PARTY_NAME, applicantOptional.get().getValue().getLabelForDynamicList());
                return partyIdAndTypeMap;
            }

            Optional<Element<PartyDetails>> respondentOptional = getParty(caseData.getRespondents(), userDetails);
            if (respondentOptional.isPresent()) {
                partyIdAndTypeMap.put(PARTY_ID, String.valueOf(respondentOptional.get().getId()));
                partyIdAndTypeMap.put(PARTY_TYPE, SERVED_PARTY_RESPONDENT);
                partyIdAndTypeMap.put(PARTY_NAME, respondentOptional.get().getValue().getLabelForDynamicList());
                return partyIdAndTypeMap;
            }

        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            if (null != caseData.getApplicantsFL401().getUser()
                && userDetails.getId().equalsIgnoreCase(caseData.getApplicantsFL401().getUser().getIdamId())) {
                partyIdAndTypeMap.put(PARTY_ID, String.valueOf(caseData.getApplicantsFL401().getPartyId()));
                partyIdAndTypeMap.put(PARTY_TYPE, SERVED_PARTY_APPLICANT);
                partyIdAndTypeMap.put(PARTY_NAME, caseData.getApplicantsFL401().getLabelForDynamicList());
                return partyIdAndTypeMap;
            }
            if (null != caseData.getRespondentsFL401().getUser()
                && userDetails.getId().equalsIgnoreCase(caseData.getRespondentsFL401().getUser().getIdamId())) {
                partyIdAndTypeMap.put(PARTY_ID, String.valueOf(caseData.getRespondentsFL401().getPartyId()));
                partyIdAndTypeMap.put(PARTY_TYPE, SERVED_PARTY_RESPONDENT);
                partyIdAndTypeMap.put(PARTY_NAME, caseData.getRespondentsFL401().getLabelForDynamicList());
                return partyIdAndTypeMap;
            }
        }

        return partyIdAndTypeMap;
    }

    private Optional<Element<PartyDetails>> getParty(List<Element<PartyDetails>> parties,
                                                       UserDetails userDetails) {
        return nullSafeCollection(parties).stream()
            .filter(element -> null != element.getValue().getUser()
                && userDetails.getId().equalsIgnoreCase(element.getValue().getUser().getIdamId()))
            .findFirst();
    }

    private List<CitizenNotification> getAllCitizenDashboardNotifications(String authorization,
                                                                          CaseData caseData,
                                                                          CitizenDocumentsManagement citizenDocumentsManagement,
                                                                          UserDetails userDetails,
                                                                          Map<String, String> partyIdAndType) {
        List<CitizenNotification> citizenNotifications = new ArrayList<>();

        //PRL-5565 - FM5 notification
        addFm5ReminderNotification(authorization,
                                   caseData,
                                   citizenDocumentsManagement.getCitizenDocuments(),
                                   userDetails,
                                   citizenNotifications);

        //PRL-5688 - Orders notifications
        if (CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenOrders())) {
            addOrderNotifications(caseData, citizenDocumentsManagement.getCitizenOrders(), citizenNotifications);
        }

        //PRL-5431 - SOA & SOS notifications
        if (CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenApplicationPacks())) {
            //SOA applicant/respondent notifications
            addSoaNotifications(caseData, citizenDocumentsManagement, citizenNotifications, userDetails, partyIdAndType);

            //SOS notifications
            addSosNotifications(caseData, citizenDocumentsManagement, citizenNotifications, partyIdAndType.get(PARTY_TYPE));
        }

        //Respondent response notification to applicant
        addRespondentResponseNotification(caseData, citizenDocumentsManagement,  citizenNotifications, partyIdAndType.get(PARTY_TYPE));

        return citizenNotifications;
    }

    private void addOrderNotifications(CaseData caseData,
                                       List<CitizenDocuments> citizenOrders,
                                       List<CitizenNotification> citizenNotifications) {
        List<CitizenDocuments> multipleOrdersServed = getMultipleOrdersServed(citizenOrders);
        Map<String, Object> notifMap = new HashMap<>();
        notifMap.put(IS_NEW, multipleOrdersServed.stream().anyMatch(CitizenDocuments::isNew));
        notifMap.put(IS_FINAL, multipleOrdersServed.stream().anyMatch(CitizenDocuments::isFinal));
        notifMap.put(IS_MULTIPLE, multipleOrdersServed.size() > 1);

        if (citizenOrders.get(0).isPersonalService()) {
            //personal service by unrepresented applicant lip
            notifMap.put(IS_PERSONAL, true);
            if (citizenOrders.get(0).isSosCompleted()) {
                //CA - CAN3, DA - DN6
                citizenNotifications.addAll(getNotifications(caseData, NotificationNames.ORDER_SOS_CA_CB_APPLICANT, notifMap));
            } else {
                //CRNF3
                citizenNotifications.addAll(getNotifications(caseData, NotificationNames.ORDER_PERSONAL_APPLICANT, notifMap));
            }
        } else {
            //personal service by Court admin/bailiff & non-personal service
            notifMap.put(IS_PERSONAL, false);
            if (citizenOrders.get(0).isSosCompleted()) {
                //CA - CAN3, DA - DN6
                citizenNotifications.addAll(getNotifications(caseData, NotificationNames.ORDER_SOS_CA_CB_APPLICANT, notifMap));
            } else {
                //CRNF2
                citizenNotifications.addAll(getNotifications(caseData, NotificationNames.ORDER_APPLICANT_RESPONDENT, notifMap));
            }
        }
        //Any order where SOS is not completed
        if (isAnyOrderPersonalServicePendingSos(citizenOrders)) {
            notifMap.put(IS_PERSONAL, true);
            citizenNotifications.addAll(getNotifications(caseData, NotificationNames.ORDER_PERSONAL_APPLICANT, notifMap));
        }
    }

    private boolean isAnyOrderPersonalServicePendingSos(List<CitizenDocuments> citizenOrders) {
        return citizenOrders.stream()
            .skip(1) //skip first order as it's already handled
            .anyMatch(order -> order.isPersonalService() && !order.isSosCompleted());
    }

    private List<CitizenDocuments> getMultipleOrdersServed(List<CitizenDocuments> citizenOrders) {
        List<CitizenDocuments> multipleOrdersServed = new ArrayList<>();
        multipleOrdersServed.add(citizenOrders.get(0));

        if (citizenOrders.get(0).isMultiple()) {
            for (int i = 1; i < citizenOrders.size(); i++) {
                if (null != citizenOrders.get(0).getServedDateTime()
                    && null != citizenOrders.get(i).getServedDateTime()
                    && citizenOrders.get(0).getServedDateTime().toLocalDate()
                    .equals(citizenOrders.get(i).getServedDateTime().toLocalDate())) {
                    multipleOrdersServed.add(citizenOrders.get(i));
                }
            }
        }

        return multipleOrdersServed;
    }

    private void addFm5ReminderNotification(String authorization,
                                            CaseData caseData,
                                            List<CitizenDocuments> citizenDocuments,
                                            UserDetails userDetails,
                                            List<CitizenNotification> citizenNotifications) {
        if (null != caseData.getFm5ReminderNotificationDetails()
            && "YES".equalsIgnoreCase(caseData.getFm5ReminderNotificationDetails().getFm5RemindersSent())
            && !isFm5UploadedByParty(citizenDocuments, userDetails)
            && !isFirstHearingCompleted(authorization, String.valueOf(caseData.getId()))) {
            //CAN10
            citizenNotifications.addAll(getNotifications(caseData, NotificationNames.FM5_REMINDER_APPLICANT_RESPONDENT, null));
        }
    }

    private void addSoaNotifications(CaseData caseData,
                                     CitizenDocumentsManagement citizenDocumentsManagement,
                                     List<CitizenNotification> citizenNotifications,
                                     UserDetails userDetails,
                                     Map<String, String> partyIdAndType) {
        CitizenDocuments citizenAppPack = citizenDocumentsManagement.getCitizenApplicationPacks().get(0);
        //SOA Applicant - personal service(unrepresented applicant) pending sos.
        if (CollectionUtils.isNotEmpty(citizenAppPack.getApplicantSoaPack())
            && SERVED_PARTY_APPLICANT.equals(partyIdAndType.get(PARTY_TYPE)) //logged in party is applicant
            && UNREPRESENTED_APPLICANT.equals(citizenAppPack.getWhoIsResponsible())
            && !isSosCompletedPostSoa(caseData)) {
            //CA - CAN7 & CAN9, DA - DN2
            Map<String, Object> notifMap = new HashMap<>();
            notifMap.put(IS_PERSONAL, citizenAppPack.isPersonalService());
            citizenNotifications.addAll(getNotifications(caseData, NotificationNames.SOA_PERSONAL_APPLICANT, notifMap));
        }

        if (!isAnyOrderServedPostSoa(citizenAppPack, citizenDocumentsManagement.getCitizenOrders())) {
            //SOA Applicant - personal(court admin/court bailiff) OR non-personal service
            if (SERVED_PARTY_APPLICANT.equals(partyIdAndType.get(PARTY_TYPE)) //logged in party is applicant
                && CollectionUtils.isNotEmpty(citizenAppPack.getApplicantSoaPack())
                && (PERSONAL_SERVICE_SERVED_BY_CA.equals(citizenAppPack.getWhoIsResponsible())
                || PERSONAL_SERVICE_SERVED_BY_BAILIFF.equals(citizenAppPack.getWhoIsResponsible()))) {
                //CA - CAN4, DA - DN1
                Map<String, Object> notifMap = new HashMap<>();
                notifMap.put(IS_PERSONAL, citizenAppPack.isPersonalService());
                citizenNotifications.addAll(getNotifications(caseData, NotificationNames.SOA_APPLICANT, notifMap));
            } else if (SERVED_PARTY_RESPONDENT.equals(partyIdAndType.get(PARTY_TYPE)) //logged in party is respondent
                && CollectionUtils.isNotEmpty(citizenAppPack.getRespondentSoaPack())
                && !isResponseSubmittedByRespondent(
                citizenDocumentsManagement.getCitizenDocuments(),
                userDetails.getId()
            )) {
                //SOA Respondent - non-personal, personal(after SOS) - before response submission
                //CA - CAN5, DA - DN3
                citizenNotifications.addAll(getNotifications(caseData, NotificationNames.SOA_RESPONDENT, null));
            }
        }
    }

    private void addSosNotifications(CaseData caseData,
                                     CitizenDocumentsManagement citizenDocumentsManagement,
                                     List<CitizenNotification> citizenNotifications,
                                     String partyType) {
        if (SERVED_PARTY_APPLICANT.equals(partyType) //logged in party is applicant
            && isSosCompletedPostSoa(caseData)
            && !isAnyOrderServedPostSos(caseData, citizenDocumentsManagement.getCitizenOrders())) {
            CitizenDocuments citizenAppPack = citizenDocumentsManagement.getCitizenApplicationPacks().get(0);
            //SOS by Court admin/Court bailiff
            if (PERSONAL_SERVICE_SERVED_BY_CA.equals(citizenAppPack.getWhoIsResponsible())
                || PERSONAL_SERVICE_SERVED_BY_BAILIFF.equals(citizenAppPack.getWhoIsResponsible())) {
                //CA - CAN8, DA - DN5
                citizenNotifications.addAll(getNotifications(caseData, NotificationNames.SOA_SOS_CA_CB_APPLICANT, null));
            }
        }
    }


    private void addRespondentResponseNotification(CaseData caseData,
                                                   CitizenDocumentsManagement citizenDocumentsManagement,
                                                   List<CitizenNotification> citizenNotifications,
                                                   String partyType) {
        if (SERVED_PARTY_APPLICANT.equals(partyType)) { //logged in party is applicant
            //CAN6
            populateCitizenResponseNotifications(caseData,
                                                 citizenDocumentsManagement,
                                                 citizenNotifications,
                                                 RESPONDENT_APPLICATION,
                                                 NotificationNames.VIEW_RESPONDENT_RESP_APPLICANT
            );
            //CAN6A
            populateCitizenResponseNotifications(caseData,
                                                 citizenDocumentsManagement,
                                                 citizenNotifications,
                                                 RESPONDENT_C1A_APPLICATION,
                                                 NotificationNames.VIEW_RESPONDENT_C1A_APPL_APPLICANT
            );
            //CAN6B
            populateCitizenResponseNotifications(caseData,
                                                 citizenDocumentsManagement,
                                                 citizenNotifications,
                                                 RESPONDENT_C1A_RESPONSE,
                                                 NotificationNames.VIEW_RESPONDENT_C1A_RESP_APPLICANT
            );
        }
    }

    private void populateCitizenResponseNotifications(CaseData caseData,
                                                      CitizenDocumentsManagement citizenDocumentsManagement,
                                                      List<CitizenNotification> citizenNotifications,
                                                      String categoryId,
                                                      NotificationNames notificationName) {
        if (isRespondentResponseAvailable(citizenDocumentsManagement.getCitizenDocuments(), categoryId)
            && !isAnyOrderServedPostLatestResp(
            citizenDocumentsManagement.getCitizenDocuments(),
            citizenDocumentsManagement.getCitizenOrders(),
            categoryId
        )) {
            Map<String, Object> notifMap = new HashMap<>();
            notifMap.put(PARTY_NAMES, respondentNamesForProvidedResponseCategory(
                citizenDocumentsManagement.getCitizenDocuments(), categoryId));

            citizenNotifications.addAll(getNotifications(caseData, notificationName, notifMap));
        }
    }

    private boolean isFm5UploadedByParty(List<CitizenDocuments> citizenDocuments,
                                         UserDetails userDetails) {
        return CollectionUtils.isNotEmpty(citizenDocuments)
            && citizenDocuments.stream()
            .anyMatch(citizenDocument -> FM5_STATEMENTS.equals(citizenDocument.getCategoryId())
                && citizenDocument.getPartyId().equals(userDetails.getId()));
    }

    private List<CitizenDocuments> getAllSystemGeneratedDocuments(CaseData caseData) {
        List<CitizenDocuments> systemGenDocuments = new ArrayList<>();
        //C100/FL401 final English document
        if (null != caseData.getFinalDocument()) {
            CitizenDocuments finalDocumentEng = getSystemGeneratedDocument(caseData.getFinalDocument(),
                                                                           APPLICANT_APPLICATION,
                                                                           caseData.getDateSubmitted(),
                                                                           false);
            systemGenDocuments.add(finalDocumentEng);
        }
        //C100/FL401 final Welsh document
        if (null != caseData.getFinalWelshDocument()) {
            CitizenDocuments finalDocumentWel = getSystemGeneratedDocument(caseData.getFinalWelshDocument(),
                                                                           APPLICANT_APPLICATION,
                                                                           caseData.getDateSubmitted(),
                                                                           true);
            systemGenDocuments.add(finalDocumentWel);
        }

        //C1A English
        if (null != caseData.getC1ADocument()) {
            CitizenDocuments c1ADocumentEng = getSystemGeneratedDocument(caseData.getC1ADocument(),
                                                                         APPLICANT_C1A_APPLICATION,
                                                                         caseData.getDateSubmitted(),
                                                                         false);
            systemGenDocuments.add(c1ADocumentEng);
        }
        //C1A Welsh
        if (null != caseData.getC1AWelshDocument()) {
            CitizenDocuments c1ADocumentWel = getSystemGeneratedDocument(caseData.getC1AWelshDocument(),
                                                                         APPLICANT_C1A_APPLICATION,
                                                                         caseData.getDateSubmitted(),
                                                                         true);
            systemGenDocuments.add(c1ADocumentWel);
        }

        return systemGenDocuments;
    }

    private CitizenDocuments getSystemGeneratedDocument(Document document,
                                                        String categoryId,
                                                        String submittedDate,
                                                        boolean isWelsh) {
        return CitizenDocuments.builder()
            .partyId(TEST_UUID) //system generated docs
            .partyType(SERVED_PARTY_APPLICANT)
            .categoryId(categoryId)
            .document(isWelsh ? null : document)
            .documentWelsh(isWelsh ? document : null)
            .uploadedDate(LocalDateTime.of(LocalDate.parse(submittedDate,
                                                           DATE_FORMATTER_YYYY_MM_DD),
                                           LocalTime.of(0, 0)))
            .documentLanguage(isWelsh ? WELSH : ENGLISH)
            .uploadedBy(SYSTEM)
            .build();
    }

    private List<CitizenDocuments> getAllOrdersFromPreviousProceedings(CaseData caseData) {
        List<CitizenDocuments> ordersFromPreviousProceedings = new ArrayList<>();
        //C100 case
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            //Applicant
            if (CollectionUtils.isNotEmpty(caseData.getExistingProceedings())) {
                ordersFromPreviousProceedings.addAll(caseData.getExistingProceedings().stream()
                                                         .map(Element::getValue)
                                                         .filter(proceeding -> null != proceeding.getUploadRelevantOrder())
                                                         .map(proceeding -> getOrdersFromPrevProceedings(
                                                             caseData,
                                                             proceeding.getUploadRelevantOrder(),
                                                             PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION,
                                                             SERVED_PARTY_APPLICANT,
                                                             caseData.getApplicants().get(0).getId()
                                                         ))
                                                         .toList());
            }

            //Respondent
            if (CollectionUtils.isNotEmpty(caseData.getRespondents())) {
                ordersFromPreviousProceedings.addAll(caseData.getRespondents().stream()
                                                         .filter(party -> null != party.getValue().getResponse()
                                                             && CollectionUtils.isNotEmpty(
                                                                 party.getValue().getResponse().getRespondentExistingProceedings()))
                                                         .map(party -> party.getValue().getResponse().getRespondentExistingProceedings().stream()
                                                             .map(Element::getValue)
                                                             .filter(proceeding -> null != proceeding.getUploadRelevantOrder())
                                                             .map(proceeding -> getOrdersFromPrevProceedings(
                                                                 caseData,
                                                                 proceeding.getUploadRelevantOrder(),
                                                                 ORDERS_FROM_OTHER_PROCEEDINGS,
                                                                 SERVED_PARTY_RESPONDENT,
                                                                 party.getId()
                                                             ))
                                                             .toList()
                                                         ).flatMap(Collection::stream)
                                                         .toList());
            }
        }

        //FL401 case
        if (FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            //Applicant
            if (null != caseData.getFl401OtherProceedingDetails()
                && CollectionUtils.isNotEmpty(caseData.getFl401OtherProceedingDetails().getFl401OtherProceedings())) {
                ordersFromPreviousProceedings.addAll(caseData.getFl401OtherProceedingDetails().getFl401OtherProceedings().stream()
                                                         .map(Element::getValue)
                                                         .filter(proceeding -> null != proceeding.getUploadRelevantOrder())
                                                         .map(proceeding -> getOrdersFromPrevProceedings(
                                                             caseData,
                                                             proceeding.getUploadRelevantOrder(),
                                                             PREVIOUS_ORDERS_SUBMITTED_WITH_APPLICATION,
                                                             SERVED_PARTY_APPLICANT,
                                                             caseData.getApplicantsFL401().getPartyId()
                                                         ))
                                                         .toList());
            }

            //Respondent
            if (null != caseData.getRespondentsFL401().getResponse()
                && CollectionUtils.isNotEmpty(caseData.getRespondentsFL401().getResponse().getRespondentExistingProceedings())) {
                ordersFromPreviousProceedings.addAll(caseData.getRespondentsFL401().getResponse().getRespondentExistingProceedings().stream()
                                                         .map(Element::getValue)
                                                         .filter(proceeding -> null != proceeding.getUploadRelevantOrder())
                                                         .map(proceeding -> getOrdersFromPrevProceedings(
                                                             caseData,
                                                             proceeding.getUploadRelevantOrder(),
                                                             ORDERS_FROM_OTHER_PROCEEDINGS,
                                                             SERVED_PARTY_RESPONDENT,
                                                             caseData.getRespondentsFL401().getPartyId()
                                                         ))
                                                         .toList());
            }
        }

        return ordersFromPreviousProceedings;
    }

    private CitizenDocuments getOrdersFromPrevProceedings(CaseData caseData,
                                                          Document document,
                                                          String categoryId,
                                                          String partyType,
                                                          UUID partyId) {
        return CitizenDocuments.builder()
            .partyId(partyId.toString())
            .partyType(partyType)
            .categoryId(categoryId)
            .document(document)
            .uploadedDate(LocalDateTime.of(LocalDate.parse(caseData.getDateSubmitted(),
                                                           DATE_FORMATTER_YYYY_MM_DD),
                                           LocalTime.of(0, 0)))
            .build();
    }

    private List<CitizenDocuments> getAllApplicationWithinProceedingsDocuments(CaseData caseData) {
        List<CitizenDocuments> applicationsWithinProceedings = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(caseData.getAdditionalApplicationsBundle())) {
            caseData.getAdditionalApplicationsBundle().stream()
                .map(Element::getValue)
                .forEach(awp -> {
                    //C2 bundle docs
                    if (null != awp.getC2DocumentBundle()
                        && CollectionUtils.isNotEmpty(awp.getC2DocumentBundle().getFinalDocument())) {
                        applicationsWithinProceedings.addAll(getAwpDocuments(
                            awp,
                            awp.getC2DocumentBundle().getFinalDocument()
                        ));
                    }

                    //Other bundle docs
                    if (null != awp.getOtherApplicationsBundle()
                        && CollectionUtils.isNotEmpty(awp.getOtherApplicationsBundle().getFinalDocument())) {
                        applicationsWithinProceedings.addAll(getAwpDocuments(
                            awp,
                            awp.getOtherApplicationsBundle().getFinalDocument()
                        ));
                    }

                    //NEED SUPPORTING DOCUMENTS ?
                });
        }
        return applicationsWithinProceedings;
    }

    private List<CitizenDocuments> getAwpDocuments(AdditionalApplicationsBundle awp,
                                                   List<Element<Document>> documents) {
        return documents.stream()
            .map(Element::getValue)
            .map(document ->
                     CitizenDocuments.builder()
                         .partyId(TEST_UUID) // NEED TO REVISIT IF THIS IS REQUIRED OR NOT
                         .partyType(awp.getPartyType().getDisplayedValue())
                         .categoryId(PartyEnum.applicant.equals(awp.getPartyType())
                                         ? APPLICATIONS_WITHIN_PROCEEDINGS : APPLICATIONS_FROM_OTHER_PROCEEDINGS)
                         .document(document)
                         .uploadedDate(LocalDateTime.parse(awp.getUploadedDateTime(),
                                                           DATE_TIME_FORMATTER_DD_MMM_YYYY_HH_MM_SS))
                         .build()
            ).toList();
    }

    private Collection<? extends CitizenDocuments> getAllStatementOfServiceDocuments(CaseData caseData) {
        List<CitizenDocuments> statementOfServiceDocuments = new ArrayList<>();
        if (null != caseData.getStatementOfService()) {
            //SOS for SOA
            if (CollectionUtils.isNotEmpty(caseData.getStatementOfService().getStmtOfServiceForApplication())) {
                statementOfServiceDocuments.addAll(
                    getStatementOfServiceDocuments(caseData.getStatementOfService().getStmtOfServiceForApplication()));
            }

            //SOS for orders
            if (CollectionUtils.isNotEmpty(caseData.getStatementOfService().getStmtOfServiceForOrder())) {
                statementOfServiceDocuments.addAll(
                    getStatementOfServiceDocuments(caseData.getStatementOfService().getStmtOfServiceForOrder()));
            }
            //ADD CITIZEN SOS
        }
        return statementOfServiceDocuments;
    }

    private List<CitizenDocuments> getStatementOfServiceDocuments(List<Element<StmtOfServiceAddRecipient>> sosDocuments) {
        return sosDocuments.stream()
            .map(Element::getValue)
            .filter(sos -> null != sos.getStmtOfServiceDocument())
            .map(sos ->
                     CitizenDocuments.builder()
                         .categoryId(ANY_OTHER_DOC)
                         .document(sos.getStmtOfServiceDocument())
                         .uploadedDate(sos.getServedDateTimeOption())
                         .build()
            ).toList();
    }

    private void addOrderAdditionalDocumentsToOtherDocuments(CaseData caseData,
                                                             List<CitizenDocuments> citizenOrders,
                                                             List<CitizenDocuments> otherDocuments) {
        if (null != caseData.getManageOrders()
            && CollectionUtils.isNotEmpty(caseData.getManageOrders().getAdditionalOrderDocuments())) {
            otherDocuments.addAll(
                citizenOrders.stream()
                    .map(order -> getAdditionalDocuments(
                        order,
                        caseData.getManageOrders().getAdditionalOrderDocuments()
                    ))
                    .flatMap(Collection::stream)
                    .toList());
        }
    }

    private List<CitizenDocuments> getAdditionalDocuments(CitizenDocuments order,
                                                          List<Element<AdditionalOrderDocument>> additionalOrderDocuments) {
        return additionalOrderDocuments.stream()
            .map(Element::getValue)
            .filter(addDoc -> getStringsSplitByDelimiter(addDoc.getServedOrders(), COMMA)
                .contains(getOrderLabelForDynamicList(order)))
            .map(addDoc -> addDoc.getAdditionalDocuments().stream()
                .map(Element::getValue)
                .map(document -> CitizenDocuments.builder()
                    .categoryId(ANY_OTHER_DOC) //Added some default category
                    .document(document)
                    .uploadedDate(LocalDateTime.of(LocalDate.parse(addDoc.getUploadedDateTime().split(",")[0],
                                                                   DATE_FORMATTER_D_MMM_YYYY),
                                                   LocalTime.of(0, 0)))
                    .build())
                .toList())
            .flatMap(Collection::stream)
            .toList();
    }

    private String getOrderLabelForDynamicList(CitizenDocuments order) {
        return String.format("%s - %s", order.getOrderType(), order.getCreatedDate().format(DATE_FORMATTER_D_MMM_YYYY));
    }

    private boolean isFirstHearingCompleted(String authorization, String caseId) {
        Hearings hearings = hearingService.getHearings(authorization, caseId);
        return null != hearings
            && nullSafeCollection(hearings.getCaseHearings())
            .stream()
            .filter(caseHearing -> List.of(AWAITING_HEARING_DETAILS,COMPLETED).contains(caseHearing.getHmcStatus()))
            .map(CaseHearing::getHearingDaySchedule)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .min(Comparator.comparing(
                HearingDaySchedule::getHearingStartDateTime,
                Comparator.nullsLast(Comparator.naturalOrder())
            )).isPresent();
    }

    private boolean isAnyOrderServedPostSoa(CitizenDocuments citizenAppPack,
                                            List<CitizenDocuments> citizenOrders) {
        return isAnyOrderServedPostDate(citizenOrders, citizenAppPack.getUploadedDate());
    }

    private boolean isAnyOrderServedPostSos(CaseData caseData,
                                            List<CitizenDocuments> citizenOrders) {
        return (null != caseData.getStatementOfService()
            && CollectionUtils.isNotEmpty(caseData.getStatementOfService().getStmtOfServiceForApplication()))
            && caseData.getStatementOfService().getStmtOfServiceForApplication().stream()
            .anyMatch(stmtOfSerParty ->
                          isAnyOrderServedPostDate(citizenOrders, stmtOfSerParty.getValue().getServedDateTimeOption()));
    }

    private boolean isAnyOrderServedPostLatestResp(List<CitizenDocuments> citizenDocuments,
                                                   List<CitizenDocuments> citizenOrders, String categoryId) {
        return CollectionUtils.isNotEmpty(citizenDocuments)
            && isAnyOrderServedPostDate(citizenOrders,
                                        getLatestReviewedDocumentDate(citizenDocuments, categoryId));
    }

    private LocalDateTime getLatestReviewedDocumentDate(List<CitizenDocuments> citizenDocuments,
                                                        String category) {
        return citizenDocuments.stream()
            .filter(citizenDocument -> category.equals(citizenDocument.getCategoryId()))
            .min(comparing(
                CitizenDocuments::getUploadedDate,
                Comparator.nullsLast(Comparator.reverseOrder())
            ))
            .map(CitizenDocuments::getUploadedDate)
            .orElse(null);
    }

    private boolean isAnyOrderServedPostDate(List<CitizenDocuments> citizenOrders,
                                            LocalDateTime postDateTime) {
        return CollectionUtils.isNotEmpty(citizenOrders)
            && citizenOrders.stream()
            .anyMatch(citizenOrder -> null != citizenOrder.getServedDateTime()
                && citizenOrder.getServedDateTime().isAfter(postDateTime));
    }

    private boolean isSosCompletedPostSoa(CaseData caseData) {
        List<String> partyIds = CaseUtils.getPartyIdList(
            caseData.getCaseTypeOfApplication(),
            caseData.getRespondents(),
            caseData.getRespondentsFL401()
        );
        log.info("Respondent partyIds for SOS check {}", partyIds);
        return (null != caseData.getStatementOfService())
            && nullSafeCollection(caseData.getStatementOfService().getStmtOfServiceForApplication()).stream()
            .anyMatch(stmtOfSerParty -> new HashSet<>(getStringsSplitByDelimiter(
                stmtOfSerParty.getValue().getSelectedPartyId(),
                COMMA
            )).containsAll(partyIds));
    }

    private boolean isRespondentResponseAvailable(List<CitizenDocuments> citizenDocuments, String categoryId) {
        return CollectionUtils.isNotEmpty(citizenDocuments)
            && citizenDocuments.stream()
            .anyMatch(citizenDocument -> categoryId.equals(citizenDocument.getCategoryId()));
    }

    private String respondentNamesForProvidedResponseCategory(List<CitizenDocuments> citizenDocuments, String category) {
        if (citizenDocuments.stream()
            .filter(citizenDocument -> category.equals(citizenDocument.getCategoryId()))
            .map(CitizenDocuments::getPartyName)
            .anyMatch(Objects::isNull)) {
            return null;
        }
        List<String> respondentNames = citizenDocuments.stream()
            .filter(citizenDocument -> category.equals(citizenDocument.getCategoryId()))
            .map(CitizenDocuments::getPartyName)
            .distinct()
            .toList();
        log.info("respondent names {}", respondentNames);
        return String.join(", ", respondentNames);
    }

    private boolean isResponseSubmittedByRespondent(List<CitizenDocuments> citizenDocuments,
                                                    String idamId) {
        return CollectionUtils.isNotEmpty(citizenDocuments)
            && citizenDocuments.stream()
            .anyMatch(citizenDocument -> RESPONDENT_APPLICATION.equals(citizenDocument.getCategoryId())
                && idamId.equals(citizenDocument.getPartyId()));
    }


    private List<CitizenNotification> getNotifications(CaseData caseData,
                                                       NotificationNames notificationName,
                                                       Map<String, Object> notifMap) {
        Set<String> notificationIds = getNotificationIds(
            CaseUtils.getCaseTypeOfApplication(caseData),
            notificationName
        );
        //empty check added as not all case types have all notifications
        if (CollectionUtils.isNotEmpty(notificationIds)) {
            return notificationIds.stream()
                .map(notification -> {
                    CitizenNotification citizenNotification = CitizenNotification.builder()
                        .id(notification)
                        .show(true)
                        .build();
                    if (null != notifMap && !notifMap.isEmpty()) {
                        citizenNotification = citizenNotification.toBuilder()
                            .isNew(ObjectUtils.isNotEmpty(notifMap.get(IS_NEW)) ? (Boolean) notifMap.get(IS_NEW) : false)
                            .isFinal(ObjectUtils.isNotEmpty(notifMap.get(IS_FINAL)) ? (Boolean) notifMap.get(IS_FINAL) : false)
                            .isMultiple(ObjectUtils.isNotEmpty(notifMap.get(IS_MULTIPLE)) ? (Boolean) notifMap.get(IS_MULTIPLE) : false)
                            .isPersonalService(ObjectUtils.isNotEmpty(notifMap.get(IS_PERSONAL)) ? (Boolean) notifMap.get(IS_PERSONAL) : false)
                            .partyNames(ObjectUtils.isNotEmpty(notifMap.get(PARTY_NAMES)) ? (String) notifMap.get(PARTY_NAMES) : null)
                            .build();
                    }
                    return citizenNotification;
                }).toList();
        }
        return Collections.emptyList();
    }

    private Set<String> getNotificationIds(String caseTypeOfApplication,
                                           NotificationNames notificationName) {
        String notification = notificationsConfig.getNotifications().get(caseTypeOfApplication).get(notificationName);
        log.info("Retrieved notification ids {} for notification name {} & {} case type", notification, caseTypeOfApplication, notificationName);
        if (null != notification) {
            return getStringsSplitByDelimiter(notification, COMMA);
        }
        return Collections.emptySet();
    }
}
