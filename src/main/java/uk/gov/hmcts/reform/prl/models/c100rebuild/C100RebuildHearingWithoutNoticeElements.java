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

    @JsonProperty("hwn_hearingPart1")
    private YesOrNo doYouNeedHearingWithoutNotice;
    @JsonProperty("hwn_reasonsForApplicationWithoutNotice")
    private String reasonsOfHearingWithoutNotice;
    @JsonProperty("hwn_doYouNeedAWithoutNoticeHearing")
    private YesOrNo doYouNeedHearingWithoutNoticeAsOtherPplDoSomething;
    @JsonProperty("hwn_doYouNeedAWithoutNoticeHearingDetails")
    private String doYouNeedHearingWithoutNoticeAsOtherPplDoSomethingDetails;
    @JsonProperty("hwn_doYouRequireAHearingWithReducedNotice")
    private YesOrNo doYouNeedHearingWithoutNoticeWithoutReducedNotice;
    @JsonProperty("hwn_doYouRequireAHearingWithReducedNoticeDetails")
    private String doYouNeedHearingWithoutNoticeWithoutReducedNoticeDetails;
}