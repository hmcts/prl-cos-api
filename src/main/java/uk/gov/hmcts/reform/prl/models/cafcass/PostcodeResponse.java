package uk.gov.hmcts.reform.prl.models.cafcass;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PostcodeResponse {
    private List<PostcodeResult> results;
}
