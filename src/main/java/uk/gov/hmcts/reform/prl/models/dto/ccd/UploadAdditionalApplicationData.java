package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.AdditionalApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2ApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadAdditionalApplicationData {

    private final List<AdditionalApplicationTypeEnum> additionalApplicationsApplyingFor;
    private final C2ApplicationTypeEnum typeOfC2Application;
    private final DynamicMultiSelectList additionalApplicantsList;
    private final C2DocumentBundle temporaryC2Document;
    private final OtherApplicationsBundle temporaryOtherApplicationsBundle;
    private final String additionalApplicationFeesToPay;
    private final YesOrNo additionalApplicationsHelpWithFees;
    private final String additionalApplicationsHelpWithFeesNumber;
    private final String representedPartyType;
}
