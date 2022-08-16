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

    private final Integer day;
    private final Integer month;
    private final Integer year;

    public String mergeDate() {
        String day1 = getDay().toString();
        if (day1.length() < 2) {
            day1 = "0" + day1;
        }
        String month1 = getMonth().toString();
        if (month1.length() < 2) {
            month1 = "0" + month1;
        }
        return getYear().toString() + "-" + month1 + "-" + day1;
    }
}
