package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
@Jacksonized
public class LinkToCA {

    @CCD(label = "*Is this linked to a C100 application?", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("linkToCaApplication")
    private final YesOrNo linkToCaApplication;
    @CCD(
            label = "Child Arrangements Case Number (FamilyMan cases not supported)",
            regex = "^[0-9]{9,}$",
            searchable = false
    )
    @JsonProperty("caApplicationNumber")
    private final String caApplicationNumber;

    @JsonCreator
    public LinkToCA(YesOrNo linkToCaApplication, String childArrangementsApplicationNumber) {
        this.linkToCaApplication = linkToCaApplication;
        this.caApplicationNumber = childArrangementsApplicationNumber;
    }
}
