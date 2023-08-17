package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServeOrderData;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ADMIN_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ID_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JUDGE_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGAL_ADVISER_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.yes;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

@Slf4j
public class CaseUtils {
    private CaseUtils() {

    }

    public static CaseData getCaseDataFromStartUpdateEventResponse(StartEventResponse startEventResponse, ObjectMapper objectMapper) {
        CaseDetails caseDetails = startEventResponse.getCaseDetails();
        if (caseDetails == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
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

        if ((State.SUBMITTED_PAID.equals(state))) {
            ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
            caseDataBuilder.dateSubmitted(DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime));
        }
        return caseDataBuilder.build();
    }

    public static String getStateLabel(State state) {
        return state != null ? state.getLabel() : "";
    }

    public static SelectTypeOfOrderEnum getSelectTypeOfOrder(CaseData caseData) {
        return caseData.getSelectTypeOfOrder();
    }

    public static String getCaseTypeOfApplication(CaseData caseData) {
        log.info("Manage order CaseTypeOfApplication ==> " + caseData.getCaseTypeOfApplication());
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
            ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
            Long noDaysPassed = Duration.between(caseData.getCreatedDate(), zonedDateTime).toDays();
            noOfDaysRemaining = PrlAppsConstants.CASE_SUBMISSION_THRESHOLD - noDaysPassed;
        }
        return noOfDaysRemaining;
    }

    public static Map<String, Object> getCourtDetails(Optional<CourtVenue> courtVenue, String baseLocationId) {
        Map<String, Object> caseDataMap = new HashMap<>();
        if (courtVenue.isPresent()) {
            String regionId = courtVenue.get().getRegionId();
            String courtName = courtVenue.get().getCourtName();
            String regionName = courtVenue.get().getRegion();
            String baseLocationName = courtVenue.get().getSiteName();
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
        log.info("regionId ===> " + regionId);
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
        return yes.equals(partyDetails.getDoTheyHaveLegalRepresentation());
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
            && !excludeId.equals(caseData.getRespondentsFL401().getPartyId())) {
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
        if (caseData.getC8Document() != null || caseData.getC8FormDocumentsUploaded() != null) {
            return true;
        }
        return false;
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

    public static String getUserRole(UserDetails userDetails) {
        if (null == userDetails || isEmpty(userDetails.getRoles())) {
            throw new IllegalStateException("Unexpected user");
        }

        List<String> roles = userDetails.getRoles();
        if (roles.contains(SOLICITOR_ROLE)) {
            return SOLICITOR;
        } else if (roles.contains(COURT_ADMIN_ROLE)) {
            return COURT_STAFF;
        } else if (roles.contains(JUDGE_ROLE)) {
            return PrlAppsConstants.COURT_STAFF;
        } else if (roles.contains(LEGAL_ADVISER_ROLE)) {
            return PrlAppsConstants.COURT_STAFF;
        }

        return CAFCASS;
    }

    public static void removeTemporaryFields(Map<String, Object> caseDataMap, String... fields) {
        for (String field : fields) {
            caseDataMap.remove(field);
        }
    }
}
