package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
public class BundleDetails {
    private String id;
    private String title;
    private String description;
    private String eligibleForStitching;
    private String eligibleForCloning;
    private List<BundleDocument> documents;
    private List<BundleFolder> folders;
    private String stitchStatus;
    private DocumentLink stitchedDocument;
    private String hasCoversheets;
    private String hasTableOfContents;
    private String hasFolderCoversheets;
    private String stitchingFailureMessage;
    private String fileName;
    private String paginationStyle;
    private String coverpageTemplate;
    private String pageNumberFormat;


    @JsonCreator
    public BundleDetails(@JsonProperty("id") String id,
                         @JsonProperty("title") String title,
                         @JsonProperty("description") String description,
                         @JsonProperty("eligibleForStitching") String eligibleForStitching,
                         @JsonProperty("eligibleForCloning") String eligibleForCloning,
                         @JsonProperty("documents") List<BundleDocument> documents,
                         @JsonProperty("folders") List<BundleFolder> folders,
                         @JsonProperty("stitchStatus") String stitchStatus,
                         @JsonProperty("stitchedDocument") DocumentLink stitchedDocument,
                         @JsonProperty("hasCoversheets") String hasCoversheets,
                         @JsonProperty("hasTableOfContents") String hasTableOfContents,
                         @JsonProperty("hasFolderCoversheets") String hasFolderCoversheets,
                         @JsonProperty("stitchingFailureMessage") String stitchingFailureMessage,
                         @JsonProperty("fileName") String fileName,
                         @JsonProperty("paginationStyle") String paginationStyle,
                         @JsonProperty("coverpageTemplate") String coverpageTemplate,
                         @JsonProperty("pageNumberFormat") String pageNumberFormat) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.eligibleForStitching = eligibleForStitching;
        this.eligibleForCloning = eligibleForCloning;
        this.documents = documents;
        this.folders = folders;
        this.stitchStatus = stitchStatus;
        this.stitchedDocument = stitchedDocument;
        this.hasCoversheets = hasCoversheets;
        this.hasTableOfContents = hasTableOfContents;
        this.hasFolderCoversheets = hasFolderCoversheets;
        this.stitchingFailureMessage = stitchingFailureMessage;
        this.fileName = fileName;
        this.paginationStyle = paginationStyle;
        this.coverpageTemplate = coverpageTemplate;
        this.pageNumberFormat = pageNumberFormat;
    }
}