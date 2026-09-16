package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.staff.StaffUser;

public class LegalAdviserCheckerTest {

    private final LegalAdviserChecker legalAdviserChecker = new LegalAdviserChecker();
    private StaffUser geoff = new StaffUser("Geoff");


    @Test
    public void checkNullLegalAdviserListAndNullLegalAdviserUserReturnsNull() {

        String result = legalAdviserChecker.
            returnLegalAdviserName(null, null);
        Assertions.assertNull(result);
    }

    @Test
    public void checkNullLegalAdviserListAndPopulatedLegalAdviserUserReturnsLegalAdviserUser() {

        String result = legalAdviserChecker.returnLegalAdviserName(
            null, geoff
        );
        Assertions.assertEquals("StaffUser(idamId=Geoff)", result);
    }

    @Test
    public void checkPopulatedLegalAdviserListAndNullLegalAdviserUserReturnsLegalAdviserList() {

        DynamicList dynamicList = DynamicList.builder()
            .value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();

        String result = legalAdviserChecker.returnLegalAdviserName(
            dynamicList, null
        );
        Assertions.assertEquals("DynamicList(value=DynamicListElement(code=12345:, label=test), listItems=null)",
                                result);
    }

    @Test
    public void checkPopulatedLegalAdviserListAndPopulatedLegalAdviserUserReturnsLegalAdviserUser() {

        DynamicList dynamicList = DynamicList.builder().value(
            DynamicListElement.builder().code("12345:").label("test").build()).build();

        String result = legalAdviserChecker.returnLegalAdviserName(
            dynamicList, geoff
        );
        Assertions.assertEquals("StaffUser(idamId=Geoff)", result);
    }

    @Test
    public void isLegalAdviserListPresentReturnsFalseForNullList() {
        boolean result = legalAdviserChecker.isLegalAdviserListPresent(null);
        Assertions.assertFalse(result);
    }

    @Test
    public void isLegalAdviserListPresentReturnsFalseForBlankValueLabel() {
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("").build()).build();
        boolean result = legalAdviserChecker.isLegalAdviserListPresent(dynamicList);
        Assertions.assertFalse(result);
    }

    @Test
    public void isLegalAdviserListPresentReturnsTrueForPopulatedValueLabel() {
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test").build()).build();
        boolean result = legalAdviserChecker.isLegalAdviserListPresent(dynamicList);
        Assertions.assertTrue(result);
    }

}
