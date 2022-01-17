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


    private final String caseLink;


    @Builder
    public CaseWorkerEmail(String caseReference,
                           String caseName, String applicantName,
                           String ordersApplyingFor, String hearingDateRequested,
                           String typeOfHearing, String respondentLastName,
                           String courtEmail, String caseLink) {
        super(caseReference);
        this.caseName = caseName;
        this.applicantName = applicantName;
        this.ordersApplyingFor = ordersApplyingFor;
        this.hearingDateRequested = hearingDateRequested;
        this.typeOfHearing = typeOfHearing;
        this.respondentLastName = respondentLastName;
        this.courtEmail = courtEmail;
        this.caseLink = caseLink;
    }
}
