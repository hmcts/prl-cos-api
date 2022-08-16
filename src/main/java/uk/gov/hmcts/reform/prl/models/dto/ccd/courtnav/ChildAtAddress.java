package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChildAtAddress {

    private final String fullName;
    private final Integer age;

    @JsonCreator
    public ChildAtAddress(String fullName, Integer age) {
        this.fullName = fullName;
        this.age = age;
    }
}
