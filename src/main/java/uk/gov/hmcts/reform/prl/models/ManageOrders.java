package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.manageorders.ChildSelectorEnum;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManageOrders {

    @JsonProperty("cafcassEmailAddress")
    private final List<Element<String>> cafcassEmailAddress;
    @JsonProperty("otherEmailAddress")
    private final List<Element<String>> otherEmailAddress;
    private final String childOption;
    private final List<ChildSelectorEnum> childSelectorOption1;
    private final List<ChildSelectorEnum> childSelectorOption2;
    private final List<ChildSelectorEnum> childSelectorOption3;
    private final List<ChildSelectorEnum> childSelectorOption4;
    private final List<ChildSelectorEnum> childSelectorOption5;
    private final List<ChildSelectorEnum> childSelectorOption6;
    private final List<ChildSelectorEnum> childSelectorOption7;
    private final List<ChildSelectorEnum> childSelectorOption8;
    private final List<ChildSelectorEnum> childSelectorOption9;
    private final List<ChildSelectorEnum> childSelectorOption10;
    private final List<ChildSelectorEnum> childSelectorOption11;
    private final List<ChildSelectorEnum> childSelectorOption12;
    private final List<ChildSelectorEnum> childSelectorOption13;
    private final List<ChildSelectorEnum> childSelectorOption14;
    private final List<ChildSelectorEnum> childSelectorOption15;
}
