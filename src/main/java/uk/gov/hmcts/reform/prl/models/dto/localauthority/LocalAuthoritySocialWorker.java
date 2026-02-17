package uk.gov.hmcts.reform.prl.models.dto.localauthority;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Organisation;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LocalAuthoritySocialWorker {
    public static final String FULL_NAME_FORMAT = "%s %s";

    private String userId;

    @JsonProperty("laSocialWorkerFirstName")
    private final String laSocialWorkerFirstName;

    @JsonProperty("laSocialWorkerLastName")
    private final String laSocialWorkerLastName;

    @JsonProperty("laSocialWorkerEmail")
    private final String laSocialWorkerEmail;

    @JsonProperty("laSocialWorkerOrg")
    private final Organisation laSocialWorkerOrg;

    @JsonProperty("laSocialWorkerFullName")
    private final String laSocialWorkerFullName;

    @JsonIgnore
    public String getLaSocialWorkerFullName() {
        return String.format(
            FULL_NAME_FORMAT,
            this.laSocialWorkerFirstName,
            this.laSocialWorkerLastName
        );
    }
}
