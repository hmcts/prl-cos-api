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

    @JsonProperty("too_courtOrder")
    private String[] courtOrder;
    @JsonProperty("too_stopOtherPeopleDoingSomethingSubField")
    private String[] reasonsOfHearingWithoutNotice;
    @JsonProperty("too_resolveSpecificIssueSubField")
    private String[] resolveSpecificIssueSubField;
    @JsonProperty("too_shortStatement")
    private String shortStatement;
}