package uk.gov.hmcts.reform.prl.models.dto.cafcass.manageorder;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.prl.enums.serveorder.CafcassCymruDocumentsEnum;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.Element;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.HearingDetails;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.D_MMMM_UUUU;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HYPHEN_SEPARATOR;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@JsonPropertyOrder(alphabetic = true)
public class CaseOrder {

    public String orderType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public String dateCreated;

    public OtherDetails otherDetails;

    public String orderTypeId;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Element<HearingDetails>> manageOrderHearingDetails;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private CaseHearing hearingDetails;


    public void setManageOrderHearingDetails(List<Element<HearingDetails>> manageOrderHearingDetails) {
        this.manageOrderHearingDetails = manageOrderHearingDetails;
        CaseHearing caseHearing = null;
        if (this.manageOrderHearingDetails != null && !this.manageOrderHearingDetails.isEmpty()) {
            caseHearing = CaseHearing.caseHearingWith()
                .hearingType(manageOrderHearingDetails.get(0).getValue()
                                 .getHearingTypes().getValue() != null
                                 ? manageOrderHearingDetails.get(0).getValue().getHearingTypes().getValue().getCode() : null)
                .hearingTypeValue(manageOrderHearingDetails.get(0).getValue()
                                      .getHearingTypes().getValue() != null
                                      ? manageOrderHearingDetails.get(0).getValue().getHearingTypes().getValue().getLabel() : null)
                .build();
            setHearingDetails(caseHearing);
        }

    }

    public void setHearingDetails(CaseHearing caseHearing) {
        this.hearingDetails = caseHearing;
    }

    private  ServeOrderDetails serveOrderDetails;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private  String selectedHearingType;

    private String hearingId;

    public void setSelectedHearingType(String selectedHearingType) {
        this.selectedHearingType = selectedHearingType;
        if (selectedHearingType != null) {
            setHearingId(selectedHearingType.split(HYPHEN_SEPARATOR)[0]);
        }
    }

    public void setHearingId(String hearingId) {
        this.hearingId = hearingId;
    }



    private List<String> courtReportType;

    @Setter(AccessLevel.NONE)
    @JsonProperty("orderDocument")
    private OrderDocument orderDocument;

    public void setOrderDocument(OrderDocument orderDocument) throws MalformedURLException {
        if (orderDocument != null
            && StringUtils.hasText(orderDocument.getDocumentUrl())) {
            URL url = new URL(orderDocument.getDocumentUrl());
            orderDocument.setDocumentId(getDocumentId(url));
            orderDocument.setDocumentUrl(null);
        }
        this.orderDocument = orderDocument;
    }

    private String getDocumentId(URL url) {
        String path = url.getPath();
        String documentId = path.split("/")[path.split("/").length - 1];
        return documentId;
    }

    public void setServeOrderDetails(ServeOrderDetails serveOrderDetails) {
        this.serveOrderDetails = serveOrderDetails;
        if (this.serveOrderDetails != null) {
            setOriginalFilingDate(serveOrderDetails.getWhenReportsMustBeFiled());
            setCourtReportType(serveOrderDetails.getCafcassCymruDocuments());
        }
    }

    @JsonProperty("originalFilingDate")
    private LocalDate originalFilingDate;

    public void setOriginalFilingDate(String originalFilingDate) {
        this.originalFilingDate = CommonUtils.formattedLocalDate(originalFilingDate, D_MMMM_UUUU);
    }

    public void setCourtReportType(List<CafcassCymruDocumentsEnum> courtReportType) {

        if (courtReportType != null && !courtReportType.isEmpty()) {
            this.courtReportType = courtReportType.stream().map(cafcassCymruDocumentsEnum -> cafcassCymruDocumentsEnum.getDisplayedValue()).collect(
                Collectors.toList());
        }
    }
}
