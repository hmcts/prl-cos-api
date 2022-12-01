package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundleNestedSubfolder4Details {
    private String name;
    private List<BundleNestedSubfolder5> folders;
    private List<BundleDocument> documents;

    @JsonCreator
    public BundleNestedSubfolder4Details(@JsonProperty("name") String name, @JsonProperty("folders") List<BundleNestedSubfolder5> folders,
                                         @JsonProperty("documents") List<BundleDocument> documents) {
        this.name = name;
        this.folders = folders;
        this.documents = documents;
    }
}
