package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class CafCassCaseData {
    @Setter(AccessLevel.NONE)
    @JsonProperty("submitAndPayDownloadApplicationLink")
    private CafCassDocument submitAndPayDownloadApplicationLink;

    public void setSubmitAndPayDownloadApplicationLink(CafCassDocument submitAndPayDownloadApplicationLink) {
        try {
            if(submitAndPayDownloadApplicationLink != null) {
                URL url = new URL(submitAndPayDownloadApplicationLink.getDocumentId());
                submitAndPayDownloadApplicationLink.setDocumentId(getDocumentId(url));
            }
        } catch (Exception e) {}
        this.submitAndPayDownloadApplicationLink = submitAndPayDownloadApplicationLink;
    }

    @Setter(AccessLevel.NONE)
    @JsonProperty("c8Document")
    private CafCassDocument c8Document;

    public void setC8Document(CafCassDocument c8Document) {
        try {
            if(c8Document != null) {
                URL url = new URL(c8Document.getDocumentId());
                c8Document.setDocumentId(getDocumentId(url));
            }
        } catch (Exception e) {}
        this.c8Document = c8Document;
    }


    @Setter(AccessLevel.NONE)
    @JsonProperty("c1ADocument")
    private Document c1ADocument;

    public void setC1ADocument(Document c1ADocument) {
        try {
            if(c1ADocument != null) {
                URL url = new URL(c1ADocument.getDocumentUrl());
                c1ADocument.setDocumentUrl(getDocumentId(url));
            }
        } catch (Exception e) {}
        this.c1ADocument =c1ADocument;
    }

    private String getDocumentId(URL url) {
        String path = url.getPath();
        String documentId = path.split("/")[path.split("/").length - 1];
        return documentId;
    }

    private String familymanCaseNumber;
    private String dateSubmitted;
    private String caseTypeOfApplication;

    @Setter(AccessLevel.NONE)
    private CafCassDocument draftConsentOrderFile;

    public void setDraftConsentOrderFile(CafCassDocument draftConsentOrderFile) {
        try {
            if(draftConsentOrderFile != null) {
                URL url = new URL(draftConsentOrderFile.getDocumentId());
                draftConsentOrderFile.setDocumentId(getDocumentId(url));
            }
        } catch (Exception e) {}
        this.draftConsentOrderFile =draftConsentOrderFile;
    }

    private ConfidentialDetails confidentialDetails;

    private YesOrNo isInterpreterNeeded;
    private List<Element<InterpreterNeed>> interpreterNeeds;

    private YesNoDontKnow childrenKnownToLocalAuthority;

    public void setOtherDocuments(List<Element<OtherDocuments>> otherDocuments) {
        try {
            if(otherDocuments != null) {
                List<Element<OtherDocuments>> updatedOtherDocumentList = otherDocuments.stream()
                    .map(otherDocumentsElement -> updateElementDocumentId(otherDocumentsElement)).collect(Collectors.toList());
                this.otherDocuments = updatedOtherDocumentList;
            }
        } catch (Exception e) {
            this.otherDocuments =otherDocuments;
        }
    }

    private Element<OtherDocuments> updateElementDocumentId(Element<OtherDocuments> otherDocumentsElement) {
        try {
            if(otherDocumentsElement != null) {
                Document documentOther = otherDocumentsElement.getValue().getDocumentOther();
                URL url = new URL(documentOther.getDocumentUrl());
                documentOther.setDocumentUrl(getDocumentId(url));
                otherDocumentsElement.getValue().setDocumentOther(documentOther);
            }
        } catch (Exception e) {
            return otherDocumentsElement;
        }
        return otherDocumentsElement;
    }

    @Setter(AccessLevel.NONE)
    private List<Element<OtherDocuments>> otherDocuments;

    public void setFinalDocument(CafCassDocument finalDocument) {
        try {
            if(finalDocument != null) {
                URL url = new URL(finalDocument.getDocumentId());
                finalDocument.setDocumentId(getDocumentId(url));
            }
        } catch (Exception e) {}
        this.finalDocument =finalDocument;
    }

    @Setter(AccessLevel.NONE)
    private CafCassDocument finalDocument;

    private List<OrderTypeEnum> ordersApplyingFor;

    private List<Element<Child>> children;

    public void setMiamCertificationDocumentUpload1(CafCassDocument miamCertificationDocumentUpload1) {
        try {
            if(miamCertificationDocumentUpload1 != null) {
                URL url = new URL(miamCertificationDocumentUpload1.getDocumentId());
                miamCertificationDocumentUpload1.setDocumentId(getDocumentId(url));
            }
        } catch (Exception e) {}
        this.miamCertificationDocumentUpload1 =miamCertificationDocumentUpload1;
    }

    @Setter(AccessLevel.NONE)
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

    public void setOrdersNonMolestationDocument(CafCassDocument ordersNonMolestationDocument) {
        try {
            if(ordersNonMolestationDocument != null) {
                URL url = new URL(ordersNonMolestationDocument.getDocumentId());
                ordersNonMolestationDocument.setDocumentId(getDocumentId(url));
            }
        } catch (Exception e) {}
        this.ordersNonMolestationDocument =ordersNonMolestationDocument;
    }

    @Setter(AccessLevel.NONE)
    private CafCassDocument ordersNonMolestationDocument;

    public void setHearingOutComeDocument(CafCassDocument hearingOutComeDocument) {
        try {
            if(hearingOutComeDocument != null) {
                URL url = new URL(hearingOutComeDocument.getDocumentId());
                hearingOutComeDocument.setDocumentId(getDocumentId(url));
            }
        } catch (Exception e) {}
        this.hearingOutComeDocument =hearingOutComeDocument;
    }

    @Setter(AccessLevel.NONE)
    private CafCassDocument hearingOutComeDocument;

    private List<Element<ManageOrderCollection>> manageOrderCollection;

    private List<Element<HearingData>> hearingData;
}
