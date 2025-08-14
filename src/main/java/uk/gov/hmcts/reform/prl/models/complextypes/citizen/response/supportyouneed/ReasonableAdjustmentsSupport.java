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

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class ReasonableAdjustmentsSupport {
    private final List<HelpCommunicationEnum> helpCommunication;
    private final String describeOtherNeed;
    private final List<CourtComfortEnum> courtComfort;
    private final String otherProvideDetails;
    private final List<CourtHearingEnum> courtHearing;
    private final String communicationSupportOther;
    private final String languageDetails;
    private final List<ReasonableAdjustmentsEnum> reasonableAdjustments;
    private final List<DocsSupportEnum> docsSupport;
    private final String otherDetails;
    private final List<LanguageRequirementsEnum> languageRequirements;
    private final List<SafetyArrangementsEnum> safetyArrangements;
    private final String safetyArrangementsDetails;
    private final List<TravellingToCourtEnum> travellingToCourt;
    private final String travellingOtherDetails;
    private final List<AttendingToCourtEnum> attendingToCourt;
    private final String hearingDetails;
    private final String signLanguageDetails;
    private final String lightingDetails;
    private final String supportWorkerDetails;
    private final String familyProviderDetails;
    private final String therapyDetails;
    private final String docsDetails;
    private final String largePrintDetails;
    private final String parkingDetails;
    private final String differentChairDetails;
}
