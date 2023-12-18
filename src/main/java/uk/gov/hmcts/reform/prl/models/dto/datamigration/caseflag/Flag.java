package uk.gov.hmcts.reform.prl.models.dto.datamigration.caseflag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class Flag {
    @JsonProperty("FlagDetails")
    private List<FlagDetail> flagDetails;
}
