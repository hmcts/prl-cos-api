package uk.gov.hmcts.reform.prl.models.dto.citizen.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.common.MappableObject;



@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder(toBuilder = true)
public class HearingResponseData implements MappableObject {

    private final String date;

    private final String time;

    private final String durationOfHearing;

    private final String typeOfHearing;

    private final String hearingLink;

    private final String courtName;

    private final String support;

    private final String hearingNotice;
}
