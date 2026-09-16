package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LegalAdviserChecker {

    public String returnLegalAdviserNameForManageOrders(CaseData caseData){
        if (!isLegalAdviserPresent(caseData.getManageOrders())) {
            return null;
        } else {
            if (caseData.getManageOrders().getLegalAdviserToReviewOrder() != null){
                return String.valueOf(caseData.getManageOrders().getLegalAdviserToReviewOrder());
            } else {
                return String.valueOf(caseData.getManageOrders().getNameOfLaToReviewOrder());
            }
        }
    }

    private boolean isLegalAdviserPresent(ManageOrders manageOrders){
        return manageOrders != null &&
            (manageOrders.getNameOfLaToReviewOrder() != null
                || manageOrders.getLegalAdviserToReviewOrder() != null);
    }
}
