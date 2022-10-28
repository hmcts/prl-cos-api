package uk.gov.hmcts.reform.prl.models.c100rebuild;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChildDto {

    private AbuseDto physicalAbuse;
    private AbuseDto psychologicalAbuse;
    private AbuseDto emotionalAbuse;
    private AbuseDto sexualAbuse;
    private AbuseDto financialAbuse;
    private AbuseDto somethingElse;
}