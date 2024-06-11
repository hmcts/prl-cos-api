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
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
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
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetailsMeta;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.ServedParties;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.models.dto.citizen.CitizenDocuments;
import uk.gov.hmcts.reform.prl.models.dto.citizen.CitizenDocumentsManagement;
import uk.gov.hmcts.reform.prl.models.dto.citizen.CitizenNotification;
import uk.gov.hmcts.reform.prl.models.dto.citizen.UiCitizenCaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.repositories.CaseRepository;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.cafcass.HearingService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.FM5_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWAITING_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAN10_FM5;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAN4_SOA_PERS_NONPERS_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAN5_SOA_RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAN6_VIEW_RESPONSE_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAN7_SOA_PERSONAL_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAN8_SOS_PERSONAL_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAN9_SOA_PERSONAL_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COMMA;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COMPLETED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CRNF2_APPLICANT_RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CRNF3_PERS_SERV_APPLICANT;
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
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_UPDATE;
import static uk.gov.hmcts.reform.prl.enums.State.DECISION_OUTCOME;
import static uk.gov.hmcts.reform.prl.enums.State.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.prl.models.dto.citizen.CitizenDocumentsManagement.unReturnedCategoriesForUI;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.PERSONAL_SERVICE_SERVED_BY_BAILIFF;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.PERSONAL_SERVICE_SERVED_BY_CA;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.UNREPRESENTED_APPLICANT;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getPartyDetailsMeta;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseService {
    public static final String YES = "Yes";
    private final CoreCaseDataApi coreCaseDataApi;
    private final CaseRepository caseRepository;
    private final IdamClient idamClient;
    private final ObjectMapper objectMapper;
    private final RoleAssignmentService roleAssignmentService;
    private final UserService userService;
    private final CcdCoreCaseDataService ccdCoreCaseDataService;
    private final HearingService hearingService;

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

        CitizenDocumentsManagement citizenDocumentsManagement = CitizenDocumentsManagement.builder()
            .citizenDocuments(getCitizenDocuments(userDetails, caseData))
            .citizenOrders(getCitizenOrders(userDetails, caseData, partyIdAndType))
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

        if (caseData.getState().equals(PREPARE_FOR_HEARING_CONDUCT_HEARING)
            || caseData.getState().equals(DECISION_OUTCOME)) {

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

        caseData.getFinalServedApplicationDetailsList().stream()
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS);

        nullSafeCollection(servedApplicationDetails.getEmailNotificationDetails()).stream()
            .map(Element::getValue)
            .sorted(comparing(EmailNotificationDetails::getTimeStamp).reversed())
            .filter(emailNotificationDetails -> getPartyIds(emailNotificationDetails.getPartyIds())
                .contains(partyIdAndType.get(PARTY_ID)))
            .findFirst()
            .ifPresent(
                emailNotificationDetails -> citizenDocuments[0] = CitizenDocuments.builder()
                    .partyId(partyIdAndType.get(PARTY_ID))
                    .servedParty(emailNotificationDetails.getServedParty())
                    .uploadedDate(LocalDateTime.parse(
                        emailNotificationDetails.getTimeStamp(),
                        formatter
                    ))
                    .applicantSoaPack(
                        SERVED_PARTY_APPLICANT.equals(partyIdAndType.get(PARTY_TYPE))
                            ? emailNotificationDetails.getDocs().stream()
                            .map(Element::getValue)
                            .toList() : null
                    )
                    .respondentSoaPack(
                        SERVED_PARTY_RESPONDENT.equals(partyIdAndType.get(PARTY_TYPE))
                            ? emailNotificationDetails.getDocs().stream()
                                .map(Element::getValue)
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

    private List<String> getPartyIds(String partyIds) {
        return null != partyIds
            ? Arrays.stream(partyIds.trim().split(COMMA)).map(String::trim).toList()
            : Collections.emptyList();
    }

    private List<String> getPartyIds(List<Element<PartyDetails>> parties) {
        return CollectionUtils.isNotEmpty(parties)
            ? parties.stream().map(Element::getId).map(Objects::toString).toList()
            : Collections.emptyList();
    }

    private static List<Document> getUnservedRespondentDocumentList(ServiceOfApplication serviceOfApplication,
                                                                    ServedApplicationDetails servedApplicationDetails) {
        //populate respondent unserved packs only in case of personal service by unrepresented lip
        return UNREPRESENTED_APPLICANT.equals(servedApplicationDetails.getWhoIsResponsible())
            && null != serviceOfApplication.getUnServedRespondentPack()
            && CollectionUtils.isNotEmpty(serviceOfApplication.getUnServedRespondentPack().getPackDocument())
            ? serviceOfApplication.getUnServedRespondentPack()
            .getPackDocument().stream()
            .map(Element::getValue)
            .toList() : null;
    }


    private CitizenDocuments retreiveApplicationPackFromBulkPrintDetails(
        ServedApplicationDetails servedApplicationDetails,
        ServiceOfApplication serviceOfApplication,
        Map<String, String> partyIdAndType) {

        final CitizenDocuments[] citizenDocuments = {null};
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS);

        nullSafeCollection(servedApplicationDetails.getBulkPrintDetails()).stream()
            .map(Element::getValue)
            .sorted(comparing(BulkPrintDetails::getTimeStamp).reversed())
            .filter(bulkPrintDetails -> getPartyIds(bulkPrintDetails.getPartyIds())
                .contains(partyIdAndType.get(PARTY_ID)))
            .findFirst()
            .ifPresent(
                bulkPrintDetails -> citizenDocuments[0] = CitizenDocuments.builder()
                    .partyId(partyIdAndType.get(PARTY_ID))
                    .servedParty(bulkPrintDetails.getServedParty())
                    .uploadedDate(LocalDateTime.parse(
                        bulkPrintDetails.getTimeStamp(),
                        formatter
                    ))
                    .applicantSoaPack(
                        SERVED_PARTY_APPLICANT.equals(partyIdAndType.get(PARTY_TYPE))
                            ? bulkPrintDetails.getPrintDocs().stream()
                            .map(Element::getValue)
                            .toList() : null
                    )
                    .respondentSoaPack(
                        SERVED_PARTY_RESPONDENT.equals(partyIdAndType.get(PARTY_TYPE))
                            ? bulkPrintDetails.getPrintDocs().stream()
                                .map(Element::getValue)
                                .toList()
                            : getUnservedRespondentDocumentList(serviceOfApplication, servedApplicationDetails)
                    )
                    .build()
        );
        return citizenDocuments[0];
    }

    private List<CitizenDocuments> getCitizenDocuments(UserDetails userDetails,
                                                       CaseData caseData) {
        List<CitizenDocuments> citizenDocuments = new ArrayList<>();

        if (null != caseData.getReviewDocuments()) {
            //add solicitor uploaded docs
            citizenDocuments.addAll(addCitizenDocuments(caseData.getReviewDocuments().getLegalProfUploadDocListDocTab()));
            //add cafacss uploaded docs
            citizenDocuments.addAll(addCitizenDocuments(caseData.getReviewDocuments().getCafcassUploadDocListDocTab()));
            //add court staff uploaded docs
            citizenDocuments.addAll(addCitizenDocuments(caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab()));
            //add citizen uploaded docs
            citizenDocuments.addAll(addCitizenDocuments(caseData.getReviewDocuments().getCitizenUploadedDocListDocTab()));

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

        citizenDocuments.sort(comparing(CitizenDocuments::getUploadedDate).reversed());

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
            .partyName(quarantineDoc.getUploadedBy())//CAN NOT BE UPLOADEDBY, SHOULD BE PARTY NAME
            .categoryId(quarantineDoc.getCategoryId())
            .uploadedBy(quarantineDoc.getUploadedBy())
            .uploadedDate(quarantineDoc.getDocumentUploadedDate())
            .document(existingDocument)
            .documentLanguage(quarantineDoc.getDocumentLanguage())
            .build();
    }

    private List<CitizenDocuments> getCitizenOrders(UserDetails userDetails,
                                                    CaseData caseData,
                                                    Map<String, String> partyIdAndType) {
        List<CitizenDocuments> citizenOrders = new ArrayList<>();

        if (!partyIdAndType.isEmpty()) {
            citizenOrders.addAll(getCitizenOrdersForParty(caseData, partyIdAndType, userDetails.getId()));
        }

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
            .createdDate(getOrderMadeDate(order))
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
            .build();
    }

    private boolean isPersonalService(OrderDetails order) {
        return null != order.getServeOrderDetails()
            && YesOrNo.Yes.equals(order.getServeOrderDetails().getServeOnRespondent());
    }

    private boolean isPersonalService(ServedApplicationDetails servedAppPack) {
        return UNREPRESENTED_APPLICANT.equals(servedAppPack.getWhoIsResponsible())
            || PERSONAL_SERVICE_SERVED_BY_CA.equals(servedAppPack.getWhoIsResponsible())
            || PERSONAL_SERVICE_SERVED_BY_BAILIFF.equals(servedAppPack.getWhoIsResponsible());
    }

    private LocalDate getOrderMadeDate(OrderDetails order) {
        if (null != order.getOtherDetails()
            && null != order.getOtherDetails().getOrderMadeDate()) {
            return LocalDate.parse(
                order.getOtherDetails().getOrderMadeDate(),
                DateTimeFormatter.ofPattern(D_MMM_YYYY)
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
        if (CollectionUtils.isEmpty(citizenDocumentsManagement.getCitizenOrders())) {
            citizenNotifications.add(CitizenNotification.builder().id(CRNF2_APPLICANT_RESPONDENT).show(false).build());
            citizenNotifications.add(CitizenNotification.builder().id(CRNF3_PERS_SERV_APPLICANT).show(false).build());
        } else {
            addOrderNotifications(citizenDocumentsManagement.getCitizenOrders(), citizenNotifications);
        }

        //PRL-5431 - SOA & response notifications
        if (CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenApplicationPacks())) {
            addSoaNotifications(citizenDocumentsManagement, citizenNotifications, userDetails, partyIdAndType);
        }

        //PRL-5431 - Statement of service notifications
        if (isSosCompletedPostSoa(caseData, getPartyIds(caseData.getRespondents()))) {
            CitizenDocuments citizenAppPack = citizenDocumentsManagement.getCitizenApplicationPacks().get(0);
            //SOS by unrepresented applicant
            if (UNREPRESENTED_APPLICANT.equals(citizenAppPack.getWhoIsResponsible())) {
                citizenNotifications.add(CitizenNotification.builder().id(CAN7_SOA_PERSONAL_APPLICANT).show(false).build());
                citizenNotifications.add(CitizenNotification.builder().id(CAN9_SOA_PERSONAL_APPLICANT).show(false).build());
            }

            //SOS by Court admin/Court bailiff
            if (PERSONAL_SERVICE_SERVED_BY_CA.equals(citizenAppPack.getWhoIsResponsible())
                || PERSONAL_SERVICE_SERVED_BY_BAILIFF.equals(citizenAppPack.getWhoIsResponsible())) {
                citizenNotifications.add(CitizenNotification.builder().id(CAN8_SOS_PERSONAL_APPLICANT).show(true).build());
            }
        }

        return citizenNotifications;
    }

    private void addOrderNotifications(List<CitizenDocuments> citizenOrders,
                                       List<CitizenNotification> citizenNotifications) {
        CitizenNotification citizenNotification;
        List<CitizenDocuments> multipleOrdersServed = citizenOrders.stream()
            .filter(CitizenDocuments::isMultiple).toList();

        if (citizenOrders.get(0).isPersonalService()) {
            //CRNF3 - personal service
            citizenNotification = CitizenNotification.builder().id(CRNF3_PERS_SERV_APPLICANT).show(true).isPersonalService(true).build();
        } else {
            //CRNF2 - non personal service
            citizenNotification = CitizenNotification.builder().id(CRNF2_APPLICANT_RESPONDENT).show(true).isPersonalService(false).build();
        }

        citizenNotification = citizenNotification.toBuilder()
            .isNew(multipleOrdersServed.stream().anyMatch(CitizenDocuments::isNew))
            .isFinal(multipleOrdersServed.stream().anyMatch(CitizenDocuments::isFinal))
            .isMultiple(multipleOrdersServed.size() > 1)
            .build();
        citizenNotifications.add(citizenNotification);
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
            citizenNotifications.add(CitizenNotification.builder().id(CAN10_FM5).show(true).build());
        } else {
            citizenNotifications.add(CitizenNotification.builder().id(CAN10_FM5).show(false).build());
        }
    }

    private void addSoaNotifications(CitizenDocumentsManagement citizenDocumentsManagement,
                                     List<CitizenNotification> citizenNotifications,
                                     UserDetails userDetails,
                                     Map<String, String> partyIdAndType) {
        CitizenDocuments citizenAppPack = citizenDocumentsManagement.getCitizenApplicationPacks().get(0);
        if (isAnyOrderServedPostSoa(citizenAppPack, citizenDocumentsManagement.getCitizenOrders())) {
            citizenNotifications.add(CitizenNotification.builder().id(CAN4_SOA_PERS_NONPERS_APPLICANT).show(false).build());
            citizenNotifications.add(CitizenNotification.builder().id(CAN5_SOA_RESPONDENT).show(false).build());
            citizenNotifications.add(CitizenNotification.builder().id(CAN6_VIEW_RESPONSE_APPLICANT).show(false).build());
            citizenNotifications.add(CitizenNotification.builder().id(CAN8_SOS_PERSONAL_APPLICANT).show(false).build());
        } else {
            //SOA Applicant - personal service(unrepresented applicant)
            if (UNREPRESENTED_APPLICANT.equals(citizenAppPack.getWhoIsResponsible())) {
                citizenNotifications.add(CitizenNotification.builder().id(CAN7_SOA_PERSONAL_APPLICANT).show(true)
                                             .isPersonalService(citizenAppPack.isPersonalService()).build());
                citizenNotifications.add(CitizenNotification.builder().id(CAN9_SOA_PERSONAL_APPLICANT).show(true)
                                             .isPersonalService(citizenAppPack.isPersonalService()).build());
            }

            //SOA Applicant - personal(court admin/court bailiff)/non-personal
            if (CollectionUtils.isNotEmpty(citizenAppPack.getApplicantSoaPack())
                && SERVED_PARTY_APPLICANT.equals(partyIdAndType.get(PARTY_TYPE))) {
                citizenNotifications.add(CitizenNotification.builder().id(CAN4_SOA_PERS_NONPERS_APPLICANT).show(true)
                                             .isPersonalService(citizenAppPack.isPersonalService()).build());
            } else {
                citizenNotifications.add(CitizenNotification.builder().id(CAN4_SOA_PERS_NONPERS_APPLICANT).show(false).build());
            }

            //SOA Respondent - non-personal
            if (CollectionUtils.isNotEmpty(citizenAppPack.getRespondentSoaPack())
                && SERVED_PARTY_RESPONDENT.equals(partyIdAndType.get(PARTY_TYPE))
                && !isResponseSubmittedByRespondent(citizenDocumentsManagement.getCitizenDocuments(), userDetails.getId())) {
                citizenNotifications.add(CitizenNotification.builder().id(CAN5_SOA_RESPONDENT).show(true).build());
            } else {
                citizenNotifications.add(CitizenNotification.builder().id(CAN5_SOA_RESPONDENT).show(false).build());
            }

            //Respondent response
            if (isRespondentResponseAvailable(citizenDocumentsManagement.getCitizenDocuments())) {
                citizenNotifications.add(CitizenNotification.builder().id(CAN6_VIEW_RESPONSE_APPLICANT).show(true).build());
            }
        }
    }

    private boolean isFm5UploadedByParty(List<CitizenDocuments> citizenDocuments,
                                         UserDetails userDetails) {
        return CollectionUtils.isNotEmpty(citizenDocuments)
            && citizenDocuments.stream()
            .anyMatch(citizenDocument -> FM5_STATEMENTS.equals(citizenDocument.getCategoryId())
                && citizenDocument.getPartyId().equals(userDetails.getId()));
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

    //CHECK & REMOVE IF NOT NEEDED
    /*private boolean isAnyOrderServedPostSos(CaseData caseData,
                                            List<CitizenDocuments> citizenOrders) {
        return (null != caseData.getStatementOfService()
            && CollectionUtils.isNotEmpty(caseData.getStatementOfService().getStmtOfServiceForApplication()))
            && caseData.getStatementOfService().getStmtOfServiceForApplication().stream()
            .anyMatch(stmtOfSerParty ->
                          isAnyOrderServedPostDate(citizenOrders, stmtOfSerParty.getValue().getServedDateTimeOption().toLocalDate()));
    }*/

    private boolean isAnyOrderServedPostDate(List<CitizenDocuments> citizenOrders,
                                            LocalDateTime postDateTime) {
        return CollectionUtils.isNotEmpty(citizenOrders)
            && citizenOrders.stream()
            .anyMatch(citizenOrder -> null != citizenOrder.getServedDateTime()
                && citizenOrder.getServedDateTime().isAfter(postDateTime));
    }

    private boolean isSosCompletedPostSoa(CaseData caseData, List<String> partyIds) {
        return (null != caseData.getStatementOfService())
            && nullSafeCollection(caseData.getStatementOfService().getStmtOfServiceForApplication()).stream()
            .anyMatch(stmtOfSerParty -> new HashSet<>(getPartyIds(stmtOfSerParty.getValue().getSelectedPartyId()))
                .containsAll(partyIds));
    }

    private boolean isRespondentResponseAvailable(List<CitizenDocuments> citizenDocuments) {
        return CollectionUtils.isNotEmpty(citizenDocuments)
            && citizenDocuments.stream()
            .anyMatch(citizenDocument -> RESPONDENT_APPLICATION.equals(citizenDocument.getCategoryId()));
    }

    private boolean isResponseSubmittedByRespondent(List<CitizenDocuments> citizenDocuments,
                                                    String idamId) {
        return CollectionUtils.isNotEmpty(citizenDocuments)
            && citizenDocuments.stream()
            .anyMatch(citizenDocument -> RESPONDENT_APPLICATION.equals(citizenDocument.getCategoryId())
                && idamId.equals(citizenDocument.getPartyId()));
    }
}
