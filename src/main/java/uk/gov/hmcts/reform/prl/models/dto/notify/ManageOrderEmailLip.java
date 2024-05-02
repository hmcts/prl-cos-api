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

    private final String emailTitle;

    private final String emailTitleWelsh;

    private final String emailText;

    private final String emailTextWelsh;

    private final String multipleOrders;

    private final String multipleOrdersWelsh;

    private final String caseLink;

    private final String caseReference;


    @Builder
    public ManageOrderEmailLip(String caseName, String applicantName, String emailSubject, String emailTitle,
                               String emailTitleWelsh, String emailText, String emailTextWelsh, String multipleOrders,
                               String multipleOrdersWelsh, String caseLink, String caseReference) {
        this.caseName = caseName;
        this.applicantName = applicantName;
        this.emailSubject = emailSubject;
        this.emailTitle = emailTitle;
        this.emailTitleWelsh = emailTitleWelsh;
        this.emailText = emailText;
        this.emailTextWelsh = emailTextWelsh;
        this.multipleOrders = multipleOrders;
        this.multipleOrdersWelsh = multipleOrdersWelsh;
        this.caseLink = caseLink;
        this.caseReference = caseReference;
    }
}
