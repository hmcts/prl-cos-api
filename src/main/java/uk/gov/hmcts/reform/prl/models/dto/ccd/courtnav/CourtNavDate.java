package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@Jacksonized
public class CourtNavDate {

    private final String day;
    private final String month;
    private final String year;

    public String mergeDate() {
        return getYear() + "-" + getMonth() + "-" + getDay();
    }
}
