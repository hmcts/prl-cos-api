package uk.gov.hmcts.reform.prl.services.documentremoval.postabouttosubmitaction;

import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Map;

public interface DocumentRemovalAboutToSubmitAction {

    void onAboutToSubmit(CaseData caseData, Map<String, Object> caseDataUpdated);
}
