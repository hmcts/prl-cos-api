package uk.gov.hmcts.reform.prl.models.dto.notify;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ManageOrderEmail extends EmailTemplateVars {


    @JsonProperty("caseName")
    private final String caseName;

    @JsonProperty("applicantName")
    private final String applicantName;

    @JsonProperty("courtName")
    private final String courtName;

    @JsonProperty("courtEmail")
    private final String courtEmail;

    @JsonProperty("fullName")
    private final String fullName;

    @JsonProperty("familyManNumber")
    private final String familyManNumber;

    @JsonProperty("caseUrgency")
    private final String caseUrgency;

    @JsonProperty("orderLink")
    private final String orderLink;

    @JsonProperty("issueDate")
    private final String issueDate;

    @JsonProperty("instructions")
    private final String instructions;

    private final String newOrder;

    private final String finalOrder;

    private final String newOrders;

    private final String finalOrders;
    private final String newAndFinalOrders;

    private final String caseLink;

    private final String dashboardLink;

    @Builder
    public ManageOrderEmail(String caseReference,
                            String caseName,
                            String applicantName, String courtName,
                            String courtEmail, String firstName,
                            String lastName, String fullName,
                            String familyManNumber, String caseUrgency, String orderLink,
                            String issueDate, String instructions,
                            String newOrder, String finalOrder, String newOrders,
                            String finalOrders, String newAndFinalOrders, String caseLink,
                            String dashboardLink) {
        super(caseReference);
        this.caseName = caseName;
        this.applicantName = applicantName;
        this.courtName = courtName;
        this.courtEmail = courtEmail;
        this.fullName = fullName;
        this.familyManNumber = familyManNumber;
        this.caseUrgency = caseUrgency;
        this.orderLink = orderLink;
        this.issueDate = issueDate;
        this.instructions = instructions;
        this.newOrder = newOrder;
        this.finalOrder = finalOrder;
        this.newOrders = newOrders;
        this.finalOrders = finalOrders;
        this.newAndFinalOrders = newAndFinalOrders;
        this.caseLink = caseLink;
        this.dashboardLink = dashboardLink;
    }
}
