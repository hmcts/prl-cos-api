package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildApplicantDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildRespondentDetailsElements;
import uk.gov.hmcts.reform.prl.models.citizen.CaseDataWithHearingResponse;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.models.dto.citizen.CitizenDocuments;
import uk.gov.hmcts.reform.prl.models.dto.citizen.CitizenDocumentsManagement;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.repositories.CaseRepository;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.cafcass.HearingService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_DEFAULT_COURT_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT_WITH_HWF;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_UPDATE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseService {
    public static final String YES = "Yes";
    private final CoreCaseDataApi coreCaseDataApi;
    private final CaseRepository caseRepository;
    private final IdamClient idamClient;
    private final ObjectMapper objectMapper;
    private final CaseDataMapper caseDataMapper;
    private final RoleAssignmentService roleAssignmentService;
    private final UserService userService;
    private static final String INVALID_CLIENT = "Invalid Client";
    private final CcdCoreCaseDataService ccdCoreCaseDataService;
    private final HearingService hearingService;

    public CaseDetails updateCase(CaseData caseData, String authToken,
                                  String caseId, String eventId) throws JsonProcessingException {
        if (CITIZEN_CASE_SUBMIT.getValue().equalsIgnoreCase(eventId)
            || CITIZEN_CASE_SUBMIT_WITH_HWF.getValue().equalsIgnoreCase(eventId)) {
            UserDetails userDetails = idamClient.getUserDetails(authToken);
            UserInfo userInfo = UserInfo
                .builder()
                .idamId(userDetails.getId())
                .firstName(userDetails.getForename())
                .lastName(userDetails.getSurname().orElse(null))
                .emailAddress(userDetails.getEmail())
                .build();

            CaseData updatedCaseData = caseDataMapper
                .buildUpdatedCaseData(caseData);
            updatedCaseData = updatedCaseData.toBuilder()
                .userInfo(wrapElements(userInfo))
                .courtName(C100_DEFAULT_COURT_NAME)
                .taskListVersion(TASK_LIST_VERSION_V2)
                .build();
            return caseRepository.updateCase(authToken, caseId, updatedCaseData, CaseEvent.fromValue(eventId));
        }
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
        List<CaseDetails> caseDetails = new ArrayList<>();
        caseDetails.addAll(performSearch(authToken, userDetails, searchCriteria, s2sToken));
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

    public Map<String, String> fetchIdamAmRoles(String authorisation, String emailId) {
        return roleAssignmentService.fetchIdamAmRoles(authorisation, emailId);
    }

    public CaseDataWithHearingResponse getCaseWithHearing(String authorisation, String caseId, String hearingNeeded) {
        CaseDataWithHearingResponse caseDataWithHearingResponse = CaseDataWithHearingResponse.builder().build();
        CaseDetails caseDetails = ccdCoreCaseDataService.findCaseById(authorisation, caseId);
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        caseData = caseData.toBuilder().noOfDaysRemainingToSubmitCase(
            CaseUtils.getRemainingDaysSubmitCase(caseData)).build();
        caseDataWithHearingResponse = caseDataWithHearingResponse.toBuilder().caseData(caseData).build();
        if ("Yes".equalsIgnoreCase(hearingNeeded)) {
            caseDataWithHearingResponse =
                caseDataWithHearingResponse.toBuilder().hearings(
                    hearingService.getHearings(authorisation, caseId)).build();
        }

        return caseDataWithHearingResponse;
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

        switch (caseData.getState()) {
            case PREPARE_FOR_HEARING_CONDUCT_HEARING:
            case DECISION_OUTCOME: {
                HashMap<String, String> partyIdAndType = findPartyIdAndType(caseData, userDetails);

                if (partyIdAndType != null) {
                    citizenDocuments.addAll(fetchSoaPacksForParty(caseData, partyIdAndType));
                }
                return citizenDocuments;
            }
            default:
                return null;
        }
    }

    private List<CitizenDocuments> fetchSoaPacksForParty(CaseData caseData, HashMap<String, String> partyIdAndType) {
        final List<CitizenDocuments>[] citizenDocuments = new List[]{new ArrayList<>()};

        caseData.getFinalServedApplicationDetailsList().stream()
            .map(Element::getValue)
            .sorted(comparing(ServedApplicationDetails::getServedAt).reversed())
            .forEach(servedApplicationDetails -> {
                if (citizenDocuments[0].size() == 0) {
                    if (servedApplicationDetails.getModeOfService().equals("By email")) {
                        citizenDocuments[0].add(retreiveApplicationPackFromEmailNotifications(
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

    private CitizenDocuments retreiveApplicationPackFromEmailNotifications(
        List<Element<EmailNotificationDetails>> emailNotificationDetailsList,
        ServiceOfApplication serviceOfApplication, HashMap<String, String> partyIdAndType) {
        final CitizenDocuments[] citizenDocuments = {null};

        String partyId = partyIdAndType.entrySet().stream()
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");

        emailNotificationDetailsList.stream()
            .map(Element::getValue)
            .sorted(comparing(EmailNotificationDetails::getTimeStamp).reversed())
            .filter(emailNotificationDetails -> emailNotificationDetails.getPartyIds().contains(partyId))
            .findFirst()
            .ifPresent(
                emailNotificationDetails -> {
                    citizenDocuments[0] = CitizenDocuments.builder()
                        .partyId(emailNotificationDetails.getPartyIds())
                        .servedParty(emailNotificationDetails.getServedParty())
                        .uploadedDate(LocalDateTime.parse(
                            emailNotificationDetails.getTimeStamp(),
                            formatter
                        ))
                        .applicantSoaPack(
                            partyIdAndType.get(partyId).equals(SERVED_PARTY_APPLICANT)
                                ? emailNotificationDetails.getDocs().stream()
                                .map(Element::getValue)
                                .collect(Collectors.toList()) : null
                        )
                        .respondentSoaPack(
                            partyIdAndType.get(partyId).equals(SERVED_PARTY_RESPONDENT)
                                ? (
                                emailNotificationDetails.getDocs().stream()
                                    .map(Element::getValue)
                                    .collect(Collectors.toList())
                            ) : getUnservedRespondentDocumentList(serviceOfApplication)
                        )
                        .build();
                }
            );
        return citizenDocuments[0];
    }

    private static List<Document> getUnservedRespondentDocumentList(ServiceOfApplication serviceOfApplication) {
        return null != serviceOfApplication.getUnServedRespondentPack()
            ? serviceOfApplication.getUnServedRespondentPack()
            .getPackDocument().stream()
            .map(Element::getValue)
            .collect(Collectors.toList()) : null;
    }


    private CitizenDocuments retreiveApplicationPackFromBulkPrintDetails(
        List<Element<BulkPrintDetails>> bulkPrintDetailsList,
        ServiceOfApplication serviceOfApplication, HashMap<String, String> partyIdAndType) {

        final CitizenDocuments[] citizenDocuments = {null};

        String partyId = partyIdAndType.entrySet().stream()
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");

        bulkPrintDetailsList.stream()
            .map(Element::getValue)
            .sorted(comparing(BulkPrintDetails::getTimeStamp).reversed())
            .filter(bulkPrintDetails -> bulkPrintDetails.getPartyIds().contains(partyId))
            .findFirst()
            .ifPresent(
                bulkPrintDetails -> {
                    citizenDocuments[0] = CitizenDocuments.builder()
                        .partyId(bulkPrintDetails.getPartyIds())
                        .servedParty(bulkPrintDetails.getServedParty())
                        .uploadedDate(LocalDateTime.parse(
                            bulkPrintDetails.getTimeStamp(),
                            formatter
                        ))
                        .applicantSoaPack(
                            partyIdAndType.get(partyId).equals(SERVED_PARTY_APPLICANT)
                                ? bulkPrintDetails.getPrintDocs().stream()
                                .map(Element::getValue)
                                .collect(Collectors.toList()) : null
                        )
                        .respondentSoaPack(
                            partyIdAndType.get(partyId).equals(SERVED_PARTY_RESPONDENT)
                                ? (
                                bulkPrintDetails.getPrintDocs().stream()
                                    .map(Element::getValue)
                                    .collect(Collectors.toList())
                            ) : getUnservedRespondentDocumentList(serviceOfApplication)
                        )
                        .build();
                }
            );
        return citizenDocuments[0];
    }

    private List<CitizenDocuments> getCitizenDocuments(UserDetails userDetails,
                                                       CaseData caseData) {
        List<CitizenDocuments> citizenDocuments = new ArrayList<>();
        List<String> unReturnedCategoriesForUI = List.of(
            "safeguardingLetter",
            "section37Report",
            "section7Report",
            "16aRiskAssessment",
            "guardianReport",
            "specialGuardianshipReport",
            "otherDocs",
            "sec37Report",
            "localAuthorityOtherDoc",
            "emailsToCourtToRequestHearingsAdjourned",
            "publicFundingCertificates",
            "noticesOfActingDischarge",
            "requestForFASFormsToBeChanged",
            "witnessAvailability",
            "lettersOfComplaint",
            "SPIPReferralRequests",
            "homeOfficeDWPResponses",
            "internalCorrespondence",
            "importantInfoAboutAddressAndContact",
            "privacyNotice",
            "specialMeasures",
            "anyOtherDoc",
            "noticeOfHearing",
            "caseSummary"
        );

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

        Collections.sort(citizenDocuments, comparing(CitizenDocuments::getUploadedDate).reversed());

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
        Document existingDocument = null;
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
            .categoryId(quarantineDoc.getCategoryId())
            .uploadedBy(quarantineDoc.getUploadedBy())
            .uploadedDate(quarantineDoc.getDocumentUploadedDate())
            .document(existingDocument)
            .build();
    }

    private List<CitizenDocuments> getCitizenOrders(UserDetails userDetails, CaseData caseData) {

        HashMap<String, String> partyIdAndType = findPartyIdAndType(caseData, userDetails);
        if (partyIdAndType != null) {
            String partyId = partyIdAndType.entrySet().stream()
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

            log.info("*** partyId from idamId {}", partyId);
            return new ArrayList<>(getCitizenOrdersForParty(caseData, partyId));
        }
        return null;
    }

    private List<CitizenDocuments> getCitizenOrdersForParty(CaseData caseData,
                                                            String partyId) {
        return nullSafeCollection(caseData.getOrderCollection()).stream()
            .map(Element::getValue)
            .filter(order -> isOrderServedForParty(order, partyId))
            .map(this::createCitizenOrder)
            .toList();
    }

    private CitizenDocuments createCitizenOrder(OrderDetails order) {
        return CitizenDocuments.builder()
            .partyType(order.getOrderType())
            //.categoryId(quarantineDoc.getCategoryId())
            .uploadedBy(order.getOtherDetails().getCreatedBy())
            .uploadedDate(order.getDateCreated())
            .document(order.getOrderDocument())
            .documentWelsh(order.getOrderDocumentWelsh())
            .build();
    }

    private boolean isOrderServedForParty(OrderDetails order,
                                          String partyId) {
        return nullSafeCollection(order.getServeOrderDetails().getServedParties()).stream()
            .map(Element::getValue)
            .anyMatch(servedParty -> servedParty.getPartyId().equalsIgnoreCase(partyId));
    }

    private HashMap findPartyIdAndType(CaseData caseData,
                                       UserDetails userDetails) {
        HashMap<String, String> partyIdAndTypeMap = new HashMap<>();
        log.info("*** Inside find partyId method ***");
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            log.info("*** C100 case type");
            Optional<Element<PartyDetails>> applicantOptional = getParty(caseData.getApplicants(), userDetails);
            if (applicantOptional.isPresent()) {
                partyIdAndTypeMap.put(String.valueOf(applicantOptional.get().getId()), SERVED_PARTY_APPLICANT);
                return partyIdAndTypeMap;
            }

            Optional<Element<PartyDetails>> respondentOptional = getParty(caseData.getRespondents(), userDetails);
            if (respondentOptional.isPresent()) {
                partyIdAndTypeMap.put(String.valueOf(respondentOptional.get().getId()), SERVED_PARTY_RESPONDENT);
                return partyIdAndTypeMap;
            }

        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            log.info("*** FL401 case type");
            if (null != caseData.getApplicantsFL401().getUser()
                && userDetails.getId().equalsIgnoreCase(caseData.getApplicantsFL401().getUser().getIdamId())) {
                partyIdAndTypeMap.put(
                    String.valueOf(caseData.getApplicantsFL401().getPartyId()),
                    SERVED_PARTY_APPLICANT
                );
                return partyIdAndTypeMap;
            }
            if (null != caseData.getRespondentsFL401().getUser()
                && userDetails.getId().equalsIgnoreCase(caseData.getRespondentsFL401().getUser().getIdamId())) {
                partyIdAndTypeMap.put(
                    String.valueOf(caseData.getRespondentsFL401().getPartyId()),
                    SERVED_PARTY_RESPONDENT
                );
                return partyIdAndTypeMap;
            }
        }

        return null;
    }

    private Optional<Element<PartyDetails>> getParty(List<Element<PartyDetails>> parties,
                                                       UserDetails userDetails) {
        return nullSafeCollection(parties).stream()
            .filter(element -> null != element.getValue().getUser()
                && userDetails.getId().equalsIgnoreCase(element.getValue().getUser().getIdamId()))
            .findFirst();
    }
}
