package uk.gov.hmcts.reform.prl.mapper.staffresponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.mapper.dynamiclistelement.LegalAdviserDynamicListElementConverter;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffResponse;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGALOFFICE;

/**
 * Filter to identify Legal Advisers from StaffResponse and convert them to DynamicListElement.
 */
@Component
@RequiredArgsConstructor
public class LegalAdviserDynamicListElementFilter implements StaffResponseToDynamicListElementFilter {

    private final LegalAdviserDynamicListElementConverter converter;

    @Override
    public Optional<DynamicListElement> filter(StaffResponse source) {
        if (LEGALOFFICE.equalsIgnoreCase(source.getStaffProfile().getUserType())) {
            return Optional.ofNullable(converter.convert(source.getStaffProfile()));
        }
        return Optional.empty();
    }
}
