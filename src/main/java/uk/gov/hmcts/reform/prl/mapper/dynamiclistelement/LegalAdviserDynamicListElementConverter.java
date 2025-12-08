package uk.gov.hmcts.reform.prl.mapper.dynamiclistelement;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffProfile;

/**
 * Converter class to transform StaffProfile objects into DynamicListElement objects.
 *
 * <p>The DynamicListElement's code is set to the staff member's Idam ID.
 *
 * <p>The DynamicListElement's label is formatted to include the staff member's first name,
 * last name, and email address in brackets.
 */
@Component
public class LegalAdviserDynamicListElementConverter implements DynamicListElementConverter<StaffProfile> {
    @Override
    public DynamicListElement convert(StaffProfile source) {
        String labelFormat = "%s %s (%s)";

        return DynamicListElement.builder()
            .code(source.getId())
            .label(String.format(
                labelFormat, source.getFirstName(), source.getLastName(),
                source.getEmailId()
            ))
            .build();
    }
}
