package uk.gov.hmcts.reform.prl.services.validators;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401Proceedings;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.FL401_OTHER_PROCEEDINGS_ERROR;
import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.dontKnow;
import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.no;
import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.yes;

@Slf4j
@Service
public class FL401OtherProceedingsChecker implements EventChecker {

    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        if (null == caseData.getFl401OtherProceedingDetails()) {
            return false;
        }

        Optional<YesNoDontKnow> otherProceedings = ofNullable(
            caseData.getFl401OtherProceedingDetails().getHasPrevOrOngoingOtherProceeding());

        if (otherProceedings.isPresent()
            && (otherProceedings.get().equals(no) || otherProceedings.get().equals(dontKnow))) {
            taskErrorService.removeError(FL401_OTHER_PROCEEDINGS_ERROR);
            return  true;
        }

        Optional<List<Element<FL401Proceedings>>> proceedingDetails = ofNullable(
            caseData.getFl401OtherProceedingDetails().getFl401OtherProceedings());

        if (proceedingDetails.isPresent()) {
            List<FL401Proceedings> allProceedings = proceedingDetails.get()
                                                .stream()
                                                .map(Element::getValue)
                                                .collect(Collectors.toList());

            //if a collection item is added and then removed the collection exists as length 0
            if (allProceedings.size() == 0) {
                return false;
            }

            boolean allMandatoryFieldsDone = true;

            for (FL401Proceedings proceeding : allProceedings) {
                boolean anyOfTheFieldsEmpty = anyEmpty(proceeding.getTypeOfCase(), proceeding.getAnyOtherDetails());
                if (anyOfTheFieldsEmpty) {
                    allMandatoryFieldsDone = false;
                    break;
                }
            }
            if (allMandatoryFieldsDone) {
                taskErrorService.removeError(FL401_OTHER_PROCEEDINGS_ERROR);
                return true;
            }
        }
        return false;

    }

    @Override
    public boolean isStarted(CaseData caseData) {

        log.info(ofNullable(caseData.getFl401OtherProceedingDetails()).toString());

        if (ofNullable(caseData.getFl401OtherProceedingDetails().getHasPrevOrOngoingOtherProceeding()).isPresent()) {
            if (ofNullable(caseData.getFl401OtherProceedingDetails().getHasPrevOrOngoingOtherProceeding()).get().equals(yes)) {
                taskErrorService.addEventError(FL401_OTHER_PROCEEDINGS, FL401_OTHER_PROCEEDINGS_ERROR,
                                               FL401_OTHER_PROCEEDINGS_ERROR.getError()
                );
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    public static boolean anyEmpty(Object... properties) {
        return Stream.of(properties).anyMatch(ObjectUtils::isEmpty);
    }

}
