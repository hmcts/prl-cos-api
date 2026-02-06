package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AwaitingInformation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is added only as a java service example. It can be deleted when more services is added.
 */
@Component
@RequiredArgsConstructor
public class AwaitingInformationService {

    public static final String CASE_NAME = "Case Name: ";


    public List<String> validateAwaitingInformation(AwaitingInformation awaitingInformation) {
        List<String> errorList = new ArrayList<>();
        if (awaitingInformation.getReviewDate() != null && !awaitingInformation.getReviewDate().isAfter(LocalDate.now())) {
            errorList.add("The date must be in the future");
        }
        return errorList;
    }
}
