package uk.gov.hmcts.reform.prl.models.sendandreply;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;


@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor
public class SendAndReplyDynamicDoc {

    @CCD(label = "Document", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList submittedDocsRefList;
}
