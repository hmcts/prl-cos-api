package uk.gov.hmcts.reform.prl.models.documentremoval;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "DocumentRemovalConfirmOptions", generate = true)
public enum DocumentRemovalConfirmOption {
    @CCD(
            label = "If a document has caused a data breach, please follow HMCTS Optic Data Incident guidance to ensure appropriate reporting"
    )
    DOCUMENT_REMOVAL_CONFIRMED
}
