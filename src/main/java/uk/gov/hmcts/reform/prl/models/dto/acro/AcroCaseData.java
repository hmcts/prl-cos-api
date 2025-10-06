package uk.gov.hmcts.reform.prl.models.dto.acro;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Builder(toBuilder = true)
public class AcroCaseData {

    private Long id;

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
    @Setter(AccessLevel.NONE)
    private List<Element<PartyDetails>> applicants;

    @Setter(AccessLevel.NONE)
    private List<Element<PartyDetails>> respondents;

    private List<Element<ApplicantConfidentialityDetails>> applicantsConfidentialDetails;
    private List<Element<ApplicantConfidentialityDetails>> respondentConfidentialDetails;

    private String courtEpimsId;
    private String courtTypeId;
    private String courtName;
    private PartyDetails applicant;
    private PartyDetails respondent;

    @Setter(AccessLevel.NONE)
    private CaseManagementLocation caseManagementLocation;
    private List<OrderDetails> fl404Orders = new ArrayList<>();
    private List<CaseHearing> caseHearings;
    private List<Element<StmtOfServiceAddRecipient>> stmtOfServiceForOrder;
    @JsonProperty("daApplicantContactInstructions")
    private String daApplicantContactInstructions;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOrderMade;
}
