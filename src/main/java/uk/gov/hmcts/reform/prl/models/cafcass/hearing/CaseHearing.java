package uk.gov.hmcts.reform.prl.models.cafcass.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder(builderMethodName = "caseHearingWith")
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CaseHearing {

    private Long hearingID;

    private String hearingType;

    private String hearingTypeValue;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String hmcStatus;

    private List<HearingDaySchedule> hearingDaySchedule;

    @JsonProperty("hearingStatus")
    public String getHearingStatus() {
        return hmcStatus;
    }

}
