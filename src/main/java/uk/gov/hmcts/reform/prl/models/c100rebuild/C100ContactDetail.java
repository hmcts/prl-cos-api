package uk.gov.hmcts.reform.prl.models.c100rebuild;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class C100ContactDetail {
    private String donKnowEmailAddress;
    private String emailAddress;
    private String telephoneNumber;
    private String donKnowTelephoneNumber;
}
