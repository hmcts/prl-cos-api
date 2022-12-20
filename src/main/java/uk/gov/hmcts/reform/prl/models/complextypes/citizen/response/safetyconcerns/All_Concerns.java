package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.safetyconcerns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class All_Concerns {
    private final SafetyConcerns c1asafetyconcerns;
    private final Otherconcerns otherconcerns;
    private final Abductions abductions;
}
