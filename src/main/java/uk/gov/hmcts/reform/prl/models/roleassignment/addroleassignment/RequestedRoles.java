package uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.Attributes;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "requestedRoles")
public class RequestedRoles {

    private String id;
    private String actorIdType;
    private String actorId;
    private String roleType;
    private String roleName;
    private String classification;
    private String grantType;
    private String roleCategory;
    private Boolean readOnly;
    @JsonFormat(timezone = "UTC", shape = JsonFormat.Shape.STRING)
    private Instant beginTime;
    @JsonFormat(timezone = "UTC", shape = JsonFormat.Shape.STRING)
    private Instant endTime;
    @JsonFormat(timezone = "UTC", shape = JsonFormat.Shape.STRING)
    private Instant created;
    private List<String> authorisations;
    private Attributes attributes;
}
