package uk.gov.hmcts.reform.prl.models.dto.notify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class OrderEmailNotification extends EmailTemplateVars {

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

    private final String multipleOrdersWelshSentence;

    private final String caseLink;

    private final String caseReference;


}
