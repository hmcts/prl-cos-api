package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401OtherProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401Proceedings;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class FL401OtherProceedingsCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    FL401OtherProceedingsChecker otherProceedingsChecker;

    @Test
    void startedWithPreviousOrOngoingProceedings() {
        CaseData caseData = CaseData.builder()
            .fl401OtherProceedingDetails(FL401OtherProceedingDetails.builder()
                                             .hasPrevOrOngoingOtherProceeding(YesNoDontKnow.yes).build()).build();
        boolean isStarted = otherProceedingsChecker.isStarted(caseData);
        assertTrue(isStarted);
    }

    @Test
    void notStartedWithoutPreviousOrOngoingProceedings() {
        CaseData caseData = CaseData.builder()
            .fl401OtherProceedingDetails(FL401OtherProceedingDetails.builder()
                                             .hasPrevOrOngoingOtherProceeding(YesNoDontKnow.no).build()).build();
        boolean isStarted = otherProceedingsChecker.isStarted(caseData);
        assertFalse(isStarted);
    }

    @Test
    void finishedIfNoPreviousOrOngoingProceedings() {
        CaseData caseData = CaseData.builder()
            .fl401OtherProceedingDetails(FL401OtherProceedingDetails.builder()
                                             .hasPrevOrOngoingOtherProceeding(YesNoDontKnow.no).build()).build();
        boolean isFinished = otherProceedingsChecker.isFinished(caseData);
        assertTrue(isFinished);
    }

    @Test
    void notFinishedWithPreviousOrOngoingProceedings() {
        CaseData caseData = CaseData.builder()
            .fl401OtherProceedingDetails(FL401OtherProceedingDetails.builder()
                                             .hasPrevOrOngoingOtherProceeding(YesNoDontKnow.yes).build()).build();
        boolean isFinished = otherProceedingsChecker.isFinished(caseData);
        assertFalse(isFinished);
    }

    @Test
    void notFinishedWithWithoutProceedings() {
        CaseData caseData = CaseData.builder().build();
        boolean isFinished = otherProceedingsChecker.isFinished(caseData);
        assertFalse(isFinished);
    }

    @Test
    void finishedWithPreviousOrOngoingProceedingList() {

        FL401Proceedings proceedingDetails = FL401Proceedings.builder().build();
        Element<FL401Proceedings> wrappedProceedings = Element.<FL401Proceedings>builder()
            .value(proceedingDetails).build();
        List<Element<FL401Proceedings>> listOfProceedings = Collections.singletonList(wrappedProceedings);


        CaseData caseData = CaseData.builder()
            .fl401OtherProceedingDetails(FL401OtherProceedingDetails.builder()
                                             .hasPrevOrOngoingOtherProceeding(YesNoDontKnow.yes)
                                             .fl401OtherProceedings(listOfProceedings)
                                             .build())
            .build();
        boolean isFinished = otherProceedingsChecker.isFinished(caseData);
        assertFalse(isFinished);
    }

    @Test
    void finishedWithOngoingProceedingList() {

        FL401Proceedings proceedingDetails = FL401Proceedings.builder()
            .anyOtherDetails("test")
            .nameOfCourt("court")
            .typeOfCase("test")
            .build();
        Element<FL401Proceedings> wrappedProceedings = Element.<FL401Proceedings>builder()
            .value(proceedingDetails).build();
        List<Element<FL401Proceedings>> listOfProceedings = Collections.singletonList(wrappedProceedings);


        CaseData caseData = CaseData.builder()
            .fl401OtherProceedingDetails(FL401OtherProceedingDetails.builder()
                                             .hasPrevOrOngoingOtherProceeding(YesNoDontKnow.yes)
                                             .fl401OtherProceedings(listOfProceedings)
                                             .build())
            .build();
        boolean isFinished = otherProceedingsChecker.isFinished(caseData);
        assertTrue(isFinished);
    }

    @Test
    void notFinishedIfTypeOfCaseEmpty() {

        FL401Proceedings proceedingDetails = FL401Proceedings.builder()
            .anyOtherDetails("test")
            .nameOfCourt("court")
            .typeOfCase("")
            .build();
        Element<FL401Proceedings> wrappedProceedings = Element.<FL401Proceedings>builder()
            .value(proceedingDetails).build();
        List<Element<FL401Proceedings>> listOfProceedings = Collections.singletonList(wrappedProceedings);


        CaseData caseData = CaseData.builder()
            .fl401OtherProceedingDetails(FL401OtherProceedingDetails.builder()
                                             .hasPrevOrOngoingOtherProceeding(YesNoDontKnow.yes)
                                             .fl401OtherProceedings(listOfProceedings)
                                             .build())
            .build();
        boolean isFinished = otherProceedingsChecker.isFinished(caseData);
        assertFalse(isFinished);
    }

    @Test
    void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(otherProceedingsChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
