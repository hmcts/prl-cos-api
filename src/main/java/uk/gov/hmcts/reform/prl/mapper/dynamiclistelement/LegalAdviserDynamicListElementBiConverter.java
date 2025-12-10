package uk.gov.hmcts.reform.prl.mapper.dynamiclistelement;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffProfile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converter class for converting between StaffProfile and LegalAdviserIdamId
 * using DynamicListElement as an intermediary representation.
 *
 * <p>The DynamicListElement's label is formatted as "FirstName LastName (Email)".
 *
 * <p>The DynamicListElement's code is the StaffProfile's Idam ID.
 */
@Component
public class LegalAdviserDynamicListElementBiConverter implements DynamicListElementBiConverter<StaffProfile, LegalAdviserIdamId> {
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

    @Override
    public LegalAdviserIdamId convertFromDynamicListElement(DynamicListElement element) {
        String label = element.getLabel();

        Pattern pattern = Pattern.compile("^(.*) \\(([^)]+)\\)$");
        Matcher matcher = pattern.matcher(label);
        String fullName;
        String email;
        if (matcher.matches()) {
            fullName = matcher.group(1).trim();
            email = matcher.group(2).trim();
        } else {
            fullName = label;
            email = label;
        }

        return LegalAdviserIdamId.builder()
            .idamId(element.getCode())
            .email(email)
            .fullName(fullName)
            .build();
    }
}
