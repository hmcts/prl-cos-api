package uk.gov.hmcts.reform.prl.models.dto.legalofficer;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StaffProfile {

    @JsonProperty("first_name")
    private String first_name;

    @JsonProperty("last_name")
    private String last_name;

    @JsonProperty("user_type")
    private String user_type;

    @JsonProperty("email_id")
    private String email_id;
}
