package uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.CafcassServiceApplicationEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfirmRecipients {
    @CCD(
            label = "Which other people in the case should receive the order? (optional)",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            typeParameterOverride = "OtherOrderRecipientsEnum"
    )
    @JsonProperty("otherPeopleList")
    private final DynamicMultiSelectList otherPeopleList;
    @CCD(label = " ", hint = "Add the email address of the Cafcass support officer.", searchable = false)
    private final List<CafcassServiceApplicationEnum> cafcassEmailOptionChecked;
    @CCD(
            label = " ",
            hint = "For example, add the email address of a local authority representative.",
            searchable = false
    )
    private final List<OtherEnum> otherEmailOptionChecked;
    @CCD(
            label = "Email address",
            hint = "Add the email address of the Cafcass support officer.",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Email"
    )
    private final List<Element<String>> cafcassEmailAddressList;
    @CCD(
            label = "Email address",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Email"
    )
    private final List<Element<String>> otherEmailAddressList;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "Check if there are restrictions on who should receive the order.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String checkRestrictionsLabel;
  @CCD(label = "Confirm recipients", searchable = false, typeOverride = FieldType.Label)
  private String confirmRecipientsLabel;
  @CCD(
          label = "If the applicant or respondent are represented by a solicitor, then the order is sent to the solicitor directly.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String orderSentToSolicitorLabel;
  @CCD(
          label = "Applicant(s)",
          searchable = false,
          typeOverride = FieldType.DynamicMultiSelectList,
          typeParameterOverride = "OrderRecipientsEnum"
  )
  private String applicantsList;
  @CCD(
          label = "Respondent(s)",
          searchable = false,
          typeOverride = FieldType.DynamicMultiSelectList,
          typeParameterOverride = "OrderRecipientsEnum"
  )
  private String respondentsList;
  @CCD(
          label = "Applicant Solicitor(s)",
          searchable = false,
          typeOverride = FieldType.DynamicMultiSelectList,
          typeParameterOverride = "OrderRecipientsEnum"
  )
  private String applicantSolicitorList;
  @CCD(
          label = "Respondent Solicitor(s)",
          searchable = false,
          typeOverride = FieldType.DynamicMultiSelectList,
          typeParameterOverride = "OrderRecipientsEnum"
  )
  private String respondentSolicitorList;
  // ==== end synthesised definition-only fields ====
}
