package uk.gov.hmcts.reform.prl.models.dto.cafcass.manageorder;
import uk.gov.hmcts.reform.prl.enums.YesNoNotApplicable;
import uk.gov.hmcts.reform.prl.enums.manageorders.ServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serveorder.LocalAuthorityDocumentsEnum;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.serveorder.CafcassCymruDocumentsEnum;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.prl.models.dto.ccd.PostalInformation;
import uk.gov.hmcts.reform.prl.models.dto.ccd.EmailInformation;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.ServedParties;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Builder(toBuilder = true)
public class ServeOrderDetails {
    @CCD(label = " ", showCondition = "additionalDocuments=\"DO_NOT_SHOW\"", searchable = false)
    @JsonProperty("whenReportsMustBeFiled")
    private String whenReportsMustBeFiled;

    @CCD(label = " ", showCondition = "additionalDocuments=\"DO_NOT_SHOW\"", searchable = false)
    private List<CafcassCymruDocumentsEnum> cafcassCymruDocuments;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", showCondition = "additionalDocuments=\"DO_NOT_SHOW\"", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> additionalDocuments;
  @CCD(label = " ", showCondition = "additionalDocuments=\"DO_NOT_SHOW\"", searchable = false)
  private YesNoNotApplicable serveOnRespondent;
  @CCD(label = "Recipients", showCondition = "additionalDocuments=\"DO_NOT_SHOW\"", searchable = false)
  private ServingRespondentsEnum servingRespondent;
  @CCD(label = "Recipients", searchable = false)
  private String recipientsOptions;
  @CCD(label = "Other people", searchable = false)
  private String otherParties;
  @CCD(label = "Provide Cafcass access to this case?", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo cafcassServed;
  @CCD(label = "Cafcass email", showCondition = "additionalDocuments=\"DO_NOT_SHOW\"", searchable = false)
  private String cafcassEmail;
  @CCD(label = "Cafcass cymru email", showCondition = "additionalDocuments=\"DO_NOT_SHOW\"", searchable = false)
  private String cafcassCymruEmail;
  @CCD(label = " ", showCondition = "additionalDocuments=\"DO_NOT_SHOW\"", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo otherPartiesServed;
  @CCD(label = " ", showCondition = "additionalDocuments=\"DO_NOT_SHOW\"", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<PostalInformation>> postalInformation;
  @CCD(label = " ", showCondition = "additionalDocuments=\"DO_NOT_SHOW\"", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<EmailInformation>> emailInformation;
  @CCD(label = " ", showCondition = "additionalDocuments=\"DO_NOT_SHOW\"", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<ServedParties>> servedParties;
  @CCD(label = " ", showCondition = "additionalDocuments=\"DO_NOT_SHOW\"", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo cafcassOrCymruNeedToProvideReport;
  @CCD(label = " ", showCondition = "additionalDocuments=\"DO_NOT_SHOW\"", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo orderEndsInvolvementOfCafcassOrCymru;
  @CCD(label = " ", showCondition = "additionalDocuments=\"DO_NOT_SHOW\"", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo doYouWantToServeOrder;
  @CCD(label = " ", showCondition = "additionalDocuments=\"DO_NOT_SHOW\"", searchable = false)
  private String whatDoWithOrder;
  @CCD(label = "Recipients", searchable = false)
  private String servingRecipientName;
  @CCD(label = "Is Cafcass Cymru involved?", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo cafcassCymruServed;
  @CCD(label = "Other Organisations", searchable = false)
  private String organisationsName;
  @CCD(label = "Who is responsible for serving the respondent?", searchable = false)
  private ServingRespondentsEnum courtPersonalService;
  @CCD(label = " ", showCondition = "additionalDocuments=\"DO_NOT_SHOW\"", searchable = false)
  private String whoIsResponsibleToServe;
  @CCD(label = " ", showCondition = "additionalDocuments=\"DO_NOT_SHOW\"", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo multipleOrdersServed;
  @CCD(label = " ", showCondition = "additionalDocuments=\"DO_NOT_SHOW\"", searchable = false)
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo localAuthorityNeedToProvideReport;
  @CCD(label = " ", showCondition = "additionalDocuments = \"DO_NOT_SHOW\"", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<String>> cirDocumentsRequested;
  @CCD(label = " ", showCondition = "additionalDocuments=\"DO_NOT_SHOW\"", searchable = false)
  private java.util.Set<LocalAuthorityDocumentsEnum> localAuthorityMultipleDocuments;
  @CCD(label = " ", showCondition = "additionalDocuments=\"DO_NOT_SHOW\"", searchable = false)
  private String whenReportsMustBeFiledByLocalAuthority;
  // ==== end synthesised definition-only fields ====
}
