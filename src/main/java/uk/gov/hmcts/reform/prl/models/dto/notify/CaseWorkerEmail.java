package uk.gov.hmcts.reform.prl.models.dto.notify;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class CaseWorkerEmail extends EmailTemplateVars {

    @JsonProperty("caseName")
    private final String caseName;

    @JsonProperty("applicantName")
    private final String applicantName;

    @JsonProperty("ordersApplyingFor")
    private final String ordersApplyingFor;

    @JsonProperty("hearingDateRequested")
    private final String hearingDateRequested;

    @JsonProperty("typeOfHearing")
    private final String typeOfHearing;

    @JsonProperty("respondentLastName")
    private final String respondentLastName;

    @JsonProperty("courtEmail")
    private final String courtEmail;

    @JsonProperty("contentFromDev")
    private final String contentFromDev;


    @JsonProperty("issueDate")
    private final String issueDate;

    @JsonProperty("isConfidential")
    private final String isConfidential;

    private final String caseLink;


    @Builder
    public CaseWorkerEmail(String caseReference,
                           String caseName, String applicantName,
                           String ordersApplyingFor, String hearingDateRequested,
                           String typeOfHearing, String respondentLastName,
                           String courtEmail,
                           String contentFromDev,
                           String issueDate, String isConfidential, String caseLink) {
        super(caseReference);
        this.caseName = caseName;
        this.applicantName = applicantName;
        this.ordersApplyingFor = ordersApplyingFor;
        this.hearingDateRequested = hearingDateRequested;
        this.typeOfHearing = typeOfHearing;
        this.respondentLastName = respondentLastName;
        this.courtEmail = courtEmail;
        this.contentFromDev = contentFromDev;
        this.issueDate = issueDate;
        this.isConfidential = isConfidential;
        this.caseLink = caseLink;
    }
}
