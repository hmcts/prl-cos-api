package uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "The request object for Query RoleAssignment")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleAssignmentQueryRequest {

    private QueryAttributes attributes;

    private List<String> roleCategory;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime validAt;
}
