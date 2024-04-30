package uk.gov.hmcts.reform.prl.models.dto.notify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ManageOrderEmailLip extends EmailTemplateVars {

    @JsonProperty("caseName")
    private final String caseName;

    @JsonProperty("applicantName")
    private final String applicantName;

    private final String emailSubject;

    private final String emailText;

    private final String orders;

    private final String order;

    private final String caseLink;

    private final String caseReference;


    @Builder
    public ManageOrderEmailLip(String caseName, String applicantName, String emailSubject, String emailText,
                               String orders, String order, String caseLink, String caseReference) {
        this.caseName = caseName;
        this.applicantName = applicantName;
        this.emailSubject = emailSubject;
        this.emailText = emailText;
        this.orders = orders;
        this.order = order;
        this.caseLink = caseLink;
        this.caseReference = caseReference;
    }
}
