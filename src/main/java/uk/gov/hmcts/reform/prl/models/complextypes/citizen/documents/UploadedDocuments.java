package uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class UploadedDocuments {
    @CCD(label = "Parent document type", searchable = false)
    private final String parentDocumentType;
    @CCD(label = "Document type", searchable = false)
    private final String documentType;
    @CCD(label = "Name", searchable = false)
    private final String partyName;
    @CCD(label = "Uploaded by applicant", showCondition = "[STATE]=\"NEVER_SHOW\"", searchable = false)
    private final String isApplicant;
    @CCD(label = "party id", showCondition = "[STATE]=\"NEVER_SHOW\"", searchable = false)
    private final String uploadedBy;
    @CCD(label = "Creation date", searchable = false)
    private final LocalDate dateCreated;
    @CCD(label = "Document details", searchable = false)
    private final DocumentDetails documentDetails;
    @CCD(label = "Uploaded document", categoryID = "citizenQuarantine", searchable = false)
    private final Document citizenDocument;
    @CCD(label = "Cafcass document", categoryID = "safeguardingLetter", searchable = false)
    private final Document cafcassDocument;
    @CCD(
            label = "Is document requested by court to upload?",
            showCondition = "[STATE]=\"NEVER_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo documentRequestedByCourt;
}
