package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.InterpreterNeed;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.MiamExemptions;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.OtherPersonInTheCase;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.OrderAppliedFor;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder(toBuilder = true)
public class CafCassCaseData {
    @JsonProperty("submitAndPayDownloadApplicationLink")
    private final CafCassDocument submitAndPayDownloadApplicationLink;

    @JsonProperty("c8Document")
    private final CafCassDocument c8Document;

    @JsonProperty("c1ADocument")
    private final Document c1ADocument;

    private final String dateSubmitted;

    private final CafCassDocument draftConsentOrderFile;

    private ConfidentialDetails confidentialDetails;

    private final YesOrNo isInterpreterNeeded;
    private final List<Element<InterpreterNeed>> interpreterNeeds;

    private final YesNoDontKnow childrenKnownToLocalAuthority;

    private final List<Element<OtherDocuments>> otherDocuments;

    @JsonProperty("finalDocument")
    private final CafCassDocument finalDocument;

    private final List<OrderTypeEnum> ordersApplyingFor;

    private final List<Element<Child>> children;

    private final CafCassDocument miamCertificationDocumentUpload1;

    private final String miamStatus;

    private MiamExemptions miamExemptions;

    private final OrderAppliedFor summaryTabForOrderAppliedFor;

    private final List<Element<PartyDetails>> applicants;

    private final List<Element<PartyDetails>> respondents;

    private final List<Element<ApplicantConfidentialityDetails>> applicantsConfidentialDetails;

    private final String applicantSolicitorEmailAddress;

    @JsonProperty("solicitorName")
    private final String solicitorName;

    private String courtName;

    private List<Element<OtherPersonInTheCase>> otherPeopleInTheCaseTable;

    private final CafCassDocument ordersNonMolestationDocument;

    private final CafCassDocument hearingOutComeDocument;

    private List<Element<ManageOrderCollection>> manageOrderCollection;

    private List<Element<HearingData>> hearingData;
}
