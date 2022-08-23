package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.MiamExemptions;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class CafCassCaseData {
    @JsonProperty("submitAndPayDownloadApplicationLink")
    private CafCassDocument submitAndPayDownloadApplicationLink;

    @JsonProperty("c8Document")
    private CafCassDocument c8Document;

    @JsonProperty("c1ADocument")
    private Document c1ADocument;

    private String dateSubmitted;
    private String caseTypeOfApplication;
    private CafCassDocument draftConsentOrderFile;

    private ConfidentialDetails confidentialDetails;

    private YesOrNo isInterpreterNeeded;
    private List<Element<InterpreterNeed>> interpreterNeeds;

    private YesNoDontKnow childrenKnownToLocalAuthority;

    private List<Element<OtherDocuments>> otherDocuments;

    private CafCassDocument finalDocument;

    private List<OrderTypeEnum> ordersApplyingFor;

    private List<Element<Child>> children;

    private CafCassDocument miamCertificationDocumentUpload1;

    private String miamStatus;

    private MiamExemptions miamExemptionsTable;

    private OrderAppliedFor summaryTabForOrderAppliedFor;

    private List<Element<ApplicantDetails>> applicants;

    private List<Element<ApplicantDetails>> respondents;

    private List<Element<ApplicantConfidentialityDetails>> applicantsConfidentialDetails;

    private String applicantSolicitorEmailAddress;

    private String solicitorName;

    private String courtName;

    private List<Element<OtherPersonInTheCase>> otherPeopleInTheCaseTable;

    private CafCassDocument ordersNonMolestationDocument;

    private CafCassDocument hearingOutComeDocument;

    private List<Element<ManageOrderCollection>> manageOrderCollection;

    private List<Element<HearingData>> hearingData;
}
