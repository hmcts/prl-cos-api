package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import uk.gov.hmcts.reform.prl.models.CaseLinksElement;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.AllPartyFlags;
import uk.gov.hmcts.reform.prl.models.caselink.AutomatedHearingCaseLink;
import uk.gov.hmcts.reform.prl.models.complextypes.AutomatedHearingCaseManagementLocation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "automatedHearingCaseDataBuilder")
public class AutomatedHearingCaseData {

    public AutomatedHearingCaseData(AutomatedHearingManageOrders manageOrders, AutomatedHearingAttendHearing attendHearing, LocalDate issueDate) {
        /* default constructor */
        this.manageOrders = manageOrders;
        this.attendHearing = attendHearing;
        this.issueDate = issueDate;
    }

    @JsonProperty("id")
    private long id;

    @JsonProperty("taskListVersion")
    private String taskListVersion;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    @JsonProperty("createdDate")
    private LocalDateTime createdDate;

    @JsonProperty("familymanCaseNumber")
    private String familymanCaseNumber;

    @JsonProperty("dateSubmitted")
    private String dateSubmitted;

    @JsonProperty("caseTypeOfApplication")
    private String caseTypeOfApplication;

    @JsonProperty("applicants")
    private List<Element<AutomatedHearingPartyDetails>> applicants;

    @JsonProperty("respondents")
    private List<Element<AutomatedHearingPartyDetails>> respondents;

    @JsonProperty("otherPartyInTheCaseRevised")
    private List<Element<AutomatedHearingPartyDetails>> otherPartyInTheCaseRevised;

    @JsonProperty("applicantSolicitorEmailAddress")
    private String applicantSolicitorEmailAddress;

    @JsonProperty("solicitorName")
    private String solicitorName;

    @JsonProperty("courtName")
    private String courtName;

    @JsonProperty("applicantsFL401")
    private AutomatedHearingPartyDetails applicantsFL401;

    @JsonProperty("respondentsFL401")
    private AutomatedHearingPartyDetails respondentsFL401;

    @JsonProperty("caseManagementLocation")
    private AutomatedHearingCaseManagementLocation caseManagementLocation;

    @JsonProperty("caseLinks")
    public List<CaseLinksElement<AutomatedHearingCaseLink>> caseLinks;

    @JsonAlias({"applicantCaseName", "applicantOrRespondentCaseName"})
    private String applicantCaseName;

    @JsonProperty("allPartyFlags")
    private AllPartyFlags allPartyFlags;

    @JsonUnwrapped
    private final AutomatedHearingManageOrders manageOrders;

    @JsonUnwrapped
    private final AutomatedHearingAttendHearing attendHearing;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonProperty("issueDate")
    private final LocalDate issueDate;

    @JsonProperty("hearingData")
    private AutomatedHearingData hearingData;
}
