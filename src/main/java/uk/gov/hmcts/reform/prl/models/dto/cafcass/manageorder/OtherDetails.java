package uk.gov.hmcts.reform.prl.models.dto.cafcass.manageorder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class OtherDetails {

    public String createdBy;
    public String orderRecipients;

    public String orderCreatedDate;

    public void setOrderCreatedDate(String orderCreatedDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
        LocalDate dateTime = LocalDate.parse(orderCreatedDate, formatter);
        this.orderCreatedDate = dateTime.toString();
    }

    public String orderMadeDate;

    public void setOrderMadeDate(String orderMadeDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
        LocalDate dateTime = LocalDate.parse(orderMadeDate, formatter);
        this.orderMadeDate = dateTime.toString();
    }
}
