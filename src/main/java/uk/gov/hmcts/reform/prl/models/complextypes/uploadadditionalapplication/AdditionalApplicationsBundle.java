package uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.ServedParties;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdditionalApplicationsBundle {
    @CCD(label = "Date and time of upload", showCondition = "c2DocumentBundle=\"DO NOT SHOW\"", searchable = false)
    private final String uploadedDateTime;
    @CCD(label = "Uploaded by", showCondition = "c2DocumentBundle=\"Do Not Show\"", searchable = false)
    private final String author;
    @CCD(label = "C2 application", searchable = false)
    private C2DocumentBundle c2DocumentBundle;
    @CCD(label = "Other applications", searchable = false)
    private OtherApplicationsBundle otherApplicationsBundle;
    @CCD(label = "Payment details", showCondition = "payment!=\"\"", searchable = false)
    private Payment payment;
    @CCD(label = "Party Type", showCondition = "c2DocumentBundle=\"DO NOT SHOW\"", searchable = false)
    private final PartyEnum partyType;
    @CCD(label = " ", showCondition = "c2DocumentBundle=\"DO NOT SHOW\"", searchable = false)
    private final List<Element<ServedParties>> selectedParties;
}
