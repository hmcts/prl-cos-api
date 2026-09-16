package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.staff.StaffUser;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;

public class LegalAdviserCheckerTest {

    private final LegalAdviserChecker legalAdviserChecker = new LegalAdviserChecker();

    @Test
    public void checkNullManageOrdersReturnsNull(){

        String result = legalAdviserChecker.returnLegalAdviserNameForManageOrders(
            CaseData.builder().manageOrders(null).build()
        );
        Assertions.assertNull(result);
    }

    @Test
    public void checkNullLegalAdviserListAndNullLegalAdviserUserReturnsNull(){

        String result = legalAdviserChecker.returnLegalAdviserNameForManageOrders(
            CaseData.builder().manageOrders(
            ManageOrders.builder().build()
            ).build());
        Assertions.assertNull(result);
    }

    @Test
    public void checkNullLegalAdviserListAndPopulatedLegalAdviserUserReturnsLegalAdviserUser(){

        StaffUser Geoff = new StaffUser("Geoff");

        String result = legalAdviserChecker.returnLegalAdviserNameForManageOrders(
            CaseData.builder().manageOrders(
            ManageOrders.builder().legalAdviserToReviewOrder(Geoff).build()
            ).build());
        Assertions.assertEquals("StaffUser(idamId=Geoff)", result);
    }

    @Test
    public void checkPopulatedLegalAdviserListAndNullLegalAdviserUserReturnsLegalAdviserList(){

        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();

        String result = legalAdviserChecker.returnLegalAdviserNameForManageOrders(
            CaseData.builder().manageOrders(
                ManageOrders.builder()
                    .nameOfLaToReviewOrder(dynamicList).build()
            ).build());
        Assertions.assertEquals("DynamicList(value=DynamicListElement(code=12345:, label=test), listItems=null)",
                                result);
    }

    @Test
    public void checkPopulatedLegalAdviserListAndPopulatedLegalAdviserUserReturnsLegalAdviserUser(){

        StaffUser Geoff = new StaffUser("Geoff");
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();

        String result = legalAdviserChecker.returnLegalAdviserNameForManageOrders(
            CaseData.builder().manageOrders(
                ManageOrders.builder()
                    .legalAdviserToReviewOrder(Geoff)
                    .nameOfLaToReviewOrder(dynamicList).build()
            ).build());
        Assertions.assertEquals("StaffUser(idamId=Geoff)", result);
    }

}
