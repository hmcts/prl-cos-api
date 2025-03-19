package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.amroles.InternalCaseworkerAmRolesEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.exception.NoCaseDataFoundException;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetailsMeta;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.CoverLetterMap;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServeOrderData;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.dto.payment.CitizenAwpPayment;
import uk.gov.hmcts.reform.prl.models.dto.payment.CreatePaymentRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.models.wa.WaMapper;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.logging.log4j.util.Strings.concat;
import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BULK_SCAN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ADMIN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ADMIN_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ID_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DD_MMM_YYYY_HH_MM_SS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ENGLISH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EUROPE_LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JUDGE_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGAL_ADVISER_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_BY_EMAIL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_BY_EMAIL_AND_POST;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_BY_POST;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.welsh;
import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.yes;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@Slf4j
public class CaseUtils {

    public static final String EUROPE_LONDON = "Europe/London";

    private CaseUtils() {

    }

    public static CaseData getCaseDataFromStartUpdateEventResponse(StartEventResponse startEventResponse, ObjectMapper objectMapper) {
        CaseDetails caseDetails = startEventResponse.getCaseDetails();
        if (caseDetails == null) {
            throw new NoCaseDataFoundException(HttpStatus.NOT_FOUND, "No case data found in the response");
        }
        return getCaseData(caseDetails, objectMapper);
    }

    public static CaseData getCaseData(CaseDetails caseDetails, ObjectMapper objectMapper) {
        State state = State.tryFromValue(caseDetails.getState()).orElse(null);
        CaseData.CaseDataBuilder caseDataBuilder = objectMapper.convertValue(caseDetails.getData(), CaseData.class)
            .toBuilder()
            .id(caseDetails.getId())
            .state(state)
            .createdDate(caseDetails.getCreatedDate())
            .lastModifiedDate(caseDetails.getLastModified());

        if ((State.SUBMITTED_PAID.equals(state)) && caseDataBuilder.build().getDateSubmitted() == null) {
            ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(EUROPE_LONDON));
            caseDataBuilder.dateSubmitted(DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime));
        }

        return caseDataBuilder.build();
    }

    public static String getStateLabel(State state) {
        return state != null ? state.getLabel() : "";
    }

    public static SelectTypeOfOrderEnum getSelectTypeOfOrder(CaseData caseData) {
        log.info("final order {}", caseData.getSelectTypeOfOrder());
        return caseData.getSelectTypeOfOrder();
    }

    public static String getCaseTypeOfApplication(CaseData caseData) {
        log.info("CaseTypeOfApplication ==> " + caseData.getCaseTypeOfApplication());
        return caseData.getCaseTypeOfApplication() != null
            ? caseData.getCaseTypeOfApplication() : caseData.getSelectedCaseTypeID();
    }



    public static String getOrderSelectionType(CaseData caseData) {
        String orderSelectionType = null;
        if (caseData.getManageOrdersOptions() != null) {
            orderSelectionType = caseData.getManageOrdersOptions().toString();
        } else if (caseData.getDraftOrderOptions() != null) {
            orderSelectionType = caseData.getDraftOrderOptions().toString();
        } else {
            orderSelectionType = "";
        }
        return orderSelectionType;
    }

    public static Long getRemainingDaysSubmitCase(CaseData caseData) {
        Long noOfDaysRemaining = null;
        if (CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())
            && State.AWAITING_SUBMISSION_TO_HMCTS.equals(caseData.getState())) {
            ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(EUROPE_LONDON));
            long noDaysPassed = Duration.between(caseData.getCreatedDate(), zonedDateTime).toDays();
            noOfDaysRemaining = PrlAppsConstants.CASE_SUBMISSION_THRESHOLD - noDaysPassed;
        }
        return noOfDaysRemaining;
    }

    /*
    Below method checks for Both if the case is a citizen case
    or the main applicant in the case is not represented.
    * **/
    public static boolean isCitizenCase(CaseData caseData) {
        return C100_CASE_TYPE.equals(CaseUtils.getCaseTypeOfApplication(caseData)) ? !hasLegalRepresentation(caseData.getApplicants().get(
            0).getValue()) : !hasLegalRepresentation(caseData.getApplicantsFL401());
    }

    public static Map<String, Object> getCourtDetails(Optional<CourtVenue> courtVenue, String baseLocationId) {
        Map<String, Object> caseDataMap = new HashMap<>();
        if (courtVenue.isPresent()) {
            String regionId = courtVenue.get().getRegionId();
            String courtName = courtVenue.get().getCourtName();
            String regionName = courtVenue.get().getRegion();
            String baseLocationName = courtVenue.get().getVenueName();
            caseDataMap.put("caseManagementLocation", CaseManagementLocation.builder()
                .region(regionId).baseLocation(baseLocationId).regionName(regionName)
                .baseLocationName(baseLocationName).build());
            caseDataMap.put(PrlAppsConstants.IS_CAFCASS, CaseUtils.cafcassFlag(regionId));
            caseDataMap.put(COURT_NAME_FIELD, courtName);
            caseDataMap.put(COURT_ID_FIELD, baseLocationId);

        }
        return caseDataMap;
    }

    public static YesOrNo cafcassFlag(String regionId) {
        YesOrNo cafcassFlag = YesOrNo.No; //wales
        if (regionId != null) {
            int intRegionId = Integer.parseInt(regionId);
            if (intRegionId > 0 && intRegionId < 7) {
                cafcassFlag = YesOrNo.Yes; //english regions
            }
        }
        log.info("is cafcass flag set ===> " + cafcassFlag);
        return cafcassFlag;
    }

    public static ServeOrderData getServeOrderData(CaseData caseData) {
        ServeOrderData serveOrderData;
        if (caseData.getServeOrderData() != null) {
            serveOrderData = caseData.getServeOrderData();
        } else {
            serveOrderData = ServeOrderData.builder().build();
        }
        return serveOrderData;
    }

    public static boolean hasLegalRepresentation(PartyDetails partyDetails) {
        return yes.equals(partyDetails.getDoTheyHaveLegalRepresentation()) || StringUtils.isNotEmpty(partyDetails.getSolicitorEmail());
    }

    public static Map<String, String> getApplicantsToNotify(CaseData caseData, UUID excludeId) {
        Map<String, String> applicantMap = new HashMap<>();
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            return nullSafeCollection(caseData.getApplicants()).stream()
                .filter(applicantElement -> !applicantElement.getId().equals(excludeId))
                .map(Element::getValue)
                .filter(applicant -> !CaseUtils.hasLegalRepresentation(applicant)
                    && Yes.equals(applicant.getCanYouProvideEmailAddress()))
                .collect(Collectors.toMap(
                    PartyDetails::getEmail,
                    party -> party.getFirstName() + EMPTY_SPACE_STRING + party.getLastName(),
                    (x, y) -> x
                ));
        } else if (null != caseData.getApplicantsFL401() && !hasLegalRepresentation(caseData.getApplicantsFL401())
            && Yes.equals(caseData.getApplicantsFL401().getCanYouProvideEmailAddress())
            && !excludeId.equals(caseData.getApplicantsFL401().getPartyId())) {
            applicantMap.put(
                caseData.getApplicantsFL401().getEmail(),
                caseData.getApplicantsFL401().getFirstName() + EMPTY_SPACE_STRING
                    + caseData.getApplicantsFL401().getLastName()
            );
        }
        return applicantMap;
    }

    public static Map<String, String> getRespondentsToNotify(CaseData caseData, UUID excludeId) {
        Map<String, String> respondentMap = new HashMap<>();
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            return nullSafeCollection(caseData.getRespondents()).stream()
                .filter(respondentElement -> !respondentElement.getId().equals(excludeId))
                .map(Element::getValue)
                .filter(respondent -> !CaseUtils.hasLegalRepresentation(respondent)
                    && Yes.equals(respondent.getCanYouProvideEmailAddress()))
                .collect(Collectors.toMap(
                    PartyDetails::getEmail,
                    party -> party.getFirstName() + EMPTY_SPACE_STRING + party.getLastName(),
                    (x, y) -> x
                ));
        } else if (null != caseData.getRespondentsFL401() && !hasLegalRepresentation(caseData.getRespondentsFL401())
            && Yes.equals(caseData.getRespondentsFL401().getCanYouProvideEmailAddress())
            && !caseData.getRespondentsFL401().getPartyId().equals(excludeId)) {
            respondentMap.put(
                caseData.getRespondentsFL401().getEmail(),
                caseData.getRespondentsFL401().getFirstName() + EMPTY_SPACE_STRING
                    + caseData.getRespondentsFL401().getLastName()
            );
        }
        return respondentMap;
    }

    public static Map<String, String> getOthersToNotify(CaseData caseData) {
        return nullSafeCollection(TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
                || TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())
                                      ? caseData.getOtherPartyInTheCaseRevised() : caseData.getOthersToNotify()).stream()
            .map(Element::getValue)
            .filter(other -> Yes.equals(other.getCanYouProvideEmailAddress()))
            .collect(Collectors.toMap(
                PartyDetails::getEmail,
                party -> party.getFirstName() + EMPTY_SPACE_STRING + party.getLastName(),
                (x, y) -> x
            ));
    }

    public static Map<String, String> getApplicantSolicitorsToNotify(CaseData caseData) {
        Map<String, String> applicantSolicitorMap = new HashMap<>();
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            return nullSafeCollection(caseData.getApplicants()).stream()
                .map(Element::getValue)
                .filter(CaseUtils::hasLegalRepresentation)
                .collect(Collectors.toMap(
                    PartyDetails::getSolicitorEmail,
                    applicant -> applicant.getRepresentativeFirstName() + EMPTY_SPACE_STRING + applicant.getRepresentativeLastName(),
                    (x, y) -> x
                ));
        } else if (null != caseData.getApplicantsFL401() && hasLegalRepresentation(caseData.getApplicantsFL401())) {
            applicantSolicitorMap.put(
                caseData.getApplicantsFL401().getSolicitorEmail(),
                caseData.getApplicantsFL401().getRepresentativeFirstName() + EMPTY_SPACE_STRING
                    + caseData.getApplicantsFL401().getRepresentativeLastName()
            );

            return applicantSolicitorMap;
        }
        return applicantSolicitorMap;
    }

    public static Map<String, String> getRespondentSolicitorsToNotify(CaseData caseData) {
        Map<String, String> respondentSolicitorMap = new HashMap<>();
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            return nullSafeCollection(caseData.getRespondents()).stream()
                .map(Element::getValue)
                .filter(CaseUtils::hasLegalRepresentation)
                .collect(Collectors.toMap(
                    PartyDetails::getSolicitorEmail,
                    respondent -> respondent.getRepresentativeFirstName() + EMPTY_SPACE_STRING + respondent.getRepresentativeLastName(),
                    (x, y) -> x
                ));
        } else if (null != caseData.getRespondentsFL401() && hasLegalRepresentation(caseData.getRespondentsFL401())) {
            respondentSolicitorMap.put(
                caseData.getRespondentsFL401().getSolicitorEmail(),
                caseData.getRespondentsFL401().getRepresentativeFirstName() + EMPTY_SPACE_STRING
                    + caseData.getRespondentsFL401().getRepresentativeLastName()
            );

            return respondentSolicitorMap;
        }
        return respondentSolicitorMap;
    }

    public static String getFormattedDatAndTime(LocalDateTime dateTime) {
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("EEEE, dd MMM, yyyy 'at' HH:mm a");
        return dateTime.format(dateTimeFormat);
    }

    public static boolean isC8Present(CaseData caseData) {
        log.info("Confidential check is happening");
        boolean isC8PresentInCase = false;
        if (caseData.getC8Document() != null || caseData.getC8FormDocumentsUploaded() != null) {
            isC8PresentInCase = true;
        }
        return isC8PresentInCase;
    }

    public static boolean isC8PresentCheckDraftAndFinal(CaseData caseData) {
        log.info("Confidential check is happening");
        boolean isC8Present = false;
        if (caseData.getC8DraftDocument() != null || caseData.getC8WelshDraftDocument() != null
            || caseData.getC8Document() != null || caseData.getC8WelshDocument() != null) {
            isC8Present = true;
        }
        return isC8Present;
    }

    public static void createCategorySubCategoryDynamicList(List<Category> categoryList,
                                                            List<DynamicListElement> dynamicListElementList,
                                                            List<String> categoriesToExclude) {
        nullSafeCollection(categoryList).forEach(category -> {
            if (isEmpty(category.getSubCategories())) {
                //Exclude quarantine categories
                if (!categoriesToExclude.contains(category.getCategoryId())) {
                    dynamicListElementList.add(
                        DynamicListElement.builder().code(category.getCategoryId())
                            .label(category.getCategoryName()).build()
                    );
                }
            } else {
                createCategorySubCategoryDynamicList(
                    category.getSubCategories(),
                    dynamicListElementList,
                    categoriesToExclude
                );
            }
        });
    }

    /**
     * Please do not use this method as it was created for external user and it has dependency on idam roles.
     * We are depending on AM roles for internal users
     * @param userDetails It takes User Details as input and returns roles for users
     * @return string
     */
    public static String getUserRole(UserDetails userDetails) {
        if (null == userDetails || isEmpty(userDetails.getRoles())) {
            throw new IllegalStateException("Unexpected user");
        }

        List<String> roles = userDetails.getRoles();
        if (roles.contains(SOLICITOR_ROLE)) {
            return SOLICITOR;
        } else if (roles.contains(COURT_ADMIN_ROLE)) {
            return COURT_ADMIN;
        } else if (roles.contains(JUDGE_ROLE)) {
            return COURT_STAFF;
        } else if (roles.contains(LEGAL_ADVISER_ROLE)) {
            return COURT_STAFF;
        } else if (roles.contains(CAFCASS)) {
            return CAFCASS;
        } else if (roles.contains(BULK_SCAN)) {
            return BULK_SCAN;
        } else if (roles.contains(CITIZEN_ROLE)) {
            return CITIZEN;
        }

        return CAFCASS;
    }

    public static void removeTemporaryFields(Map<String, Object> caseDataMap, String... fields) {
        for (String field : fields) {
            caseDataMap.remove(field);
        }
    }

    public static Document convertDocType(uk.gov.hmcts.reform.ccd.client.model.Document document) {
        return Document.builder().documentUrl(document.getDocumentURL())
            .documentBinaryUrl(document.getDocumentBinaryURL())
            .documentFileName(document.getDocumentFilename())
            .build();
    }

    public static boolean unServedPacksPresent(CaseData caseData) {
        boolean arePacksPresent = false;
        if (caseData.getServiceOfApplication() != null && ((caseData.getServiceOfApplication().getUnServedApplicantPack() != null
            && caseData.getServiceOfApplication().getUnServedApplicantPack().getPackDocument() != null)
            || (caseData.getServiceOfApplication().getUnServedRespondentPack() != null
            && caseData.getServiceOfApplication().getUnServedRespondentPack().getPackDocument() != null)
            || (caseData.getServiceOfApplication().getUnServedOthersPack() != null
            && caseData.getServiceOfApplication().getUnServedOthersPack().getPackDocument() != null)
            || (caseData.getServiceOfApplication().getUnServedLaPack() != null
            && caseData.getServiceOfApplication().getUnServedLaPack().getPackDocument() != null)
            || (caseData.getServiceOfApplication().getUnServedCafcassCymruPack() != null
            && caseData.getServiceOfApplication().getUnServedCafcassCymruPack().getServedPartyEmail() != null))) {
            arePacksPresent = true;
        }
        return arePacksPresent;
    }

    public static String convertLocalDateTimeToAmOrPmTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
        return localDateTime.format(formatter);
    }

    public static String getPartyFromPartyId(String partyId, CaseData caseData) {
        String partyName = "";
        if (C100_CASE_TYPE.equalsIgnoreCase(getCaseTypeOfApplication(caseData))) {
            partyName = returnMatchingPartyIfAny(caseData.getApplicants(), partyId);
            if (partyName.isBlank()) {
                partyName = returnMatchingPartyIfAny(caseData.getRespondents(), partyId);
            }
            return partyName;
        } else {
            if (partyId.equalsIgnoreCase(String.valueOf(caseData.getApplicantsFL401().getPartyId()))) {
                partyName = caseData.getApplicantsFL401().getLabelForDynamicList();
            } else if (partyId.equalsIgnoreCase(String.valueOf(caseData.getApplicantsFL401().getSolicitorPartyId()))) {
                partyName = caseData.getApplicantsFL401().getRepresentativeFullName();
            } else if (partyId.equalsIgnoreCase(String.valueOf(caseData.getRespondentsFL401().getPartyId()))) {
                partyName = caseData.getRespondentsFL401().getLabelForDynamicList();
            } else if (partyId.equalsIgnoreCase(String.valueOf(caseData.getRespondentsFL401().getSolicitorPartyId()))) {
                partyName = caseData.getRespondentsFL401().getRepresentativeFullName();
            }
            return partyName;
        }
    }

    private static String returnMatchingPartyIfAny(List<Element<PartyDetails>> partyDetails, String partyId) {
        for (Element<PartyDetails> party : partyDetails) {
            if (partyId.equalsIgnoreCase(String.valueOf(party.getId()))) {
                return party.getValue().getLabelForDynamicList();
            } else if (partyId.equalsIgnoreCase(String.valueOf(party.getValue().getSolicitorPartyId()))) {
                return party.getValue().getRepresentativeFullName();
            }
        }
        return "";
    }

    public static LocalDateTime convertUtcToBst(LocalDateTime hearingStartDateTime) {
        ZonedDateTime givenZonedTime = hearingStartDateTime.atZone(ZoneId.of("UTC"));
        return givenZonedTime.withZoneSameInstant(ZoneId.of(EUROPE_LONDON)).toLocalDateTime();
    }

    public static boolean isCitizenAccessEnabled(PartyDetails party) {
        return party != null && party.getUser() != null
            && party.getUser().getIdamId() != null;
    }

    public static String getDynamicMultiSelectedValueLabels(List<DynamicMultiselectListElement> dynamicMultiselectListElements) {
        return nullSafeCollection(dynamicMultiselectListElements).stream()
            .map(DynamicMultiselectListElement::getLabel)
            .collect(Collectors.joining(","));
    }

    public static CaseInvite getCaseInvite(UUID partyId, List<Element<CaseInvite>> caseInvites) {
        if (CollectionUtils.isNotEmpty(caseInvites)) {
            Optional<Element<CaseInvite>> caseInvite = caseInvites.stream()
                .filter(caseInviteElement -> Optional.ofNullable(caseInviteElement.getValue().getPartyId()).isPresent())
                .filter(caseInviteElement -> partyId.equals(caseInviteElement.getValue().getPartyId())).findFirst();
            if (caseInvite.isPresent()) {
                return caseInvite.map(Element::getValue).orElse(null);
            }
        }
        return null;
    }

    public static void setCaseState(CallbackRequest callbackRequest, Map<String, Object> caseDataUpdated) {
        log.info("State from callbackRequest " + callbackRequest.getCaseDetails().getState());
        State state = State.tryFromValue(callbackRequest.getCaseDetails().getState()).orElse(null);
        if (null != state) {
            log.info("State " + state.getLabel());
            caseDataUpdated.put("caseStatus", CaseStatus.builder().state(state.getLabel()).build());
        }
    }

    public static Optional<PartyDetailsMeta> getPartyDetailsMeta(String partyId, String caseType, CaseData caseData) {
        return C100_CASE_TYPE.equalsIgnoreCase(caseType)
            ? getC100PartyDetailsMeta(partyId, caseData)
            : getFL401PartyDetailsMeta(partyId, caseData);
    }

    private static int findPartyIndex(String partyId, List<Element<PartyDetails>> parties) {
        return IntStream.range(0, parties.size())
            .filter(index -> (ObjectUtils.isNotEmpty(parties.get(index))
                && ObjectUtils.isNotEmpty(parties.get(index).getValue())
                && ObjectUtils.isNotEmpty(parties.get(index).getValue().getUser())
                && ObjectUtils.isNotEmpty(parties.get(index).getValue().getUser().getIdamId())
                && parties.get(index).getValue().getUser().getIdamId().equals(
                partyId)))
            .findFirst()
            .orElse(-1);
    }

    private static Optional<PartyDetailsMeta> getC100PartyDetailsMeta(String partyId, CaseData caseData) {
        Optional<PartyDetailsMeta> partyDetailsMeta = Optional.empty();
        if (CollectionUtils.isNotEmpty(caseData.getApplicants())) {
            int partyIndex = findPartyIndex(partyId, caseData.getApplicants());

            if (partyIndex > -1) {
                partyDetailsMeta = Optional.ofNullable(PartyDetailsMeta
                                                           .builder()
                                                           .partyType(PartyEnum.applicant)
                                                           .partyIndex(partyIndex)
                                                           .partyDetails(caseData.getApplicants().get(partyIndex).getValue())
                                                           .build());

                return partyDetailsMeta;
            }
        }

        if (CollectionUtils.isNotEmpty(caseData.getRespondents())) {
            int partyIndex = findPartyIndex(partyId, caseData.getRespondents());

            if (partyIndex > -1) {
                partyDetailsMeta = Optional.ofNullable(PartyDetailsMeta
                                                           .builder()
                                                           .partyType(PartyEnum.respondent)
                                                           .partyIndex(partyIndex)
                                                           .partyDetails(caseData.getRespondents().get(partyIndex).getValue())
                                                           .build());
                return partyDetailsMeta;
            }
        }
        return partyDetailsMeta;
    }

    private static Optional<PartyDetailsMeta> getFL401PartyDetailsMeta(String partyId, CaseData caseData) {
        Optional<PartyDetailsMeta> partyDetailsMeta = Optional.empty();
        if (ObjectUtils.isNotEmpty(caseData.getApplicantsFL401())
            && ObjectUtils.isNotEmpty(caseData.getApplicantsFL401().getUser())
            && ObjectUtils.isNotEmpty(caseData.getApplicantsFL401().getUser().getIdamId())
            && caseData.getApplicantsFL401().getUser().getIdamId().equals(partyId)) {
            partyDetailsMeta = Optional.ofNullable(PartyDetailsMeta
                                                       .builder()
                                                       .partyType(PartyEnum.applicant)
                                                       .partyIndex(0)
                                                       .partyDetails(caseData.getApplicantsFL401())
                                                       .build());
            return partyDetailsMeta;
        }

        if (ObjectUtils.isNotEmpty(caseData.getRespondentsFL401())
            && ObjectUtils.isNotEmpty(caseData.getRespondentsFL401().getUser())
            && ObjectUtils.isNotEmpty(caseData.getRespondentsFL401().getUser().getIdamId())
            && caseData.getRespondentsFL401().getUser().getIdamId().equals(partyId)) {
            partyDetailsMeta = Optional.ofNullable(PartyDetailsMeta
                                                       .builder()
                                                       .partyType(PartyEnum.respondent)
                                                       .partyIndex(0)
                                                       .partyDetails(caseData.getRespondentsFL401())
                                                       .build());
            return partyDetailsMeta;
        }

        return partyDetailsMeta;
    }

    public static List<String> getPartyNameList(List<Element<PartyDetails>> parties) {
        List<String> applicantList = new ArrayList<>();
        if (isNotEmpty(parties)) {
            applicantList = parties.stream()
                .map(Element::getValue)
                .map(PartyDetails::getLabelForDynamicList)
                .toList();
        }
        return applicantList;
    }

    public static List<String> getPartyNameList(boolean isApplicant,
                                                List<Element<PartyDetails>> parties) {
        List<String> partyList = new ArrayList<>();
        if (isNotEmpty(parties)) {
            IncrementalInteger i = new IncrementalInteger(1);
            partyList = parties.stream()
                .map(Element::getValue)
                .map(party -> party.getLabelForDynamicList() + (isApplicant
                    ? " (Applicant " + i.getAndIncrement() + ")"
                    : " (Respondent " + i.getAndIncrement() + ")"))
                .toList();
        }
        return partyList;
    }

    public static List<String> getApplicantSolicitorNameList(List<Element<PartyDetails>> parties) {
        List<String> applicantSolicitorList = new ArrayList<>();
        if (isNotEmpty(parties)) {
            applicantSolicitorList = parties.stream()
                .map(Element::getValue)
                .map(element -> element.getRepresentativeFirstName() + " " + element.getRepresentativeLastName())
                .toList();
        }
        return applicantSolicitorList;
    }

    public static List<String> getRespondentSolicitorNameList(List<Element<PartyDetails>> parties) {
        List<String> respondentSolicitorList = new ArrayList<>();
        if (isNotEmpty(parties)) {
            respondentSolicitorList = parties.stream()
                .map(Element::getValue)
                .filter(partyDetails -> YesNoDontKnow.yes.equals(partyDetails.getDoTheyHaveLegalRepresentation()))
                .map(element -> element.getRepresentativeFirstName() + " " + element.getRepresentativeLastName())
                .toList();
        }
        return respondentSolicitorList;
    }

    public static String getFL401SolicitorName(PartyDetails party) {
        if (null != party
            && isNotBlank(party.getRepresentativeFirstName())
            && isNotBlank(party.getRepresentativeLastName())) {
            return concat(
                party.getRepresentativeFirstName(),
                concat(" ", party.getRepresentativeLastName())
            );
        }
        return null;
    }

    public static String getApplicant(CaseData caseData) {
        if (!C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            return String.format(PrlAppsConstants.FORMAT, caseData.getApplicantsFL401().getFirstName(),
                                 caseData.getApplicantsFL401().getLastName()
            );
        }
        return null;
    }

    public static String getApplicantReference(CaseData caseData) {
        if (!C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            return caseData.getApplicantsFL401().getSolicitorReference();
        }
        return null;
    }

    public static String getRespondent(CaseData caseData) {
        if (!C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            return String.format(
                PrlAppsConstants.FORMAT,
                caseData.getRespondentsFL401().getFirstName(),
                caseData.getRespondentsFL401().getLastName()
            );
        }
        return null;
    }

    public static List<Element<String>> getPartyIdList(List<Element<PartyDetails>> parties) {
        return parties.stream().map(Element::getId).map(uuid -> element(uuid.toString())).toList();
    }

    public static List<String> getPartyIdList(String caseTypeOfApplication,
                                              List<Element<PartyDetails>> parties,
                                              PartyDetails fl401Party) {
        //FL401
        if (FL401_CASE_TYPE.equalsIgnoreCase(caseTypeOfApplication)) {
            return null != fl401Party
                ? List.of(fl401Party.getPartyId().toString())
                : Collections.emptyList();
        }
        //C100
        return CollectionUtils.isNotEmpty(parties)
            ? parties.stream().map(Element::getId).map(Objects::toString).map(String::trim).toList()
            : Collections.emptyList();
    }

    public static String getPartyIdListAsString(List<Element<PartyDetails>> parties) {
        return String.join(",", parties.stream().map(Element::getId).map(UUID::toString).toList());
    }

    public static boolean isCaseWithoutNotice(CaseData caseData) {
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
            && Yes.equals(caseData.getDoYouNeedAWithoutNoticeHearing())) {
            return true;
        } else if (null != caseData.getOrderWithoutGivingNoticeToRespondent()) {
            return YesOrNo.Yes.equals(caseData.getOrderWithoutGivingNoticeToRespondent().getOrderWithoutGivingNotice());
        }
        return false;
    }

    public static String getModeOfService(List<Element<EmailNotificationDetails>> emailNotificationDetails,
                                    List<Element<BulkPrintDetails>> bulkPrintDetails) {
        String temp = null;
        if (null != emailNotificationDetails && !emailNotificationDetails.isEmpty()) {
            temp = SOA_BY_EMAIL;
        }
        if (null != bulkPrintDetails && !bulkPrintDetails.isEmpty()) {
            if (null != temp) {
                temp = SOA_BY_EMAIL_AND_POST;
            } else {
                temp = SOA_BY_POST;
            }
        }
        return temp;
    }

    public static List<Element<PartyDetails>> getOthersToNotifyInCase(CaseData caseData) {
        return TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
                || TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())
            ? caseData.getOtherPartyInTheCaseRevised() : caseData.getOthersToNotify();
    }

    public static boolean isApplyOrderWithoutGivingNoticeToRespondent(CaseData caseData) {
        return ObjectUtils.isNotEmpty(caseData.getOrderWithoutGivingNoticeToRespondent())
            && YesOrNo.Yes.equals(caseData.getOrderWithoutGivingNoticeToRespondent().getOrderWithoutGivingNotice());
    }

    public static boolean checkIfAddressIsChanged(PartyDetails currentParty, PartyDetails updatedParty) {
        log.info("verifying address change");
        Address currentAddress = currentParty.getAddress();
        Address previousAddress = ObjectUtils.isNotEmpty(updatedParty.getAddress())
            ? updatedParty.getAddress() : Address.builder().build();
        boolean flag = currentAddress != null
            && (!StringUtils.equals(currentAddress.getAddressLine1(), previousAddress.getAddressLine1())
            || !StringUtils.equals(currentAddress.getAddressLine2(),previousAddress.getAddressLine2())
            || !StringUtils.equals(currentAddress.getAddressLine3(),previousAddress.getAddressLine3())
            || !StringUtils.equals(currentAddress.getCountry(),previousAddress.getCountry())
            || !StringUtils.equals(currentAddress.getCounty(),previousAddress.getCounty())
            || !StringUtils.equals(currentAddress.getPostCode(),previousAddress.getPostCode())
            || !StringUtils.equals(currentAddress.getPostTown(),previousAddress.getPostTown())
            || !isConfidentialityRemainsSame(currentParty.getIsAddressConfidential(),
                                             updatedParty.getIsAddressConfidential()))
            && (StringUtils.isNotEmpty(currentAddress.getAddressLine1())
                || StringUtils.isNotEmpty(previousAddress.getAddressLine1()));
        log.info("checkIfAddressIsChanged ===>" + flag);
        return flag;
    }

    public static boolean isEmailAddressChanged(PartyDetails currentParty, PartyDetails updatedParty) {
        log.info("Verify email address change");
        boolean flag = (!StringUtils.equals(currentParty.getEmail(),updatedParty.getEmail())
            || !isConfidentialityRemainsSame(currentParty.getIsEmailAddressConfidential(),
                                             updatedParty.getIsEmailAddressConfidential()))
            && (StringUtils.isNotEmpty(currentParty.getEmail())
                || StringUtils.isNotEmpty(updatedParty.getEmail()));
        log.info("isEmailAddressChanged ===>" + flag);
        return flag;
    }

    public static boolean isPhoneNumberChanged(PartyDetails currentParty, PartyDetails updatedParty) {
        log.info("verifying phone number change");
        boolean flag = (!StringUtils.equals(currentParty.getPhoneNumber(),updatedParty.getPhoneNumber())
            || !isConfidentialityRemainsSame(currentParty.getIsPhoneNumberConfidential(),
                                             updatedParty.getIsPhoneNumberConfidential()))
            && (StringUtils.isNotEmpty(currentParty.getPhoneNumber())
                || StringUtils.isNotEmpty(updatedParty.getPhoneNumber()));
        log.info("isPhoneNumberChanged ===>" + flag);
        return flag;
    }

    private static boolean isConfidentialityRemainsSame(YesOrNo newConfidentiality, YesOrNo oldConfidentiality) {
        log.info("inside isConfidentialityRemainsSame");
        if (ObjectUtils.isEmpty(oldConfidentiality)
            && ObjectUtils.isEmpty(newConfidentiality)) {
            return true;
        } else if (ObjectUtils.isEmpty(oldConfidentiality)
            && ObjectUtils.isNotEmpty(newConfidentiality)) {
            return No.equals(newConfidentiality);
        } else {
            return newConfidentiality.equals(oldConfidentiality);
        }
    }

    public static String getLanguageRequirements(CaseData caseData) {
        if (YesOrNo.Yes.equals(caseData.getWelshLanguageRequirement())) {
            if ((welsh.equals(caseData.getWelshLanguageRequirementApplication())
                && Yes.equals(caseData.getWelshLanguageRequirementApplicationNeedEnglish()))
                || (english.equals(caseData.getWelshLanguageRequirementApplication())
                && Yes.equals(caseData.getLanguageRequirementApplicationNeedWelsh()))) {
                return "Both";
            }
            return LanguagePreference.getLanguagePreference(caseData).getDisplayedValue();
        }
        return "English";
    }

    public static List<String> mapAmUserRolesToIdamRoles(RoleAssignmentServiceResponse roleAssignmentServiceResponse,
                                                   String authorisation,
                                                   UserDetails userDetails) {
        //This would check for user roles from AM for Judge/Legal advisor/Court admin
        //and then return the corresponding idam role base on that
        List<String> roles = roleAssignmentServiceResponse.getRoleAssignmentResponse().stream().map(
            RoleAssignmentResponse::getRoleName).toList();

        String idamRole;
        if (roles.stream().anyMatch(InternalCaseworkerAmRolesEnum.JUDGE.getRoles()::contains)) {
            idamRole = Roles.JUDGE.getValue();
        } else if (roles.stream().anyMatch(InternalCaseworkerAmRolesEnum.LEGAL_ADVISER.getRoles()::contains)) {
            idamRole = Roles.LEGAL_ADVISER.getValue();
        } else if (roles.stream().anyMatch(InternalCaseworkerAmRolesEnum.COURT_ADMIN.getRoles()::contains)) {
            idamRole = Roles.COURT_ADMIN.getValue();
        } else if (userDetails.getRoles().contains(Roles.SOLICITOR.getValue())) {
            idamRole = Roles.SOLICITOR.getValue();
        } else if (userDetails.getRoles().contains(Roles.CITIZEN.getValue())) {
            idamRole = Roles.CITIZEN.getValue();
        } else if (userDetails.getRoles().contains(Roles.SYSTEM_UPDATE.getValue())) {
            idamRole = Roles.SYSTEM_UPDATE.getValue();
        } else {
            idamRole = "";
        }

        roles = new ArrayList<>(Collections.singleton(idamRole));
        return roles;
    }

    public static boolean hasDashboardAccess(Element<PartyDetails> party) {
        return null != party.getValue()
            && null != party.getValue().getUser()
            && null != party.getValue().getUser().getIdamId();
    }

    public static String getApplicantNameForDaOrderSelectedForCaCase(CaseData caseData) {
        PartyDetails applicant1 = C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
            ? caseData.getApplicants().get(0).getValue() : caseData.getApplicantsFL401();
        return String.format(PrlAppsConstants.FORMAT, applicant1.getFirstName(),
                             applicant1.getLastName()
        );

    }

    public static String getApplicantReferenceForDaOrderSelectedForCaCase(CaseData caseData) {
        PartyDetails applicant1 = C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
            ? caseData.getApplicants().get(0).getValue() : caseData.getApplicantsFL401();
        return applicant1.getSolicitorReference();
    }

    public static String getRespondentForDaOrderSelectedForCaCase(CaseData caseData) {
        PartyDetails respondent1 = C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
            ? caseData.getRespondents().get(0).getValue() : caseData.getRespondentsFL401();
        return String.format(
            PrlAppsConstants.FORMAT, respondent1.getFirstName(),
            respondent1.getLastName()
        );
    }

    public static LocalDate getRespondentDobForDaOrderSelectedForCaCase(CaseData caseData) {
        PartyDetails respondent1 = C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
            ? caseData.getRespondents().get(0).getValue() : caseData.getRespondentsFL401();
        if (ofNullable(respondent1.getDateOfBirth()).isPresent()) {
            return respondent1.getDateOfBirth();
        }
        return null;
    }

    public static WaMapper getWaMapper(String clientContext) {
        if (clientContext != null) {
            log.info("clientContext is present");
            byte[] decodedBytes = Base64.getDecoder().decode(clientContext);
            String decodedString = new String(decodedBytes);
            try {
                return new ObjectMapper().readValue(decodedString, WaMapper.class);
            } catch (Exception ex) {
                log.error("Exception while parsing the Client-Context {}", ex.getMessage());
            }
        }
        return null;
    }

    public static String getDraftOrderId(WaMapper waMapper) {
        if (null != waMapper) {
            if (null != waMapper.getClientContext().getUserTask().getTaskData().getAdditionalProperties()) {
                return waMapper.getClientContext().getUserTask().getTaskData().getAdditionalProperties().getOrderId();
            }
        }
        return null;
    }

    public static DraftOrder getDraftOrderFromCollectionId(List<Element<DraftOrder>> draftOrderCollection, UUID draftOrderId) {
        if (CollectionUtils.isNotEmpty(draftOrderCollection)) {
            return draftOrderCollection.stream()
                .filter(element -> element.getId().equals(draftOrderId))
                .map(Element::getValue)
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("Could not find order"));
        }
        return null;
    }

    public static Optional<Element<PartyDetails>> getParty(String code, List<Element<PartyDetails>> parties) {
        Optional<Element<PartyDetails>> party = Optional.empty();
        if (CollectionUtils.isNotEmpty(parties)) {
            party = parties.stream()
                .filter(element -> code.equalsIgnoreCase(String.valueOf(element.getId()))).findFirst();
        }
        return party;
    }

    public static List<DynamicMultiselectListElement> getSelectedApplicantsOrRespondentsForC100(List<Element<PartyDetails>> applicantsOrRespondents,
                                                                                                List<DynamicMultiselectListElement> value) {
        return value.stream().filter(element -> applicantsOrRespondents.stream().anyMatch(party -> party.getId().toString().equals(
            element.getCode()))).collect(
            Collectors.toList());
    }

    public static List<DynamicMultiselectListElement> getSelectedApplicantsOrRespondentsForFL401(PartyDetails applicantsOrRespondent,
                                                                                                 List<DynamicMultiselectListElement> value) {
        return value.stream().filter(element -> applicantsOrRespondent.getPartyId().toString().equals(
            element.getCode())).collect(Collectors.toList());
    }

    public static String formatAddress(Address address) {
        if (ObjectUtils.isNotEmpty(address)) {
            List<String> addressLines = new ArrayList<>();
            getAddressLines(address, addressLines);
            return String.join("\n", addressLines);
        } else {
            return EMPTY_STRING;
        }
    }

    private static void getAddressLines(Address address, List<String> addressLines) {
        if (address.getAddressLine1() != null && StringUtils.isNotEmpty(address.getAddressLine1().trim())) {
            addressLines.add(address.getAddressLine1());
        }
        if (address.getAddressLine2() != null && StringUtils.isNotEmpty(address.getAddressLine2().trim())) {
            addressLines.add(address.getAddressLine2());
        }
        if (address.getAddressLine3() != null && StringUtils.isNotEmpty(address.getAddressLine3().trim())) {
            addressLines.add(address.getAddressLine3());
        }
        if (address.getPostTown() != null && StringUtils.isNotEmpty(address.getPostTown().trim())) {
            addressLines.add(address.getPostTown());
        }
        if (address.getPostCode() != null && StringUtils.isNotEmpty(address.getPostCode().trim())) {
            addressLines.add(address.getPostCode());
        }
    }

    public static Set<String> getStringsSplitByDelimiter(String partyIds,
                                                         String delimiter) {
        return null != partyIds
            ? Arrays.stream(partyIds.trim().split(delimiter)).map(String::trim).collect(Collectors.toSet())
            : Collections.emptySet();
    }

    public static String getCurrentDate() {
        return DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
            .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE)));
    }

    public static List<Document> getCoverLettersForParty(UUID partyId, List<Element<CoverLetterMap>> coverLetters) {
        if (CollectionUtils.isEmpty(coverLetters)) {
            return Collections.emptyList();
        }
        for (Element<CoverLetterMap> coverLetterMapElement: coverLetters) {
            if (partyId.equals(coverLetterMapElement.getId())) {
                return getCoverLettersFrom(coverLetterMapElement.getValue().getCoverLetters());
            }
        }
        return Collections.emptyList();
    }

    private static List<Document> getCoverLettersFrom(List<Element<Document>> coverLettermap) {
        return CollectionUtils.isNotEmpty(coverLettermap) ? coverLettermap.stream().map(Element::getValue).toList()
            : Collections.emptyList();
    }

    public static void mapCoverLetterToTheParty(UUID partyId, List<Element<CoverLetterMap>> coverLettersMap, List<Document> coverLetters) {
        boolean partyIdExists = false;
        for (Element<CoverLetterMap> coverLetterMapElement: coverLettersMap) {
            if (partyId.equals(coverLetterMapElement.getId())) {
                partyIdExists = true;
                if (CollectionUtils.isNotEmpty(coverLetterMapElement.getValue().getCoverLetters())) {
                    coverLetterMapElement.getValue().getCoverLetters().addAll(wrapElements(coverLetters));
                } else {
                    coverLetterMapElement.getValue().setCoverLetters(wrapElements(coverLetters));
                }
            }
        }
        if (!partyIdExists) {
            coverLettersMap.add(element(partyId, CoverLetterMap.builder()
                                    .coverLetters(wrapElements(coverLetters))
                                    .build()));
        }
    }

    public static Optional<Element<CitizenAwpPayment>> getCitizenAwpPaymentIfPresent(List<Element<CitizenAwpPayment>> citizenAwpPayments,
                                                                                     CreatePaymentRequest createPaymentRequest) {
        if (isNotEmpty(citizenAwpPayments)) {
            return citizenAwpPayments.stream()
                .filter(awpPaymentElement ->
                            isCitizenAwpPaymentPresent(awpPaymentElement.getValue(), createPaymentRequest))
                .findFirst();
        }
        return Optional.empty();
    }

    public static boolean isCitizenAwpPaymentPresent(CitizenAwpPayment citizenAwpPayment,
                                                     CreatePaymentRequest createPaymentRequest) {
        return citizenAwpPayment.getAwpType().equals(createPaymentRequest.getAwpType())
            && citizenAwpPayment.getPartType().equals(createPaymentRequest.getPartyType())
            && null != createPaymentRequest.getFeeType()
            && citizenAwpPayment.getFeeType().equals(createPaymentRequest.getFeeType().name());
    }

    public static List<String> getSelectedPartyIds(String caseTypeOfApplication,
                                                   List<Element<PartyDetails>> parties,
                                                   PartyDetails fl401Party,
                                                   List<DynamicMultiselectListElement> selectedParties) {
        //FL401
        if (FL401_CASE_TYPE.equalsIgnoreCase(caseTypeOfApplication)) {
            return selectedParties.stream()
                .map(DynamicMultiselectListElement::getCode)
                .filter(code -> fl401Party.getPartyId().toString().equals(code))
                .toList();
        }
        //C100
        return nullSafeCollection(parties)
            .stream()
            .map(Element::getId)
            .filter(id ->
                        selectedParties.stream().anyMatch(
                            party -> id.toString().equals(party.getCode()))
            )
            .map(Objects::toString)
            .toList();
    }

    public static PartyDetails getOtherPerson(String id, CaseData caseData) {
        List<Element<PartyDetails>> otherPartiesToNotify = TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
            || TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())
            ? caseData.getOtherPartyInTheCaseRevised()
            : caseData.getOthersToNotify();
        if (null != otherPartiesToNotify) {
            Optional<Element<PartyDetails>> otherPerson = otherPartiesToNotify.stream()
                .filter(element -> element.getId().toString().equalsIgnoreCase(id))
                .findFirst();
            if (otherPerson.isPresent()) {
                return otherPerson.get().getValue();
            }
        }
        return null;
    }

    public static String getLanguage(String clientContextString) {

        WaMapper waMapper = null;

        if (clientContextString != null) {
            byte[] decodedBytes = Base64.getDecoder().decode(clientContextString);
            String decodedString = new String(decodedBytes);
            try {
                waMapper = new ObjectMapper().readValue(decodedString, WaMapper.class);
            } catch (Exception ex) {
                log.error("Exception while parsing the Client-Context {}", ex.getMessage());
            }
        }

        if (null != waMapper
            && null != waMapper.getClientContext()
            && null != waMapper.getClientContext().getUserLanguage()) {
            return waMapper.getClientContext().getUserLanguage().getLanguage();
        }

        return ENGLISH;
    }

    public static String getContactInstructions(PartyDetails applicantsFL401) {
        return null != applicantsFL401.getApplicantContactInstructions() ? applicantsFL401.getApplicantContactInstructions() : null;
    }
}
