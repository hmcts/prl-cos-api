package uk.gov.hmcts.reform.prl.models.dto.ccd.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class Match {

    private String state;

    @JsonProperty("data.caseTypeOfApplication")
    private String caseTypeOfApplication;

    @JsonProperty("data.fm5RemindersSent")
    private String fm5RemindersSent;

    @JsonProperty("data.cafcassServedOptions")
    private YesOrNo cafcassServedOptions;

    @JsonProperty("data.caseCreatedBy")
    private String caseCreatedBy;

    @JsonProperty("data.helpWithFees")
    private String helpWithFees;


    @JsonProperty("data.hwfRequestedForAdditionalApplicationsFlag")
    private String hwfRequestedForAdditionalApplicationsFlag;

    @JsonProperty("data.nextHearingDate")
    private LocalDate nextHearingDate;

}
