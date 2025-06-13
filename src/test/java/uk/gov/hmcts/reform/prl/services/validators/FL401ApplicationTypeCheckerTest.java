package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.LinkToCA;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class FL401ApplicationTypeCheckerTest {

    @Mock
    private TaskErrorService taskErrorService;

    @InjectMocks
    private FL401ApplicationTypeChecker fl401ApplicationTypeChecker;

    private CaseData caseData;
    private TypeOfApplicationOrders orders;
    private LinkToCA linkToCA;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        caseData = CaseData.builder().build();
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
    void whenFieldsPartiallyCompleteIsFinishedReturnsFalse() {
        assertFalse(fl401ApplicationTypeChecker.isFinished(caseData));
    }

    @Test
    void whenAllRequiredFieldsCompletedThenIsFinishedReturnsTrue() {

        CaseData caseData = CaseData.builder()
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .build();

        assertTrue(fl401ApplicationTypeChecker.isFinished(caseData));
    }

    @Test
    void whenAllRequiredFieldsCompletedIsFinishedReturnsTrue() {
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

        CaseData caseData = CaseData.builder()
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .build();

        assertTrue(fl401ApplicationTypeChecker.isFinished(caseData));
    }

    @Test
    void whenAllRequiredFieldsCompletedIsFinishedReturnsTrueWithLinkToCaIsNo() {
        List<FL401OrderTypeEnum> orderList = new ArrayList<>();

        orderList.add(FL401OrderTypeEnum.nonMolestationOrder);
        orderList.add(FL401OrderTypeEnum.occupationOrder);

        TypeOfApplicationOrders orders = TypeOfApplicationOrders.builder()
            .orderType(orderList)
            .build();

        LinkToCA linkToCA = LinkToCA.builder()
            .linkToCaApplication(YesOrNo.No)
            .build();

        CaseData caseData = CaseData.builder()
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .build();

        assertTrue(fl401ApplicationTypeChecker.isFinished(caseData));
    }

    @Test
    void whenAllRequiredFieldsCompletedIsFinishedReturnsTrueWithLinkToCaIsDontKnow() {
        List<FL401OrderTypeEnum> orderList = new ArrayList<>();

        orderList.add(FL401OrderTypeEnum.nonMolestationOrder);
        orderList.add(FL401OrderTypeEnum.occupationOrder);

        TypeOfApplicationOrders orders = TypeOfApplicationOrders.builder()
            .orderType(orderList)
            .build();

        LinkToCA linkToCA = LinkToCA.builder()
            .build();

        CaseData caseData = CaseData.builder()
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(null)
            .build();
        assertFalse(fl401ApplicationTypeChecker.isFinished(caseData));
    }

    @Test
    void whenNoCaseDataThenIsStartedReturnsFalse() {
        assertFalse(fl401ApplicationTypeChecker.isStarted(caseData));
    }

    @Test
    void whenNoCaseDataThenHasMandatoryReturnsFalse() {
        assertFalse(fl401ApplicationTypeChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    void whenCaseDataPresentThenHasMandatoryReturnsFalse() {

        LinkToCA linkToCA = LinkToCA.builder()
            .linkToCaApplication(YesOrNo.No)
            .build();

        CaseData caseData = CaseData.builder()
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .build();

        assertFalse(fl401ApplicationTypeChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(fl401ApplicationTypeChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
