package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;


@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
public class BundleDetails {
    private String id;
    private String title;
    private String description;
    private String stitchStatus;
    private DocumentLink stitchedDocument;
    private DocumentLink historicalStitchedDocument;


    @JsonCreator
    public BundleDetails(@JsonProperty("id") String id,
                         @JsonProperty("title") String title,
                         @JsonProperty("description") String description,
                         @JsonProperty("stitchStatus") String stitchStatus,
                         @JsonProperty("stitchedDocument") DocumentLink stitchedDocument,
                         @JsonProperty("historicalStitchedDocument") DocumentLink historicalStitchedDocument) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.stitchStatus = stitchStatus;
        this.stitchedDocument = stitchedDocument;
        this.historicalStitchedDocument = historicalStitchedDocument;

    }
}
