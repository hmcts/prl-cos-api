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

@ComplexType(name = "BundleSubfolder", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundleSubfolderDetails {
    @CCD(label = "Subfolder Name", searchable = false)
    private String name;

    @CCD(
            label = "Folder Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "BundleNestedSubfolder1"
    )
    private List<BundleNestedSubfolder1> folders;
    @CCD(
            label = "Folder Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "BundleDocument"
    )
    private List<BundleDocument> documents;

    @JsonCreator
    public BundleSubfolderDetails(@JsonProperty("name") String name,
                                  List<BundleNestedSubfolder1> folders, @JsonProperty("documents") List<BundleDocument> documents) {
        this.name = name;
        this.folders = folders;
        this.documents = documents;
    }
}