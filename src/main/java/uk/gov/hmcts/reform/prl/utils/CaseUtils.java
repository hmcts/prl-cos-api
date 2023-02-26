package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.complextypes.LocalCourtAdminEmail;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_EMAIL_ADDRESS_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ID_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME_FIELD;

@Slf4j
public class CaseUtils {

    private CaseUtils() {

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
        SelectTypeOfOrderEnum isFinalOrder = null;
        if (caseData.getSelectTypeOfOrder() != null) {
            isFinalOrder = caseData.getSelectTypeOfOrder();
        } else if (caseData.getServeOrderData() != null) {
            isFinalOrder = caseData.getServeOrderData().getSelectTypeOfUploadOrder();
        }
        return isFinalOrder;
    }

    public static String getCaseTypeOfApplication(CaseData caseData) {
        log.info("Manage order CaseTypeOfApplication ==> " +  caseData.getCaseTypeOfApplication());
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
                .regionId(regionId).baseLocationId(baseLocationId).regionName(regionName)
                .baseLocationName(baseLocationName).build());
            caseDataMap.put(COURT_NAME_FIELD, courtName);
            caseDataMap.put(COURT_ID_FIELD, baseLocationId);

        }
        return caseDataMap;
    }

    public static Map<String, Object> getCourtEmail(String[] idEmail, String caseTypeOfApplication) {
        String courtEmail = "";
        Map<String, Object> caseDataMap = new HashMap<>();
        if (idEmail.length > 1) {
            courtEmail = Arrays.stream(idEmail).toArray()[1].toString();
        }
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseTypeOfApplication)) {
            caseDataMap.put("localCourtAdmin", List.of(Element.<LocalCourtAdminEmail>builder().id(UUID.randomUUID())
                                                               .value(LocalCourtAdminEmail.builder().email(courtEmail)
                                                                          .build()).build()));
        } else {
            caseDataMap.put(COURT_EMAIL_ADDRESS_FIELD, courtEmail);
        }

        return caseDataMap;
    }

    public static String cafcassFlag(String regionId) {
        log.info("regionId ===> " + regionId);
        String cafcassFlag = PrlAppsConstants.NO; //wales

        int intRegionId = Integer.parseInt(regionId);

        if (intRegionId > 0 && intRegionId < 7) {
            cafcassFlag = PrlAppsConstants.YES; //english regions
        }
        log.info("is cafcass flag set ===> " + cafcassFlag);
        return cafcassFlag;
    }
}
