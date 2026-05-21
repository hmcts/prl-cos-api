package uk.gov.hmcts.reform.prl.services.documentremoval.submittedaction;


import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

public interface DocumentRemovalSubmittedAction {

    void onSubmitted(CallbackRequest callbackRequest);
}
