package uk.gov.hmcts.reform.prl.clients.ccd.records;

import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Map;

public record CitizenUpdatePartyDataContent(Map<String, Object> updatedCaseDataMap,
                                            CaseData updatedCaseData) {
}
