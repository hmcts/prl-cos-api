package uk.gov.hmcts.reform.prl.models.cafcass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class PostcodeResponse implements java.io.Serializable {
    private List<PostcodeResult> results;
}
