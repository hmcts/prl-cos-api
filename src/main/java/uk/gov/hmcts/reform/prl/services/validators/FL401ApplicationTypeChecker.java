package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.LinkToCA;
import uk.gov.hmcts.reform.prl.models.complextypes.Orders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.TYPE_OF_APPLICATION_ERROR;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Service
public class FL401ApplicationTypeChecker implements EventChecker {

    @Autowired
    private TaskErrorService taskErrorService;

    public boolean isStarted(CaseData caseData) {
        return anyNonEmpty(
            caseData.getTypeOfApplicationOrders()
        );
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    @Override
    public boolean isFinished(CaseData caseData) {

        Optional<Orders> ordersOptional = ofNullable(caseData.getTypeOfApplicationOrders());
        Optional<LinkToCA> applicationTypeLinkToCA = ofNullable(caseData.getTypeOfApplicationLinkToCA());

        boolean finished;

        if (ordersOptional.isPresent() && (ordersOptional.get().getOrderType().contains(FL401OrderTypeEnum.nonMolestationOrder)
            || ordersOptional.get().getOrderType().contains(FL401OrderTypeEnum.occupationOrder))) {

            if (applicationTypeLinkToCA.isPresent() && applicationTypeLinkToCA.get().getLinkToCaApplication().equals(
                YesOrNo.Yes)) {
                finished = applicationTypeLinkToCA.get().getChildArrangementsApplicationNumber() != null;
            } else {
                return false;
            }

            if (finished) {
                taskErrorService.removeError(TYPE_OF_APPLICATION_ERROR);
                return true;
            }
            taskErrorService.addEventError(
                TYPE_OF_APPLICATION,
                TYPE_OF_APPLICATION_ERROR,
                TYPE_OF_APPLICATION_ERROR.getError()
            );
        }

        return false;
    }

}
