package uk.gov.hmcts.reform.prl.models.roleassignment;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Notes {

    private String userId;
    private LocalDateTime time;
    private String comment;
}
