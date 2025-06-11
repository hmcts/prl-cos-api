package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.hmcts.reform.prl.enums.FL401Consent;
import uk.gov.hmcts.reform.prl.models.complextypes.StatementOfTruth;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavStatementOfTruth;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ConsentEnum;

import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring", imports = {LocalDate.class})
public interface StatementOfTruthMapper {

    @Mapping(target = "applicantConsent", source = "applicantConsent", qualifiedByName = "mapConsentList")
    @Mapping(target = "date", expression = "java(LocalDate.parse(source.getDate().mergeDate()))")
    StatementOfTruth map(CourtNavStatementOfTruth source);

    @Named("mapConsentList")
    static List<FL401Consent> mapConsentList(List<ConsentEnum> declaration) {
        return declaration == null ? null :
            declaration.stream()
                .map(ConsentEnum::getId)
                .map(FL401Consent::getDisplayedValueFromEnumString)
                .toList();
    }
}
