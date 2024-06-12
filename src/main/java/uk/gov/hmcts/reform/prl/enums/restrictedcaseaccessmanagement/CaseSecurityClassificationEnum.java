package uk.gov.hmcts.reform.prl.enums.restrictedcaseaccessmanagement;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum CaseSecurityClassificationEnum {
   PUBLIC, PRIVATE, RESTRICTED
}
