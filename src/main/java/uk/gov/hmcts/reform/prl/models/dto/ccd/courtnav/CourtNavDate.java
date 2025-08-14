package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourtNavDate {

    private Integer day;
    private Integer month;
    private Integer year;

    public String mergeDate() {
        if (day == null || month == null || year == null) {
            return null;
        }

        String dayStr = String.format("%02d", day);
        String monthStr = String.format("%02d", month);
        return String.format("%d-%s-%s", year, monthStr, dayStr);
    }
}
