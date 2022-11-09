package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class C100RebuildHearingWithoutNoticeElements {

    @JsonProperty("hearingPart1")
    private YesOrNo doYouNeedHearingWithoutNotice;
    @JsonProperty("reasonsForApplicationWithoutNotice")
    private String reasonsOfHearingWithoutNotice;
    @JsonProperty("doYouNeedAWithoutNoticeHearing")
    private YesOrNo doYouNeedHearingWithoutNoticeAsOtherPplDoSomething;
    @JsonProperty("doYouNeedAWithoutNoticeHearingDetails")
    private String doYouNeedHearingWithoutNoticeAsOtherPplDoSomethingDetails;
    @JsonProperty("doYouRequireAHearingWithReducedNotice")
    private YesOrNo doYouNeedHearingWithoutNoticeWithoutReducedNotice;
    @JsonProperty("doYouRequireAHearingWithReducedNoticeDetails")
    private String doYouNeedHearingWithoutNoticeWithoutReducedNoticeDetails;
}