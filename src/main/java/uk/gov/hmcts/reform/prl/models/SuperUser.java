package uk.gov.hmcts.reform.prl.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperUser {
    private String email;
    private String firstName;
    private String lastName;
}
