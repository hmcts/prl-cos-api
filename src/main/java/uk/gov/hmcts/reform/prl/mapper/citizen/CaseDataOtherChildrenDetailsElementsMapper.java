package uk.gov.hmcts.reform.prl.mapper.citizen;

import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildOtherChildrenDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ChildDetail;
import uk.gov.hmcts.reform.prl.models.c100rebuild.DateofBirth;
import uk.gov.hmcts.reform.prl.models.c100rebuild.PersonalDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

public class CaseDataOtherChildrenDetailsElementsMapper {

    private CaseDataOtherChildrenDetailsElementsMapper() {
    }

    public static void updateOtherChildDetailsElementsForCaseData(CaseData.CaseDataBuilder<?,?> caseDataBuilder,
                                                                  C100RebuildOtherChildrenDetailsElements c100RebuildOtherChildrenDetailsElements) {

        if (PrlAppsConstants.YES.equals(c100RebuildOtherChildrenDetailsElements.getHasOtherChildren())) {
            caseDataBuilder
                .otherChildren(buildChildDetails(c100RebuildOtherChildrenDetailsElements.getOtherChildrenDetails()));
        }
    }

    private static List<Element<Child>> buildChildDetails(List<ChildDetail> childDetails) {
        return childDetails.stream()
            .map(CaseDataOtherChildrenDetailsElementsMapper::mapToChildDetails)
            .toList();
    }

    private static Element<Child> mapToChildDetails(ChildDetail childDetail) {

        return Element.<Child>builder().value(Child.builder()
                   .firstName(childDetail.getFirstName())
                   .lastName(childDetail.getLastName())
                   .dateOfBirth(getDateOfBirth(childDetail))
                   .isDateOfBirthUnknown(buildDateOfBirthUnknown(childDetail.getPersonalDetails()))
                   .gender(Gender.getDisplayedValueFromEnumString((childDetail.getPersonalDetails().getGender())))
                   .otherGender(childDetail.getPersonalDetails().getOtherGenderDetails())
                   .build()
            ).build();
    }

    private static LocalDate getDateOfBirth(ChildDetail childDetail) {
        LocalDate dateOfBirth = buildDateOfBirth(childDetail.getPersonalDetails().getDateOfBirth());
        return dateOfBirth != null ? dateOfBirth : buildDateOfBirth(childDetail.getPersonalDetails().getApproxDateOfBirth());
    }

    private static DontKnow buildDateOfBirthUnknown(PersonalDetails personalDetails) {
        return Yes.name().equals(personalDetails.getIsDateOfBirthUnknown()) ? DontKnow.dontKnow : null;
    }

    private static LocalDate buildDateOfBirth(DateofBirth dateOfBirth) {
        if (isNotEmpty(dateOfBirth.getYear()) && isNotEmpty(dateOfBirth.getMonth()) && isNotEmpty(dateOfBirth.getDay())) {
            return LocalDate.of(Integer.parseInt(dateOfBirth.getYear()), Integer.parseInt(dateOfBirth.getMonth()),
                                Integer.parseInt(dateOfBirth.getDay())
            );
        }
        return null;
    }

}

