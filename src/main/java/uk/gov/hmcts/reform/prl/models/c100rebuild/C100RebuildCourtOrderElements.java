package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class C100RebuildCourtOrderElements {

    @JsonProperty("courtOrder")
    private String[] courtOrder;
    @JsonProperty("stopOtherPeopleDoingSomethingSubField")
    private String[] reasonsOfHearingWithoutNotice;
    @JsonProperty("resolveSpecificIssueSubField")
    private String[] resolveSpecificIssueSubField;
    @JsonProperty("shortStatement")
    private String shortStatement;
}