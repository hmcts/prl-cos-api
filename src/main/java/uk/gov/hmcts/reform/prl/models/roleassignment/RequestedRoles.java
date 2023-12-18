package uk.gov.hmcts.reform.prl.models.roleassignment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.prl.enums.GrantType;
import uk.gov.hmcts.reform.prl.enums.RoleCategory;
import uk.gov.hmcts.reform.prl.enums.RoleType;
import uk.gov.hmcts.reform.prl.enums.Status;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "requestedRoles")
public class RequestedRoles {

    private String actorIdType;
    private String actorId;
    private RoleType roleType;
    private String roleName;
    private Classification classification;
    private GrantType grantType;
    private List<String> authorisation;
    private RoleCategory roleCategory;
    private Boolean readOnly;
    private LocalDateTime created;
    private String id;
    private String log;
    private String process;
    private String reference;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private Attributes attributes;
    private List<Notes> notes;
    private Status status;
    private Integer statusSequence;
}
