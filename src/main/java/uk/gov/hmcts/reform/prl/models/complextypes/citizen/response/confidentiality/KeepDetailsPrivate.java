package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesNoIDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class KeepDetailsPrivate {
    @CCD(
            label = "Do the other people named in this application (the applicants) \nknow any of your contact details?\n",
            searchable = false
    )
    private final YesNoIDontKnow otherPeopleKnowYourContactDetails;
    @CCD(
            label = "Do you want to keep your contact details private from the other people named in the application (the applicants)?  \nThe answers you give in your response will be shared with the other people named in this application (the applicants). This will include your contact details.  \nFor example, if you believe the other people in the case pose a risk to you or the children, you can ask the court to keep your contact details private. ",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo confidentiality;
    @CCD(label = " ", showCondition = "confidentiality=\"Yes\"", searchable = false)
    private final List<ConfidentialityListEnum> confidentialityList;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "Do you want to keep your contact details private from the other people named in the application (the applicants)? ",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String confidentialityHeading;
  @CCD(
          label = "The answers you give in your response will be shared with the other people named in this application (the applicants). This will include your contact details. ",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String confidentialityHintLine1;
  @CCD(
          label = "For example, if you believe the other people in the case pose a risk to you or the children, you can ask the court to keep your contact details private.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String confidentialityHintLine2;
  @CCD(
          label = "### Specify which contact details you want to keep private.  \nMake sure you only select details the applicants do not already know.",
          showCondition = "confidentiality=\"Yes\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String confidentialityListLabel;
  // ==== end synthesised definition-only fields ====
}
