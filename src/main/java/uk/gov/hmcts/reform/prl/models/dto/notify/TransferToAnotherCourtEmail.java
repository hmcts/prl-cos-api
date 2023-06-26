package uk.gov.hmcts.reform.prl.models.dto.notify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TransferToAnotherCourtEmail extends EmailTemplateVars {

    @JsonProperty("caseName")
    private final String caseName;
    @JsonProperty("issueDate")
    private final String issueDate;
    @JsonProperty("solicitorName")
    private final String solicitorName;
    @JsonProperty("caseLink")
    private final String caseLink;
    @JsonProperty("litigantName")
    private final String litigantName;

    @JsonProperty("citizenSignUpLink")
    private final String citizenSignUpLink;

    @JsonProperty("orderLink")
    private final String orderLink;

    @JsonProperty("transferDate")
    private final String transferDate;

    @JsonProperty("courtName")
    private final String courtName;

    @Builder
    public TransferToAnotherCourtEmail(String caseReference,
                                       String caseName,
                                       String issueDate,
                                       String solicitorName,
                                       String caseLink,
                                       String litigantName,
                                       String citizenSignUpLink,
                                       String orderLink,
                                       String transferDate,
                                       String courtName) {
        super(caseReference);
        this.caseName = caseName;
        this.issueDate = issueDate;
        this.solicitorName = solicitorName;
        this.caseLink = caseLink;
        this.litigantName = litigantName;
        this.citizenSignUpLink = citizenSignUpLink;
        this.orderLink = orderLink;
        this.transferDate = transferDate;
        this.courtName = courtName;
    }
}
