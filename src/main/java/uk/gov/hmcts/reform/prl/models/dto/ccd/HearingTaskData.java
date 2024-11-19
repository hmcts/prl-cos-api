package uk.gov.hmcts.reform.prl.models.dto.ccd;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HearingTaskData {

    private String currentHearingId;
    private String currentHearingStatus;
    private List<Element<String>> existedTaskHearingIds;
}
