package uk.gov.hmcts.reform.prl.mapper.bundle;

import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;

public interface IBundleCreateRequestMapper {

    String REDACTED_DOCUMENT_URL = "documents/00000000-0000-0000-0000-000000000000";
    String REDACTED_DOCUMENT_URL_BINARY = "documents/00000000-0000-0000-0000-000000000000/binary";
    String REDACTED_DOCUMENT_FILE_NAME = "*redacted*";

    BundleCreateRequest mapCaseDataToBundleCreateRequest(CaseData caseData, String eventId, Hearings hearingDetails,
                                                                String bundleConfigFileName);
}
