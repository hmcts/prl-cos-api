package uk.gov.hmcts.reform.prl.models.complextypes.serviceofdocuments;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.ServeOrgDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SodPack {

    @CCD(label = "Documents", searchable = false)
    private final List<Element<Document>> documents;
    @CCD(
            label = "Applicant ids",
            showCondition = "servedBy=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Text"
    )
    private final List<Element<String>> applicantIds;
    @CCD(
            label = "Respondent ids",
            showCondition = "servedBy=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Text"
    )
    private final List<Element<String>> respondentIds;
    @CCD(
            label = "Other person ids",
            showCondition = "servedBy=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Text"
    )
    private final List<Element<String>> otherPersonIds;
    @CCD(label = "Parties to be served", searchable = false)
    private final String partiesToBeServed;
    @CCD(label = "Additional recipients", showCondition = "servedBy=\"DO_NOT_SHOW\"", searchable = false)
    private List<Element<ServeOrgDetails>> additionalRecipients;
    @CCD(label = "To be served by", searchable = false)
    private final String servedBy;
    @CCD(label = "Is personal service?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isPersonalService;
    @CCD(label = "Submitted by", searchable = false)
    private final String submittedBy;
    @CCD(label = "Submitted date and time", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime submittedDateTime;
}
