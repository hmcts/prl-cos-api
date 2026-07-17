package uk.gov.hmcts.reform.prl.models.complextypes.managedocuments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.managedocuments.DocumentPartyEnum;
import uk.gov.hmcts.reform.prl.enums.managedocuments.DocumentRelatedToCase;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class ManageDocuments {

    @CCD(
            label = "Submitting document on behalf of",
            hint = "Select a party",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "DocumentPartyEnum"
    )
    private final DocumentPartyEnum documentParty;
    @CCD(label = "Document category", searchable = false, typeOverride = FieldType.DynamicList)
    private final DynamicList documentCategories;
    @CCD(label = "Document", hint = "File size must be under 1GB.", searchable = false)
    private final Document document;
    //NOT IN USE
    @CCD(
            label = "Details",
            showCondition = "documentDetails=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String documentDetails;
    @CCD(
            label = "Do you need to restrict access to the document?",
            showCondition = "documentRestrictCheckbox=\"DO_NOT_SHOW\"",
            searchable = false
    )
    private final List<RestrictToCafcassHmcts> documentRestrictCheckbox;

    //PRL-4320 - manage docs redesign
    @CCD(
            label = "Does the document contain confidential information?",
            hint = "Only HMCTS staff and the judiciary will be able to see it.",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isConfidential;
    @CCD(
            label = "Do you want to request this document is restricted?",
            hint = "The court will only restrict a document if there is a very good reason.Only court staff and the judiciary will be able to see it.",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isRestricted;
    @CCD(
            label = "Explain why you want to restrict access to the document",
            showCondition = "isRestricted=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String restrictedDetails;
    @CCD(label = "Confirm the document is related to this case", searchable = false)
    private final List<DocumentRelatedToCase> documentRelatedToCaseCheckbox;
}
