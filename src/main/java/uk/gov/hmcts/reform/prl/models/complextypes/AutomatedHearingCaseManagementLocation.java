package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Getter
@AllArgsConstructor
@Builder(builderMethodName = "automatedHearingCaseManagementLocationWith")
public class AutomatedHearingCaseManagementLocation {
    private String region;
    private String baseLocation;
}
