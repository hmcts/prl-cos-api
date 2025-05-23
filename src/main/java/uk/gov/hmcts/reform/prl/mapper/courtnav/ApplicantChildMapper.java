package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ProtectedChild;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Mapper(componentModel = "spring")
public interface ApplicantChildMapper {

    @Named("toApplicantChild")
    static ApplicantChild toApplicantChild(ProtectedChild child) {
        return ApplicantChild.builder()
            .fullName(child.getFullName())
            .dateOfBirth(LocalDate.parse(child.getDateOfBirth().mergeDate()))
            .applicantChildRelationship(child.getRelationship())
            .applicantRespondentShareParental(child.isParentalResponsibility() ? Yes : No)
            .respondentChildRelationship(child.getRespondentRelationship())
            .build();
    }

    default List<Element<ApplicantChild>> mapProtectedChildren(List<ProtectedChild> protectedChildren) {
        if (protectedChildren == null) {
            return null;
        }

        return protectedChildren.stream()
            .map(child -> element(toApplicantChild(child)))
            .toList();
    }
}
