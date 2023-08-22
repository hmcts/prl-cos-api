package uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.ServedParties;

import java.util.List;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdditionalApplicationsBundle {
    private final String uploadedDateTime;
    private final String author;
    private C2DocumentBundle c2DocumentBundle;
    private OtherApplicationsBundle otherApplicationsBundle;
    private Payment payment;
    private final PartyEnum partyType;
    private final List<Element<ServedParties>> selectedParties;
}
