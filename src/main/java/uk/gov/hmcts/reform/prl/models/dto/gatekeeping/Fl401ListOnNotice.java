package uk.gov.hmcts.reform.prl.models.dto.gatekeeping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Fl401ListOnNotice {

    private final String isFl401CaseCreatedForWithOutNotice;
    private final String fl401WithOutNoticeReasonToRespondent;
    private final Fl401ListOnNoticeDirections fl401ListOnNoticeAdditionalDirections;
    private final List<Element<HearingData>> fl401ListOnNoticeHearingDetails;
    private final String fl401ListOnNoticeDirectionsToAdmin;

}
