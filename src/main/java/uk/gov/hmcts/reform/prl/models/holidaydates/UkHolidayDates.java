package uk.gov.hmcts.reform.prl.models.holidaydates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@Data
@Builder(toBuilder = true)
public class UkHolidayDates {
    @JsonProperty("england-and-wales")
    private CountryHolidayDates englandAndWales;

    private UkHolidayDates() {
    }

    public UkHolidayDates(CountryHolidayDates englandAndWales) {
        this.englandAndWales = englandAndWales;
    }

    public CountryHolidayDates getEnglandAndWales() {
        return englandAndWales;
    }

}
