package uk.gov.hmcts.reform.prl.models.caseflags.flagdetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class Path {
    public String id;
    public String value;
}
