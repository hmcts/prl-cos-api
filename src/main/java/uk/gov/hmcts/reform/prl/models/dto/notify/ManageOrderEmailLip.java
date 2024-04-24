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

    private final String newOrderTitle;

    private final String finalOrderTitle;

    private final String newAndFinalOrderTitle;

    private final String order;

    private final String orders;

    private final String newOrderText;

    private final String finalOrderText;

    private final String newOrdersText;

    private final String finalOrdersText;

    private final String newAndFinalOrdersText;

    private final String newOrderExplanation;

    private final String finalOrderExplanation;

    private final String caseLink;

    @Builder
    public ManageOrderEmailLip(String caseName, String applicantName, String newOrderTitle, String finalOrderTitle,
                               String newAndFinalOrderTitle, String order, String orders, String newOrderText,
                               String finalOrderText, String newOrdersText, String finalOrdersText,
                               String newAndFinalOrdersText, String newOrderExplanation, String finalOrderExplanation,
                               String caseLink) {
        this.caseName = caseName;
        this.applicantName = applicantName;
        this.newOrderTitle = newOrderTitle;
        this.finalOrderTitle = finalOrderTitle;
        this.newAndFinalOrderTitle = newAndFinalOrderTitle;
        this.order = order;
        this.orders = orders;
        this.newOrderText = newOrderText;
        this.finalOrderText = finalOrderText;
        this.newOrdersText = newOrdersText;
        this.finalOrdersText = finalOrdersText;
        this.newAndFinalOrdersText = newAndFinalOrdersText;
        this.newOrderExplanation = newOrderExplanation;
        this.finalOrderExplanation = finalOrderExplanation;
        this.caseLink = caseLink;
    }
}
