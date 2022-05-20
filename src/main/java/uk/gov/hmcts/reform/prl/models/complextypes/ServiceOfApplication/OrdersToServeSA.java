package uk.gov.hmcts.reform.prl.models.complextypes.ServiceOfApplication;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ServiceOfApplication.AmendDischargedVariedEnum;
import uk.gov.hmcts.reform.prl.enums.ServiceOfApplication.AppointmentOfGuardianEnum;
import uk.gov.hmcts.reform.prl.enums.ServiceOfApplication.BlankOrderEnum;
import uk.gov.hmcts.reform.prl.enums.ServiceOfApplication.BlankOrderOrDirectionsEnum;
import uk.gov.hmcts.reform.prl.enums.ServiceOfApplication.BlankOrderOrDirectionsWithdrawEnum;
import uk.gov.hmcts.reform.prl.enums.ServiceOfApplication.ChildArrangementsSpecificProhibitedOrderEnum;
import uk.gov.hmcts.reform.prl.enums.ServiceOfApplication.GeneralFormUndertakingEnum;
import uk.gov.hmcts.reform.prl.enums.ServiceOfApplication.NonMolestationEnum;
import uk.gov.hmcts.reform.prl.enums.ServiceOfApplication.NoticeOfProceedingsEnum;
import uk.gov.hmcts.reform.prl.enums.ServiceOfApplication.NoticeOfProceedingsNonPartiesEnum;
import uk.gov.hmcts.reform.prl.enums.ServiceOfApplication.NoticeOfProceedingsPartiesEnum;
import uk.gov.hmcts.reform.prl.enums.ServiceOfApplication.OccupationEnum;
import uk.gov.hmcts.reform.prl.enums.ServiceOfApplication.OtherUploadAnOrderEnum;
import uk.gov.hmcts.reform.prl.enums.ServiceOfApplication.ParentalResponsibilityEnum;
import uk.gov.hmcts.reform.prl.enums.ServiceOfApplication.PowerOfArrestEnum;
import uk.gov.hmcts.reform.prl.enums.ServiceOfApplication.SpecialGuardianShipEnum;
import uk.gov.hmcts.reform.prl.enums.ServiceOfApplication.StandardDirectionsOrderEnum;
import uk.gov.hmcts.reform.prl.enums.ServiceOfApplication.TransferOfCaseToAnotherCourtEnum;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@Builder
public class OrdersToServeSA {
    private final String sentDocumentPlaceHolder;

    private final List<StandardDirectionsOrderEnum> standardDirectionsOrderOption;
    private final List<BlankOrderOrDirectionsEnum>    blankOrderOrDirectionsOption;

    private final List<BlankOrderOrDirectionsWithdrawEnum> blankOrderOrDirectionsWithdrawOption;
    private final List<ChildArrangementsSpecificProhibitedOrderEnum>    childArrangementSpecificOrderOption;

    private final List<ParentalResponsibilityEnum> parentalResponsibilityOption;
    private final List<SpecialGuardianShipEnum>    specialGuardianShipOption;

    private final List<NoticeOfProceedingsPartiesEnum> noticeOfProceedingsPartiesOption;
    private final List<NoticeOfProceedingsNonPartiesEnum>    noticeOfProceedingsNonPartiesOption;

    private final List<TransferOfCaseToAnotherCourtEnum> transferOfCaseToAnotherCourtOption;
    private final List<AppointmentOfGuardianEnum>    appointmentOfGuardianOption;

    private final List<NonMolestationEnum> nonMolestationOption;
    private final List<OccupationEnum>    occupationOption;

    private final List<PowerOfArrestEnum> powerOfArrestOption;
    private final List<AmendDischargedVariedEnum>    amendDischargedVariedOption;

    private final List<BlankOrderEnum> blankOrderEnumOption;
    private final List<GeneralFormUndertakingEnum>    generalFormUndertakingOption;

    private final List<NoticeOfProceedingsEnum> noticeOfProceedingsEnumOption;
    private final List<OtherUploadAnOrderEnum>    otherUploadAnOrderOption;

    private final Document pd36qLetter;
    private final Document specialArrangementsLetter;
    private final Document additionalDocuments;
}
