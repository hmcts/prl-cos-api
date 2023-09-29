package uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2Consent;

@Slf4j
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class C2ApplicationDetails {

    private final C2Consent consent;
    private final String reasonForNotInformingRespondent;

}
