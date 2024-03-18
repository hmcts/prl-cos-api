
package uk.gov.hmcts.reform.prl.clients.ccd.records;

import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Map;

public record CitizenUpdatePartyDataContent(Map<String, Object> updatedCaseDataMap,
                                            CaseData updatedCaseData) {
}
