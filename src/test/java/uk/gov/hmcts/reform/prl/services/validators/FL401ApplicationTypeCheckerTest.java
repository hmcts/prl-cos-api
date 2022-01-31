package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.LinkToCA;
import uk.gov.hmcts.reform.prl.models.complextypes.Orders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

public class FL401ApplicationTypeCheckerTest {

    @Mock
    private TaskErrorService taskErrorService;

    @Mock
    private FL401ApplicationTypeChecker fl401ApplicationTypeChecker;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void whenFieldsPartiallyCompleteIsFinishedReturnsFalse() {

        CaseData caseData = CaseData.builder()
            .build();

        assertFalse(fl401ApplicationTypeChecker.isFinished(caseData));

    }

    @Test
    public void whenAllRequiredFieldsCompletedThenIsFinishedReturnsTrue() {
        List<FL401OrderTypeEnum> orderList = new ArrayList<>();

        orderList.add(FL401OrderTypeEnum.nonMolestationOrder);

        Orders orders = Orders.builder()
            .orderType(orderList)
            .build();

        LinkToCA linkToCA = LinkToCA.builder()
            .linkToCaApplication(YesOrNo.Yes)
            .childArrangementsApplicationNumber("123")
            .build();

        CaseData caseData = CaseData.builder()
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .build();

        when(fl401ApplicationTypeChecker.isFinished(caseData)).thenReturn(true);

    }

    @Test
    public void whenAllRequiredFieldsCompletedIsFinishedReturnsTrue() {
        List<FL401OrderTypeEnum> orderList = new ArrayList<>();

        orderList.add(FL401OrderTypeEnum.nonMolestationOrder);
        orderList.add(FL401OrderTypeEnum.occupationOrder);

        Orders orders = Orders.builder()
            .orderType(orderList)
            .build();

        LinkToCA linkToCA = LinkToCA.builder()
            .linkToCaApplication(YesOrNo.Yes)
            .childArrangementsApplicationNumber("123")
            .build();

        CaseData caseData = CaseData.builder()
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .build();

        when(fl401ApplicationTypeChecker.isFinished(caseData)).thenReturn(true);

    }

    @Test
    public void whenNoCaseDataThenIsStartedReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        assertFalse(fl401ApplicationTypeChecker.isStarted(caseData));

    }

    @Test
    public void whenNoCaseDataThenHasMandatoryReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        assertFalse(fl401ApplicationTypeChecker.hasMandatoryCompleted(caseData));

    }

    @Test
    public void whenCaseDataPresentThenHasMandatoryReturnsFalse() {

        List<FL401OrderTypeEnum> orderList = new ArrayList<>();

        orderList.add(FL401OrderTypeEnum.nonMolestationOrder);

        Orders orders = Orders.builder()
            .orderType(orderList)
            .build();

        CaseData caseData = CaseData.builder()
            .typeOfApplicationOrders(orders)
            .build();

        assertFalse(fl401ApplicationTypeChecker.hasMandatoryCompleted(caseData));

    }
}
