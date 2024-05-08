package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.caseflags.request.CitizenPartyFlagsRequest;
import uk.gov.hmcts.reform.prl.models.caseflags.request.FlagDetailRequest;
import uk.gov.hmcts.reform.prl.models.citizen.CaseDataWithHearingResponse;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetailsMeta;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.models.dto.citizen.CitizenDocuments;
import uk.gov.hmcts.reform.prl.models.dto.citizen.CitizenDocumentsManagement;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COMMA;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DD_MMM_YYYY_HH_MM_SS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PARTY_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PARTY_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.PARTY_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_CAFCASS_CYMRU;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_RESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_UPDATE;
import static uk.gov.hmcts.reform.prl.enums.State.DECISION_OUTCOME;
import static uk.gov.hmcts.reform.prl.enums.State.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.prl.models.dto.citizen.CitizenDocumentsManagement.unReturnedCategoriesForUI;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getPartyDetailsMeta;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseService {
    public static final String YES = "Yes";
    public static final String ERROR = "error";
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
        log.info("Inside getPartyCaseFlags caseId {}", caseId);
        log.info("Inside getPartyCaseFlags partyId {}", partyId);
        CaseDetails caseDetails = getCase(authToken, caseId);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        Optional<PartyDetailsMeta> partyDetailsMeta = getPartyDetailsMeta(
            partyId,
            caseData.getCaseTypeOfApplication(),
            caseData
        );

        if (partyDetailsMeta.isPresent()) {
            log.info("Party details meta {}", partyDetailsMeta.get().getPartyDetails());
            log.info(
                "Party details meta labelForDynamicList {}",
                partyDetailsMeta.get().getPartyDetails().getLabelForDynamicList()
            );
        }

        if (partyDetailsMeta.isPresent()
            && partyDetailsMeta.get().getPartyDetails() != null
            && !StringUtils.isEmpty(partyDetailsMeta.get().getPartyDetails().getLabelForDynamicList())) {
            log.info("Party details meta is valid ");
            Optional<String> partyExternalCaseFlagField = getPartyExternalCaseFlagField(
                caseData.getCaseTypeOfApplication(),
                partyDetailsMeta.get().getPartyType(),
                partyDetailsMeta.get().getPartyIndex()
            );

            if (partyExternalCaseFlagField.isPresent()) {
                log.info("partyExternalCaseFlagField is present:: {}", partyExternalCaseFlagField.get());
                log.info("data is present as well:: {}", caseDetails.getData().get(partyExternalCaseFlagField.get()));
                log.info(" validating the data with entire case details:: {}", caseDetails.getData());
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
        log.info("Inside updateCitizenRAflags caseId {}", caseId);
        log.info("Inside updateCitizenRAflags eventId {}", eventId);
        log.info("Inside updateCitizenRAflags citizenPartyFlagsRequest {}", citizenPartyFlagsRequest);

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
        try {
            log.info("partyExternalCaseFlagField ===>" + objectMapper.writeValueAsString(partyExternalCaseFlagField.get()));
        } catch (JsonProcessingException e) {
            log.info("error");
        }

        Flags flags = objectMapper.convertValue(
            updatedCaseData.get(partyExternalCaseFlagField.get()),
            Flags.class
        );
        try {
            log.info("Existing external Party flags  ===>" + objectMapper.writeValueAsString(flags));
        } catch (JsonProcessingException e) {
            log.info("error");
        }
        flags = flags.toBuilder()
            .details(convertFlags(citizenPartyFlagsRequest.getPartyExternalFlags().getDetails()))
            .build();
        try {
            log.info("Updated external Party flags  ===>" + objectMapper.writeValueAsString(flags));
        } catch (JsonProcessingException e) {
            log.info("error");
        }
        Map<String, Object> externalCaseFlagMap = new HashMap<>();
        externalCaseFlagMap.put(partyExternalCaseFlagField.get(), flags);

        CaseDataContent caseDataContent = ccdCoreCaseDataService.createCaseDataContent(
            startEventResponse,
            externalCaseFlagMap
        );

        try {
            log.info("Case data content is  ===>" + objectMapper.writeValueAsString(caseDataContent));
        } catch (JsonProcessingException e) {
            log.info("error");
        }

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

        return CitizenDocumentsManagement.builder()
            .citizenDocuments(getCitizenDocuments(userDetails, caseData))
            .citizenOrders(getCitizenOrders(userDetails, caseData))
            .citizenApplicationPacks(getCitizenApplicationPacks(userDetails, caseData))
            .build();
    }

    private List<CitizenDocuments> getCitizenApplicationPacks(UserDetails userDetails,
                                                              CaseData caseData) {
        List<CitizenDocuments> citizenDocuments = new ArrayList<>();

        if (caseData.getState().equals(PREPARE_FOR_HEARING_CONDUCT_HEARING)
            || caseData.getState().equals(DECISION_OUTCOME)) {
            HashMap<String, String> partyIdAndType = findPartyIdAndType(caseData, userDetails);

            if (!partyIdAndType.isEmpty()) {
                citizenDocuments.addAll(fetchSoaPacksForParty(caseData, partyIdAndType));
            }
            return citizenDocuments;
        }
        return Collections.emptyList();
    }

    private List<CitizenDocuments> fetchSoaPacksForParty(CaseData caseData, HashMap<String, String> partyIdAndType) {
        final List<CitizenDocuments>[] citizenDocuments = new List[]{new ArrayList<>()};

        caseData.getFinalServedApplicationDetailsList().stream()
            .map(Element::getValue)
            .sorted(comparing(ServedApplicationDetails::getServedAt).reversed())
            .forEach(servedApplicationDetails -> {
                if (citizenDocuments[0].isEmpty()
                    && servedApplicationDetails.getModeOfService() != null) {
                    if (servedApplicationDetails.getModeOfService().equals("By email")) {
                        citizenDocuments[0].add(retrieveApplicationPackFromEmailNotifications(
                            servedApplicationDetails.getEmailNotificationDetails(), caseData.getServiceOfApplication(),
                            partyIdAndType
                        ));
                    } else {
                        citizenDocuments[0].add(retreiveApplicationPackFromBulkPrintDetails(
                            servedApplicationDetails.getBulkPrintDetails(), caseData.getServiceOfApplication(),
                            partyIdAndType
                        ));
                    }
                }
            });
        return citizenDocuments[0];
    }

    private CitizenDocuments retrieveApplicationPackFromEmailNotifications(
        List<Element<EmailNotificationDetails>> emailNotificationDetailsList,
        ServiceOfApplication serviceOfApplication, HashMap<String, String> partyIdAndType) {
        final CitizenDocuments[] citizenDocuments = {null};
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS);

        nullSafeCollection(emailNotificationDetailsList).stream()
            .map(Element::getValue)
            .sorted(comparing(EmailNotificationDetails::getTimeStamp).reversed())
            .filter(emailNotificationDetails -> getPartyIds(emailNotificationDetails.getPartyIds())
                .contains(partyIdAndType.get(PARTY_ID)))
            .findFirst()
            .ifPresent(
                emailNotificationDetails -> citizenDocuments[0] = CitizenDocuments.builder()
                    .partyId(emailNotificationDetails.getPartyIds())
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
                            ? (
                            emailNotificationDetails.getDocs().stream()
                                .map(Element::getValue)
                                .toList()
                        ) : getUnservedRespondentDocumentList(serviceOfApplication)
                    )
                    .wasCafcassServed(isCafcassOrCafcassCymruServed(emailNotificationDetailsList))
                    .build()
        );
        return citizenDocuments[0];
    }

    private List<String> getPartyIds(String partyIds) {
        return null != partyIds
            ? Arrays.stream(partyIds.trim().split(COMMA)).map(String::trim).toList()
            : Collections.emptyList();
    }

    private static List<Document> getUnservedRespondentDocumentList(ServiceOfApplication serviceOfApplication) {
        return null != serviceOfApplication.getUnServedRespondentPack()
            ? serviceOfApplication.getUnServedRespondentPack()
            .getPackDocument().stream()
            .map(Element::getValue)
            .toList() : null;
    }


    private CitizenDocuments retreiveApplicationPackFromBulkPrintDetails(
        List<Element<BulkPrintDetails>> bulkPrintDetailsList,
        ServiceOfApplication serviceOfApplication, HashMap<String, String> partyIdAndType) {

        final CitizenDocuments[] citizenDocuments = {null};
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS);

        nullSafeCollection(bulkPrintDetailsList).stream()
            .map(Element::getValue)
            .sorted(comparing(BulkPrintDetails::getTimeStamp).reversed())
            .filter(bulkPrintDetails -> getPartyIds(bulkPrintDetails.getPartyIds())
                .contains(partyIdAndType.get(PARTY_ID)))
            .findFirst()
            .ifPresent(
                bulkPrintDetails -> citizenDocuments[0] = CitizenDocuments.builder()
                    .partyId(bulkPrintDetails.getPartyIds())
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
                            ? (
                            bulkPrintDetails.getPrintDocs().stream()
                                .map(Element::getValue)
                                .toList()
                        ) : getUnservedRespondentDocumentList(serviceOfApplication)
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
            .partyId(quarantineDoc.getUploadedByIdamId())
            .partyType(quarantineDoc.getDocumentParty())
            .partyName(quarantineDoc.getUploadedBy())
            .categoryId(quarantineDoc.getCategoryId())
            .uploadedBy(quarantineDoc.getUploadedBy())
            .uploadedDate(quarantineDoc.getDocumentUploadedDate())
            .document(existingDocument)
            .documentLanguage(quarantineDoc.getDocumentLanguage())
            .build();
    }

    private List<CitizenDocuments> getCitizenOrders(UserDetails userDetails, CaseData caseData) {
        List<CitizenDocuments> citizenDocuments = new ArrayList<>();
        HashMap<String, String> partyIdAndType = findPartyIdAndType(caseData, userDetails);

        if (!partyIdAndType.isEmpty()) {
            citizenDocuments.addAll(getCitizenOrdersForParty(caseData, partyIdAndType, userDetails.getId()));
        }

        return citizenDocuments;
    }

    private List<CitizenDocuments> getCitizenOrdersForParty(CaseData caseData,
                                                            HashMap<String, String> partyIdAndType,
                                                            String idamId) {
        String partyId = partyIdAndType.get(PARTY_ID);
        log.info("*** partyId from idamId {}", partyId);

        return nullSafeCollection(caseData.getOrderCollection()).stream()
            .map(Element::getValue)
            .filter(order -> isOrderServedForParty(order, partyId))
            .map(order -> createCitizenOrder(order, idamId, partyIdAndType))
            .toList();
    }

    private CitizenDocuments createCitizenOrder(OrderDetails order,
                                                String idamId,
                                                HashMap<String, String> partyIdAndType) {
        return CitizenDocuments.builder()
            .partyId(idamId)
            .partyType(partyIdAndType.get(PARTY_TYPE))
            .partyName(partyIdAndType.get(PARTY_NAME))
            .orderType(order.getOrderTypeId())
            .uploadedBy(order.getOtherDetails().getCreatedBy())
            .createdDate(getOrderMadeDate(order))
            .servedDate(getServedDate(order))
            .document(order.getOrderDocument())
            .documentWelsh(order.getOrderDocumentWelsh())
            .isNew(!isFinalOrder(order))
            .isFinal(isFinalOrder(order))
            .wasCafcassServed(isCafcassOrCafcassCymruServed(order))
            .build();
    }

    private LocalDate getOrderMadeDate(OrderDetails order) {
        if (null != order.getOtherDetails()
            && null != order.getOtherDetails().getOrderMadeDate()) {
            return LocalDate.parse(
                order.getOtherDetails().getOrderMadeDate(),
                DateTimeFormatter.ofPattern("dd MMM yyyy")
            );
        } else if (null != order.getDateCreated()) {
            //If order made date is not available then fallback to order created date.
            return order.getDateCreated().toLocalDate();
        }
        return null;
    }

    private LocalDate getServedDate(OrderDetails order) {
        if (null != order.getOtherDetails()
            && null != order.getOtherDetails().getOrderServedDate()) {
            return LocalDate.parse(
                order.getOtherDetails().getOrderServedDate(),
                DateTimeFormatter.ofPattern("dd MMM yyyy")
            );
        }
        return null;
    }

    private boolean isCafcassOrCafcassCymruServed(OrderDetails order) {
        return null != order.getServeOrderDetails()
            && (YesOrNo.Yes.equals(order.getServeOrderDetails().getCafcassServed())
            || YesOrNo.Yes.equals(order.getServeOrderDetails().getCafcassCymruServed()));
    }

    private boolean isCafcassOrCafcassCymruServed(List<Element<EmailNotificationDetails>> emailNotificationDetailsList) {
        return nullSafeCollection(emailNotificationDetailsList).stream()
            .map(Element::getValue)
            .anyMatch(emailNotificationDetails ->
                          SERVED_PARTY_CAFCASS.equalsIgnoreCase(emailNotificationDetails.getServedParty())
                              || SERVED_PARTY_CAFCASS_CYMRU.equalsIgnoreCase(emailNotificationDetails.getServedParty()));
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

    private HashMap<String, String> findPartyIdAndType(CaseData caseData,
                                       UserDetails userDetails) {
        HashMap<String, String> partyIdAndTypeMap = new HashMap<>();
        log.info("*** Inside find partyId method ***");
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            log.info("*** C100 case type");
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
            log.info("*** FL401 case type");
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

        return new HashMap<>();
    }

    private Optional<Element<PartyDetails>> getParty(List<Element<PartyDetails>> parties,
                                                       UserDetails userDetails) {
        return nullSafeCollection(parties).stream()
            .filter(element -> null != element.getValue().getUser()
                && userDetails.getId().equalsIgnoreCase(element.getValue().getUser().getIdamId()))
            .findFirst();
    }
}
