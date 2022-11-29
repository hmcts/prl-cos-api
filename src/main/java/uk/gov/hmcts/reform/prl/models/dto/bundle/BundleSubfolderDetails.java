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
public class BundleSubfolderDetails {
    private String name;

    private List<BundleSubfolder> folders;
    private List<BundleDocument> documents;

    @JsonCreator
    public BundleSubfolderDetails(@JsonProperty("name") String name,@JsonProperty("folders") List<BundleSubfolder> folders,
                                  @JsonProperty("documents") List<BundleDocument> documents) {
        this.name = name;
        this.folders = folders;
        this.documents = documents;
    }
}
