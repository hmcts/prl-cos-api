package uk.gov.hmcts.reform.prl.services.sendandreply.messagehandler;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;

import java.util.Map;

@Getter
@Builder
public class MessageRequest {
    private CaseData caseData;
    private Map<String, Object> caseDataMap;
    private Message message;

    public String getCaseId() {
        return String.valueOf(caseData.getId());
    }
}
