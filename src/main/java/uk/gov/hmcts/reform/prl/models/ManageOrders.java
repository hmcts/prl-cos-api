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
    @JsonProperty("childOption")
    private final String childOption;
    @JsonProperty("childSelectorOption1")
    private final List<ChildSelectorEnum> childSelectorOption1;
    @JsonProperty("childSelectorOption2")
    private final List<ChildSelectorEnum> childSelectorOption2;
    @JsonProperty("childSelectorOption3")
    private final List<ChildSelectorEnum> childSelectorOption3;
    @JsonProperty("childSelectorOption4")
    private final List<ChildSelectorEnum> childSelectorOption4;
    @JsonProperty("childSelectorOption5")
    private final List<ChildSelectorEnum> childSelectorOption5;
    @JsonProperty("childSelectorOption6")
    private final List<ChildSelectorEnum> childSelectorOption6;
    @JsonProperty("childSelectorOption7")
    private final List<ChildSelectorEnum> childSelectorOption7;
    @JsonProperty("childSelectorOption8")
    private final List<ChildSelectorEnum> childSelectorOption8;
    @JsonProperty("childSelectorOption9")
    private final List<ChildSelectorEnum> childSelectorOption9;
    @JsonProperty("childSelectorOption10")
    private final List<ChildSelectorEnum> childSelectorOption10;
    @JsonProperty("childSelectorOption11")
    private final List<ChildSelectorEnum> childSelectorOption11;
    @JsonProperty("childSelectorOption12")
    private final List<ChildSelectorEnum> childSelectorOption12;
    @JsonProperty("childSelectorOption13")
    private final List<ChildSelectorEnum> childSelectorOption13;
    @JsonProperty("childSelectorOption14")
    private final List<ChildSelectorEnum> childSelectorOption14;
    @JsonProperty("childSelectorOption15")
    private final List<ChildSelectorEnum> childSelectorOption15;
}
