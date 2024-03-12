package uk.gov.hmcts.reform.prl.models.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.StatementOfServiceWhatWasServed;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;


@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class StatementOfService {

    @JsonProperty("stmtOfServiceWhatWasServed")
    private StatementOfServiceWhatWasServed stmtOfServiceWhatWasServed;
    @JsonProperty("stmtOfServiceAddRecipient")
    private List<Element<StmtOfServiceAddRecipient>> stmtOfServiceAddRecipient;
    @JsonProperty("stmtOfServiceForOrder")
    private List<Element<StmtOfServiceAddRecipient>> stmtOfServiceForOrder;
    @JsonProperty("stmtOfServiceForApplication")
    private List<Element<StmtOfServiceAddRecipient>> stmtOfServiceForApplication;
}
