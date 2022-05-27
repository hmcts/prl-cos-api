package uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.AmendDischargedVariedEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.AppointmentOfGuardianEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.BlankOrderEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.BlankOrderOrDirectionsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.BlankOrderOrDirectionsWithdrawEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.ChildArrangementsSpecificProhibitedOrderEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.GeneralFormUndertakingEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.NonMolestationEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.NoticeOfProceedingsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.NoticeOfProceedingsNonPartiesEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.NoticeOfProceedingsPartiesEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.OccupationEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.OtherUploadAnOrderEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.ParentalResponsibilityEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.PowerOfArrestEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SpecialGuardianShipEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.StandardDirectionsOrderEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.TransferOfCaseToAnotherCourtEnum;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public List<String> getSelectedOrders() {
        return Stream.of(OrdersToServeSA.class.getDeclaredFields()).filter(Objects::nonNull)
            .map(field -> {
                try {
                    return field.get(this);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return null;
            }).filter(Objects::nonNull)
            .map(Object::toString)
            .map(s -> s.substring(1, s.length() - 1))
            .collect(Collectors.toList());
    }

}
