package uk.gov.hmcts.reform.prl.models.c100respondentsolicitor;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;

@Data
@Builder(toBuilder = true)
public class ApplicantC8 {
    private ResponseDocuments applicantAc8;
    private ResponseDocuments applicantBc8;
    private ResponseDocuments applicantCc8;
    private ResponseDocuments applicantDc8;
    private ResponseDocuments applicantEc8;
}
