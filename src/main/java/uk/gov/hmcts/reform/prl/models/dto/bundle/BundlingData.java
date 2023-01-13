package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundlingData {

    @JsonProperty("applicantCaseName")
    private final String applicantCaseName;

    @JsonProperty("caseNumber")
    private final String caseNumber;

    @JsonProperty("hearingDetails")
    private final BundleHearingInfo hearingDetails;

    @JsonProperty("orders")
    private final List<Element<BundlingRequestDocument>> orders;

    @JsonProperty("allOtherDocuments")
    private List<Element<BundlingRequestDocument>> allOtherDocuments;

    @JsonProperty("applications")
    private List<Element<BundlingRequestDocument>> applications;


}