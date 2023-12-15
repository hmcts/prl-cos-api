package uk.gov.hmcts.reform.prl.models.roleassignment;

import lombok.Data;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.prl.enums.GrantType;
import uk.gov.hmcts.reform.prl.enums.Roles;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RequestedRoles {

    private String id;
    private String actorIdType;
    private String actorId;
    private String roleType;
    private String roleName;
    private Classification classification;
    private GrantType grantType;
    private List<String> authorisation;
    private Roles roleCategory;
    private Boolean readOnly;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private Attributes attributes;
    private Notes notes;
}
