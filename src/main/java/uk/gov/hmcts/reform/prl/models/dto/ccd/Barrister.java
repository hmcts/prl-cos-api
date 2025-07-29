package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Barrister {

    private UUID barristerOrgUuid;
    private UUID barristerPartyId;
    private final String barristerFirstName;
    private final String barristerLastName;

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
