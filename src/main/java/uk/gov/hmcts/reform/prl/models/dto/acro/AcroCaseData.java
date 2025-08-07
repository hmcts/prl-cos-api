package uk.gov.hmcts.reform.prl.models.dto.acro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CaseManagementLocation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Builder(toBuilder = true)
public class AcroCaseData {

    @Setter(AccessLevel.NONE)
    private List<Element<OrderDetails>> orderCollection;

    private String familymanCaseNumber;
    private String dateSubmitted;
    private String issueDate;
    private String caseTypeOfApplication;

    @Setter(AccessLevel.NONE)
    private PartyDetails applicantsFL401;

    @Setter(AccessLevel.NONE)
    private PartyDetails respondentsFL401;

    private List<Element<ApplicantConfidentialityDetails>> applicantsConfidentialDetails;

    private String courtEpimsId;
    private String courtTypeId;
    private String courtName;

    @Setter(AccessLevel.NONE)
    private CaseManagementLocation caseManagementLocation;
    private List<OrderDetails> fl404Orders;
    private List<CaseHearing> caseHearings;
}
