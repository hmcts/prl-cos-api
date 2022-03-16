package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.LinkToCA;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FL401ApplicationTypeCheckerTest {

    @Mock
    private TaskErrorService taskErrorService;

    @Mock
    FL401ApplicationTypeChecker fl401ApplicationTypeChecker;

    private CaseData caseData;
    private TypeOfApplicationOrders orders;
    private LinkToCA linkToCA;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        List<FL401OrderTypeEnum> orderList = new ArrayList<>();
        orderList.add(FL401OrderTypeEnum.nonMolestationOrder);

        orders = TypeOfApplicationOrders.builder()
            .orderType(orderList)
            .build();

        linkToCA = LinkToCA.builder()
            .linkToCaApplication(YesOrNo.Yes)
            .caApplicationNumber("123")
            .build();

    }

    @Test
    public void whenFieldsPartiallyCompleteIsFinishedReturnsFalse() {
        assertFalse(fl401ApplicationTypeChecker.isFinished(caseData));
    }

    @Test
    public void whenAllRequiredFieldsCompletedThenIsFinishedReturnsTrue() {

        caseData = CaseData.builder()
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .build();

        when(fl401ApplicationTypeChecker.isFinished(caseData)).thenReturn(true);
        assertTrue(fl401ApplicationTypeChecker.isFinished(caseData));
    }

    @Test
    public void whenAllRequiredFieldsCompletedIsFinishedReturnsTrue() {
        List<FL401OrderTypeEnum> orderList = new ArrayList<>();

        orderList.add(FL401OrderTypeEnum.nonMolestationOrder);
        orderList.add(FL401OrderTypeEnum.occupationOrder);

        TypeOfApplicationOrders orders = TypeOfApplicationOrders.builder()
            .orderType(orderList)
            .build();

        LinkToCA linkToCA = LinkToCA.builder()
            .linkToCaApplication(YesOrNo.Yes)
            .caApplicationNumber("123")
            .build();

        caseData = CaseData.builder()
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .build();

        when(fl401ApplicationTypeChecker.isFinished(caseData)).thenReturn(true);

        assertTrue(fl401ApplicationTypeChecker.isFinished(caseData));
    }

    @Test
    public void whenNoCaseDataThenIsStartedReturnsFalse() {
        assertFalse(fl401ApplicationTypeChecker.isStarted(caseData));
    }

    @Test
    public void whenNoCaseDataThenHasMandatoryReturnsFalse() {
        assertFalse(fl401ApplicationTypeChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenCaseDataPresentThenHasMandatoryReturnsFalse() {

        LinkToCA linkToCA = LinkToCA.builder()
            .linkToCaApplication(YesOrNo.No)
            .build();

        caseData = CaseData.builder()
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .build();

        assertFalse(fl401ApplicationTypeChecker.hasMandatoryCompleted(caseData));
    }
}
