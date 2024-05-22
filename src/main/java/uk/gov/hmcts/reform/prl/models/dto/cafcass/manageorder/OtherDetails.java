package uk.gov.hmcts.reform.prl.models.dto.cafcass.manageorder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Slf4j
public class OtherDetails {

    public String createdBy;
    public String orderRecipients;

    public String orderCreatedDate;

    public void setOrderCreatedDate(String orderCreatedDate) {
        if (orderCreatedDate != null) {
            LocalDate dateTime = null;
            try {
                dateTime = LocalDate.parse(orderCreatedDate, DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK));
            } catch (DateTimeParseException dateTimeParseException) {
                try {
                    dateTime = LocalDate.parse(orderCreatedDate, DateTimeFormatter.ofPattern("d MMM yyyy", Locale.UK));
                } catch (DateTimeParseException e) {
                    try {
                        dateTime = LocalDate.parse(
                            orderCreatedDate,
                            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
                        );
                    } catch (DateTimeParseException exception) {
                        log.info("orderCreatedDate received {}", orderCreatedDate);
                    }
                }
            }
            this.orderCreatedDate = dateTime.toString();
            if (this.orderMadeDate == null) {
                this.orderMadeDate = this.orderCreatedDate;
            }
        }
    }

    public String orderMadeDate;

    public void setOrderMadeDate(String orderMadeDate) {
        if (orderMadeDate != null) {
            LocalDate dateTime = null;
            try {
                dateTime = LocalDate.parse(orderMadeDate, DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK));
            } catch (DateTimeParseException e) {
                try {
                    dateTime = LocalDate.parse(orderMadeDate, DateTimeFormatter.ofPattern("d MMM yyyy", Locale.UK));
                } catch (DateTimeParseException ex) {
                    try {
                        dateTime = LocalDate.parse(
                            orderMadeDate,
                            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
                        );
                    } catch (DateTimeParseException exception) {
                        log.info("orderMadeDate received {}", orderMadeDate);
                    }
                }
            }
            this.orderMadeDate = dateTime.toString();
        }
    }
}
