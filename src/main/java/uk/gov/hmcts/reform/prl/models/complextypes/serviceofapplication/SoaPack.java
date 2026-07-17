package uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SoaPack {

    @CCD(label = "Document", searchable = false)
    private final List<Element<Document>> packDocument;

    @CCD(
            label = " ",
            showCondition = "partyIds=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Text"
    )
    private final List<Element<String>> partyIds;

    @CCD(label = "Served by", searchable = false)
    private final String servedBy;

    @CCD(label = "Pack created date", searchable = false)
    private final String packCreatedDate;

    @CCD(label = " ", showCondition = "partyIds=\"DO_NOT_SHOW\"", searchable = false)
    private final String personalServiceBy;

    @CCD(label = "Cover letter mapping", showCondition = "partyIds=\"DO_NOT_SHOW\"", searchable = false)
    @JsonProperty("coverLettersMap")
    private final List<Element<CoverLetterMap>> coverLettersMap;

    @CCD(label = "Email address", searchable = false)
    private final String servedPartyEmail;
}
