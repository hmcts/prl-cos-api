package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Barrister {

    private String barristerOrgId;
    private String barristerId;
    private final String barristerFirstName;
    private final String barristerLastName;
    private final String barristerEmail;
    private final String barristerRole;

    public static final String FULL_NAME_FORMAT = "%s %s";

    @JsonIgnore
    public String getBarristerFullName() {
        return String.format(
            FULL_NAME_FORMAT,
            this.barristerFirstName,
            this.barristerLastName
        );
    }

}
