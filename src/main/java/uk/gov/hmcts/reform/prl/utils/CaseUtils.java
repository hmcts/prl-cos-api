package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDateTime;

public class CaseUtils {

    public static CaseData getCaseData(CaseDetails caseDetails, ObjectMapper objectMapper) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class)
            .toBuilder()
            .id(caseDetails.getId())
            .state(State.tryFromValue(caseDetails.getState()).orElse(null))
            .createdDate(caseDetails.getCreatedDate())
            .lastModifiedDate(caseDetails.getLastModified())
            .build();

        return caseData;
    }

    public static CaseData getCaseData(CaseDetails caseDetails, ObjectMapper objectMapper, State state) {
        CaseData.CaseDataBuilder caseDataBuilder = objectMapper.convertValue(caseDetails.getData(), CaseData.class)
            .toBuilder()
            .id(caseDetails.getId())
            .state(State.tryFromValue(caseDetails.getState()).orElse(null))
            .createdDate(caseDetails.getCreatedDate())
            .lastModifiedDate(caseDetails.getLastModified());

        if (null != state && (State.SUBMITTED_NOT_PAID.equals(state) || State.SUBMITTED_PAID.equals(state))) {
            caseDataBuilder.dateSubmitted(LocalDateTime.now());
        }
        return caseDataBuilder.build();
    }

    public static String getStateLabel(State state) {
        return state != null ? state.getLabel() : "";
    }
}
