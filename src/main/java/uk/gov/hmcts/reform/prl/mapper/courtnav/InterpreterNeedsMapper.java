package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.mapstruct.Mapper;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.InterpreterNeed;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;

import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Mapper(componentModel = "spring")
public interface InterpreterNeedsMapper {

    default List<Element<InterpreterNeed>> map(CourtNavFl401 source) {
        boolean needsInterpreter = Boolean.TRUE.equals(
            source.getFl401().getGoingToCourt().getIsInterpreterRequired());

        if (!needsInterpreter) {
            return Collections.emptyList();
        }

        String language = source.getFl401().getGoingToCourt().getInterpreterLanguage();
        String dialect = source.getFl401().getGoingToCourt().getInterpreterDialect();

        String fullLanguage = (dialect != null && !dialect.isEmpty())
            ? language + " - " + dialect
            : language;

        InterpreterNeed need = InterpreterNeed.builder()
            .party(List.of(PartyEnum.applicant))
            .language(fullLanguage)
            .build();

        return List.of(element(need));
    }
}
