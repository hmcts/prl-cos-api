package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleDocument;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleFolder;
import uk.gov.hmcts.reform.prl.models.dto.ccd.PaginationStyle;
import uk.gov.hmcts.reform.prl.models.dto.ccd.PageNumberFormat;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;


@ComplexType(name = "Bundle", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
public class BundleDetails {
    @CCD(label = "Bundle ID", searchable = false)
    private String id;
    @CCD(label = "Config used for bundle", showCondition = "title=\"DUMMY_VALUE_TO_HIDE_FIELD\"", searchable = false)
    private String title;
    @CCD(label = "Description", searchable = false, typeOverride = FieldType.TextArea)
    private String description;
    @CCD(label = "Stitch status", searchable = false)
    private String stitchStatus;
    @CCD(label = "Stitched document", categoryID = "courtBundle", searchable = false, typeOverride = FieldType.Document)
    private DocumentLink stitchedDocument;
    @CCD(label = "Historical stitched document", searchable = false, typeOverride = FieldType.Document)
    private DocumentLink historicalStitchedDocument;


    @JsonCreator
    public BundleDetails(@JsonProperty("id") String id,
                         @JsonProperty("title") String title,
                         @JsonProperty("description") String description,
                         @JsonProperty("stitchStatus") String stitchStatus,
                         @JsonProperty("stitchedDocument") DocumentLink stitchedDocument,
                         @JsonProperty("historicalStitchedDocument") DocumentLink historicalStitchedDocument,
                         @JsonProperty("bundleStatusInfo") String bundleStatusInfo,
                         @JsonProperty("documents") java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<BundleDocument>> documents,
                         @JsonProperty("folders") java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<BundleFolder>> folders,
                         @JsonProperty("stitchingFailureMessage") String stitchingFailureMessage,
                         @JsonProperty("eligibleForStitching") uk.gov.hmcts.ccd.sdk.type.YesOrNo eligibleForStitching,
                         @JsonProperty("eligibleForCloning") uk.gov.hmcts.ccd.sdk.type.YesOrNo eligibleForCloning,
                         @JsonProperty("hasCoversheets") uk.gov.hmcts.ccd.sdk.type.YesOrNo hasCoversheets,
                         @JsonProperty("hasTableOfContents") uk.gov.hmcts.ccd.sdk.type.YesOrNo hasTableOfContents,
                         @JsonProperty("hasFolderCoversheets") uk.gov.hmcts.ccd.sdk.type.YesOrNo hasFolderCoversheets,
                         @JsonProperty("fileName") String fileName,
                         @JsonProperty("paginationStyle") PaginationStyle paginationStyle,
                         @JsonProperty("coverpageTemplate") String coverpageTemplate,
                         @JsonProperty("pageNumberFormat") PageNumberFormat pageNumberFormat) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.stitchStatus = stitchStatus;
        this.stitchedDocument = stitchedDocument;
        this.historicalStitchedDocument = historicalStitchedDocument;

        this.bundleStatusInfo = bundleStatusInfo;
        this.documents = documents;
        this.folders = folders;
        this.stitchingFailureMessage = stitchingFailureMessage;
        this.eligibleForStitching = eligibleForStitching;
        this.eligibleForCloning = eligibleForCloning;
        this.hasCoversheets = hasCoversheets;
        this.hasTableOfContents = hasTableOfContents;
        this.hasFolderCoversheets = hasFolderCoversheets;
        this.fileName = fileName;
        this.paginationStyle = paginationStyle;
        this.coverpageTemplate = coverpageTemplate;
        this.pageNumberFormat = pageNumberFormat;
    }

    /** Retained so existing positional call sites still compile. */
    public BundleDetails(String id,
                         String title,
                         String description,
                         String stitchStatus,
                         DocumentLink stitchedDocument,
                         DocumentLink historicalStitchedDocument) {
        this(id, title, description, stitchStatus, stitchedDocument, historicalStitchedDocument, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "<div class='govuk-warning-text'><span class='govuk-warning-text__icon' aria-hidden='true'>!</span><strong class='govuk-warning-text__text'>Bundle generation is in progress.. Please refresh the page manually to see the bundle pdf if the Stitch Status is NEW in the below details</strong></div>",
          showCondition = "stitchStatus=\"NEW\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String bundleStatusInfo;
  @CCD(label = "Bundle document", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<BundleDocument>> documents;
  @CCD(label = "Bundle folder", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<BundleFolder>> folders;
  @CCD(label = "Error from Stiching service", searchable = false)
  private String stitchingFailureMessage;
  @CCD(label = "Is this the bundle you want to amend?", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo eligibleForStitching;
  @CCD(
          label = "Is this the bundle you want to clone?",
          showCondition = "eligibleForCloning = \"DO_NOT_SHOW\"",
          searchable = false
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo eligibleForCloning;
  @CCD(label = "Should this bundle have coversheets separating each document?", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hasCoversheets;
  @CCD(label = "Should this bundle have a title page with a table of contents?", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hasTableOfContents;
  @CCD(label = "Should this bundle’s folders have a coversheet?", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hasFolderCoversheets;
  @CCD(label = "Name of the PDF", searchable = false)
  private String fileName;
  @CCD(
          label = "Pagination Style",
          searchable = false,
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "paginationStyle"
  )
  private PaginationStyle paginationStyle;
  @CCD(label = "Cover page template", searchable = false)
  private String coverpageTemplate;
  @CCD(
          label = "Page Number Format",
          searchable = false,
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "pageNumberFormat"
  )
  private PageNumberFormat pageNumberFormat;
  // ==== end synthesised definition-only fields ====
}
