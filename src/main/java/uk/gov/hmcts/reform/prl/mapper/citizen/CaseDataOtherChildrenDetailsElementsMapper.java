package uk.gov.hmcts.reform.prl.mapper.citizen;

import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildOtherChildrenDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ChildDetail;
import uk.gov.hmcts.reform.prl.models.c100rebuild.DateofBirth;
import uk.gov.hmcts.reform.prl.models.c100rebuild.PersonalDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherChildrenNotInTheCase;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

public class CaseDataOtherChildrenDetailsElementsMapper {

    private CaseDataOtherChildrenDetailsElementsMapper() {
    }

    public static void updateOtherChildDetailsElementsForCaseData(CaseData.CaseDataBuilder<?,?> caseDataBuilder,
                                                                  C100RebuildOtherChildrenDetailsElements c100RebuildOtherChildrenDetailsElements) {

        if (PrlAppsConstants.YES.equals(c100RebuildOtherChildrenDetailsElements.getHasOtherChildren())) {
            List<Element<OtherChildrenNotInTheCase>> otherChildrenNotInTheCase =
                buildOtherChildrenNotInTheCase(c100RebuildOtherChildrenDetailsElements.getOtherChildrenDetails());
            caseDataBuilder.childrenNotPartInTheCaseYesNo(YesOrNo.Yes);
            caseDataBuilder.childrenNotInTheCase(otherChildrenNotInTheCase);
        }
    }

    private static List<Element<OtherChildrenNotInTheCase>> buildOtherChildrenNotInTheCase(List<ChildDetail> childDetails) {
        return childDetails.stream()
            .map(CaseDataOtherChildrenDetailsElementsMapper::mapToOtherChildrenNotInTheCase)
            .toList();
    }

    private static Element<OtherChildrenNotInTheCase> mapToOtherChildrenNotInTheCase(ChildDetail childDetail) {
        return Element.<OtherChildrenNotInTheCase>builder()
            .id(UUID.fromString(childDetail.getId()))
            .value(OtherChildrenNotInTheCase.builder()
                   .firstName(childDetail.getFirstName())
                   .lastName(childDetail.getLastName())
                   .dateOfBirth(getDateOfBirth(childDetail))
                   .isDateOfBirthKnown(buildDateOfBirthKnown(childDetail.getPersonalDetails()))
                   .gender(Gender.getDisplayedValueFromEnumString((childDetail.getPersonalDetails().getGender())))
                   .otherGender(childDetail.getPersonalDetails().getOtherGenderDetails())
                   .build()
            ).build();
    }

    private static LocalDate getDateOfBirth(ChildDetail childDetail) {
        LocalDate dateOfBirth = buildDateOfBirth(childDetail.getPersonalDetails().getDateOfBirth());
        return dateOfBirth != null ? dateOfBirth : buildDateOfBirth(childDetail.getPersonalDetails().getApproxDateOfBirth());
    }

    private static YesOrNo buildDateOfBirthKnown(PersonalDetails personalDetails) {
        return Yes.name().equals(personalDetails.getIsDateOfBirthUnknown()) ? No : Yes;
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

