package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "BundleNestedSubfolder1", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundleNestedSubfolder1Details {
    @CCD(label = "Subfolder Name", searchable = false)
    private String name;
    @CCD(
            label = "Sub Folder Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "BundleDocument"
    )
    private List<BundleDocument> documents;

    @JsonCreator
    public BundleNestedSubfolder1Details(@JsonProperty("name") String name,
                                         @JsonProperty("documents") List<BundleDocument> documents) {
        this.name = name;
        this.documents = documents;
    }
}