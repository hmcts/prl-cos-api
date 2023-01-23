package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.AdditionalApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2ApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.models.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.UploadAdditionalApplicationBundle;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadAdditionalApplicationData {

    private final List<AdditionalApplicationTypeEnum> additionalApplicationsApplyingFor;
    private final C2ApplicationTypeEnum typeOfC2Application;
    private final DynamicList applicantsList;
    private final C2DocumentBundle temporaryC2Document;
    private final OtherApplicationsBundle temporaryOtherApplicationsBundle;

    //ccd-config not added for below
    private final UploadAdditionalApplicationBundle uploadAdditionalApplicationBundle;
}
