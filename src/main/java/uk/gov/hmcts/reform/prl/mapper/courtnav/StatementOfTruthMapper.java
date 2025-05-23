package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.mapstruct.Mapper;
import uk.gov.hmcts.reform.prl.enums.FL401Consent;
import uk.gov.hmcts.reform.prl.models.complextypes.StatementOfTruth;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ConsentEnum;

import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring")
public interface StatementOfTruthMapper {

    default StatementOfTruth map(CourtNavFl401 source) {
        var statement = source.getFl401().getStatementOfTruth();

        return StatementOfTruth.builder()
            .applicantConsent(mapConsent(statement.getDeclaration()))
            .signature(statement.getSignature())
            .fullname(statement.getSignatureFullName())
            .date(LocalDate.parse(statement.getSignatureDate().mergeDate()))
            .nameOfFirm(statement.getRepresentativeFirmName())
            .signOnBehalf(statement.getRepresentativePositionHeld())
            .build();
    }

    private List<FL401Consent> mapConsent(List<ConsentEnum> declaration) {
        return declaration.stream()
            .map(ConsentEnum::getId)
            .map(FL401Consent::getDisplayedValueFromEnumString)
            .toList();
    }
}
