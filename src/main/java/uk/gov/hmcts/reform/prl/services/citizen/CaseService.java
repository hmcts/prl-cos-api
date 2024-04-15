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
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
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
import uk.gov.hmcts.reform.prl.models.dto.citizen.UiCitizenCaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.repositories.CaseRepository;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.cafcass.HearingService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_SUBMIT_WITH_HWF;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_UPDATE;
import static uk.gov.hmcts.reform.prl.models.dto.citizen.CitizenDocumentsManagement.unReturnedCategoriesForUI;
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
        caseDataWithHearingResponse = caseDataWithHearingResponse.toBuilder()
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
            case PREPARE_FOR_HEARING_CONDUCT_HEARING,
                 DECISION_OUTCOME: {
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

        emailNotificationDetailsList.stream()
            .map(Element::getValue)
            .sorted(comparing(EmailNotificationDetails::getTimeStamp).reversed())
            .filter(emailNotificationDetails -> Arrays.asList(
                emailNotificationDetails.getPartyIds().split("\\s*,\\s*")).contains(partyIdAndType.get(PARTY_ID)))
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

        bulkPrintDetailsList.stream()
            .map(Element::getValue)
            .sorted(comparing(BulkPrintDetails::getTimeStamp).reversed())
            .filter(bulkPrintDetails -> Arrays.asList(
                bulkPrintDetails.getPartyIds().split("\\s*,\\s*")).contains(partyIdAndType.get(PARTY_ID)))
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
            .partyName(quarantineDoc.getUploadedBy())
            .categoryId(quarantineDoc.getCategoryId())
            .uploadedBy(quarantineDoc.getUploadedBy())
            .uploadedDate(quarantineDoc.getDocumentUploadedDate())
            .document(existingDocument)
            .build();
    }

    private List<CitizenDocuments> getCitizenOrders(UserDetails userDetails, CaseData caseData) {
        List<CitizenDocuments> citizenDocuments = new ArrayList<>();
        HashMap<String, String> partyIdAndType = findPartyIdAndType(caseData, userDetails);

        if (partyIdAndType != null) {
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
