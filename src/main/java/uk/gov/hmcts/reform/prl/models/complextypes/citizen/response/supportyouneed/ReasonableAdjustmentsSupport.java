package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.supportyouneed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.citizen.AttendingToCourtEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.CourtComfortEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.CourtHearingEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.DocsSupportEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.HelpCommunicationEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.LanguageRequirementsEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.ReasonableAdjustmentsEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.SafetyArrangementsEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.TravellingToCourtEnum;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class ReasonableAdjustmentsSupport {
    @CCD(label = " ", searchable = false)
    private final List<HelpCommunicationEnum> helpCommunication;
    @CCD(label = " ", searchable = false)
    private final String describeOtherNeed;
    @CCD(label = " ", searchable = false)
    private final List<CourtComfortEnum> courtComfort;
    @CCD(label = " ", searchable = false)
    private final String otherProvideDetails;
    @CCD(label = " ", searchable = false)
    private final List<CourtHearingEnum> courtHearing;
    @CCD(label = " ", searchable = false)
    private final String communicationSupportOther;
    @CCD(label = " ", searchable = false)
    private final String languageDetails;
    @CCD(label = " ", searchable = false)
    private final List<ReasonableAdjustmentsEnum> reasonableAdjustments;
    @CCD(label = " ", searchable = false)
    private final List<DocsSupportEnum> docsSupport;
    @CCD(label = " ", searchable = false)
    private final String otherDetails;
    @CCD(label = " ", searchable = false)
    private final List<LanguageRequirementsEnum> languageRequirements;
    @CCD(label = " ", searchable = false)
    private final List<SafetyArrangementsEnum> safetyArrangements;
    @CCD(label = " ", searchable = false)
    private final String safetyArrangementsDetails;
    @CCD(label = " ", searchable = false)
    private final List<TravellingToCourtEnum> travellingToCourt;
    @CCD(label = " ", searchable = false)
    private final String travellingOtherDetails;
    @CCD(label = " ", searchable = false)
    private final List<AttendingToCourtEnum> attendingToCourt;
    @CCD(label = " ", searchable = false)
    private final String hearingDetails;
    @CCD(label = " ", searchable = false)
    private final String signLanguageDetails;
    @CCD(label = " ", searchable = false)
    private final String lightingDetails;
    @CCD(label = " ", searchable = false)
    private final String supportWorkerDetails;
    @CCD(label = " ", searchable = false)
    private final String familyProviderDetails;
    @CCD(label = " ", searchable = false)
    private final String therapyDetails;
    @CCD(label = " ", searchable = false)
    private final String docsDetails;
    @CCD(label = " ", searchable = false)
    private final String largePrintDetails;
    @CCD(label = " ", searchable = false)
    private final String parkingDetails;
    @CCD(label = " ", searchable = false)
    private final String differentChairDetails;
}
