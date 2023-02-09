package uk.gov.hmcts.reform.prl.models.c100rebuild;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class C100RebuildStatementOfTruthElements {
    private String applicantStatementOfTruth;
}
