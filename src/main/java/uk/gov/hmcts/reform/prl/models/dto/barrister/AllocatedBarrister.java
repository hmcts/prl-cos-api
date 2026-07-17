package uk.gov.hmcts.reform.prl.models.dto.barrister;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AllocatedBarrister {
    public static final String FULL_NAME_FORMAT = "%s %s";

    @CCD(label = "Parties", searchable = false, typeOverride = FieldType.DynamicRadioList)
    @JsonProperty("partyList")
    private final DynamicList partyList;

    @CCD(label = "First name of the barrister", searchable = false)
    @JsonProperty("barristerFirstName")
    private final String barristerFirstName;

    @CCD(label = "Last name of the barrister", searchable = false)
    @JsonProperty("barristerLastName")
    private final String barristerLastName;

    @CCD(label = "Email of the barrister", searchable = false, typeOverride = FieldType.Email)
    @JsonProperty("barristerEmail")
    private final String barristerEmail;

    @CCD(label = "Organisation Search", searchable = false)
    @JsonProperty("barristerOrg")
    private final Organisation barristerOrg;

    @CCD(label = "Email of the solicitor", searchable = false, typeOverride = FieldType.Email)
    @JsonProperty("solicitorEmail")
    private final String solicitorEmail;

    @CCD(label = "Solicitor's full name", searchable = false)
    @JsonProperty("solicitorFullName")
    private final String solicitorFullName;

    @JsonIgnore
    public String getBarristerFullName() {
        return String.format(
            FULL_NAME_FORMAT,
            this.barristerFirstName,
            this.barristerLastName
        );
    }
}
