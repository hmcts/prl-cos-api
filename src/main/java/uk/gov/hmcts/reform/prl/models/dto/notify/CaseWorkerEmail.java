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

    @JsonProperty("isCaseUrgent")
    private final String isCaseUrgent;

    @JsonProperty("isConfidential")
    private final String isConfidential;

    @JsonProperty("caseUrgency")
    private final String caseUrgency;

    private final String caseLink;


    @Builder
    public CaseWorkerEmail(String caseReference,
                           String caseName, String applicantName,
                           String ordersApplyingFor, String hearingDateRequested,
                           String typeOfHearing, String respondentLastName,
                           String courtEmail,
                           String contentFromDev, String issueDate,
                           String isCaseUrgent, String isConfidential,
                           String caseUrgency, String caseLink) {
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
        this.isCaseUrgent = isCaseUrgent;
        this.isConfidential = isConfidential;
        this.caseUrgency = caseUrgency;
        this.caseLink = caseLink;
    }
}
