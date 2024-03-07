package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.LinkToCA;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.FL401_TYPE_OF_APPLICATION_ERROR;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FL401ApplicationTypeChecker implements EventChecker {
    private final TaskErrorService taskErrorService;

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

        Optional<TypeOfApplicationOrders> ordersOptional = ofNullable(caseData.getTypeOfApplicationOrders());
        Optional<LinkToCA> applicationTypeLinkToCA = ofNullable(caseData.getTypeOfApplicationLinkToCA());

        boolean finished = false;

        if (ordersOptional.isPresent() && (ordersOptional.get().getOrderType().contains(FL401OrderTypeEnum.nonMolestationOrder)
            || ordersOptional.get().getOrderType().contains(FL401OrderTypeEnum.occupationOrder))) {

            if (applicationTypeLinkToCA.isPresent()) {
                if (applicationTypeLinkToCA.get().getLinkToCaApplication().equals(
                    YesOrNo.Yes)) {
                    finished = applicationTypeLinkToCA.get().getCaApplicationNumber() != null;
                } else if (applicationTypeLinkToCA.get().getLinkToCaApplication().equals(
                    YesOrNo.No)) {
                    taskErrorService.removeError(FL401_TYPE_OF_APPLICATION_ERROR);
                    return true;
                }
            } else {
                taskErrorService.addEventError(
                    FL401_TYPE_OF_APPLICATION,
                    FL401_TYPE_OF_APPLICATION_ERROR,
                    FL401_TYPE_OF_APPLICATION_ERROR.getError()
                );
                return false;
            }

            if (finished) {
                taskErrorService.removeError(FL401_TYPE_OF_APPLICATION_ERROR);
                return true;
            }
            taskErrorService.addEventError(
                FL401_TYPE_OF_APPLICATION,
                FL401_TYPE_OF_APPLICATION_ERROR,
                FL401_TYPE_OF_APPLICATION_ERROR.getError()
            );
        }
        taskErrorService.addEventError(
            FL401_TYPE_OF_APPLICATION,
            FL401_TYPE_OF_APPLICATION_ERROR,
            FL401_TYPE_OF_APPLICATION_ERROR.getError()
        );
        return false;
    }

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {
        return TaskState.NOT_STARTED;
    }

}
