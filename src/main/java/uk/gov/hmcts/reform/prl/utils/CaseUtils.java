package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

public class CaseUtils {

    public static CaseData getCaseData(CaseDetails caseDetails, ObjectMapper objectMapper) {
        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class)
            .toBuilder()
            .id(caseDetails.getId())
            .state(State.fromValue(caseDetails.getState()))
            .createdDate(caseDetails.getCreatedDate())
            .modifiedDate(caseDetails.getLastModified())
            .build();

        return caseData;
    }
}
