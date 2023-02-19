package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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
}
