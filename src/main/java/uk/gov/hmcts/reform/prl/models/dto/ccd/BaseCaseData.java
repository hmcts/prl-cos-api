package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.reopenclosedcases.ValidReopenClosedCasesStatusEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.refuge.RefugeConfidentialDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.closingcases.ClosingCaseOptions;
import uk.gov.hmcts.reform.prl.models.dto.ccd.restrictedcaseaccessmanagement.CaseAccessStatusAndReason;
import uk.gov.hmcts.reform.prl.models.serviceofdocuments.ServiceOfDocuments;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@SuperBuilder(toBuilder = true)
public class BaseCaseData {

    private long id;

    private State state;

    private String taskListVersion;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime lastModifiedDate;

    private String dateSubmitted;

    private String caseSubmittedTimeStamp;

    private String courtSeal;

    @JsonProperty("c1ADraftDocument")
    private  Document c1ADraftDocument;
    @JsonProperty("c1AWelshDraftDocument")
    private  Document c1AWelshDraftDocument;

    /**
     * Case Type Of Application.
     */
    private String selectedCaseTypeID;
    /**
     * Case Type Of Application.
     */
    @JsonProperty("caseTypeOfApplication")
    private String caseTypeOfApplication;
    /**
     * Case name.
     */
    @JsonAlias({"applicantCaseName", "applicantOrRespondentCaseName"})
    private String applicantCaseName;

    //FPET-567 - Added for hiding fields for SDO
    @JsonProperty("isSdoSelected")
    private YesOrNo isSdoSelected;

    @JsonProperty("isPathfinderCase")
    private YesOrNo isPathfinderCase;

    @JsonUnwrapped
    private DocumentsNotifications documentsNotifications;

    private YesOrNo hwfRequestedForAdditionalApplicationsFlag;
    private String awpWaTaskName;
    private String awpHwfRefNo;

    /**
     * Process urgent help with fees.
     */
    @JsonUnwrapped
    private ProcessUrgentHelpWithFees processUrgentHelpWithFees;

    @JsonProperty("isApplicantRepresented")
    private String isApplicantRepresented;


    @JsonProperty("refugeDocuments")
    private List<Element<RefugeConfidentialDocuments>> refugeDocuments;

    @JsonProperty("historicalRefugeDocuments")
    private List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments;

    @JsonUnwrapped
    private CaseAccessStatusAndReason caseAccessStatusAndReason;

    @JsonUnwrapped
    private ClosingCaseOptions closingCaseOptions;

    //PRL-6191 - Added for Record final decision
    private String finalCaseClosedDate;

    private YesOrNo caseClosed;

    //PRL-6262 - Reopening closed cases
    private ValidReopenClosedCasesStatusEnum changeStatusOptions;
    private String reopenStateTo;

    @JsonUnwrapped
    private ServiceOfDocuments serviceOfDocuments;

    @JsonProperty("nextHearingDate")
    private LocalDate nextHearingDate;

    @JsonUnwrapped
    private HearingTaskData hearingTaskData;

    private String isNonWorkAllocationEnabledCourtSelected;

    @JsonProperty("respondentSolicitorName")
    private String respondentSolicitorName;

    @JsonProperty("daApplicantContactInstructions")
    private String daApplicantContactInstructions;
}
