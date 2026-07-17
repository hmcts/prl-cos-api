package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Barrister {

    @CCD(label = " ", searchable = false)
    private Organisation barristerOrg;
    @CCD(label = "Barrister Id", searchable = false)
    private String barristerId;
    @CCD(label = "Barrister first name", searchable = false)
    private final String barristerFirstName;
    @CCD(label = "Barrister last name", searchable = false)
    private final String barristerLastName;
    @CCD(label = "Barrister email", searchable = false)
    private final String barristerEmail;
    @CCD(label = " ", searchable = false)
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
