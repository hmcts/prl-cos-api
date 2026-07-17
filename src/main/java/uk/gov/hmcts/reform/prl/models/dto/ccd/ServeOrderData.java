package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.serveorder.CafcassCymruDocumentsEnum;
import uk.gov.hmcts.reform.prl.enums.serveorder.LocalAuthorityDocumentsEnum;
import uk.gov.hmcts.reform.prl.enums.serveorder.WhatToDoWithOrderEnum;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruCaseworkerPrivatelawJudgeRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaCaseworkerPrivatelawSolicitorRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServeOrderData {
    @CCD(
            label = "Does Cafcass or Cafcass Cymru need to provide a report?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
    )
    @JsonProperty("cafcassOrCymruNeedToProvideReport")
    private final YesOrNo cafcassOrCymruNeedToProvideReport;
    @CCD(
            label = "Cafcass or Cafcass Cymru needs to produce the following documentation: ",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
    )
    @JsonProperty("cafcassCymruDocuments")
    private final List<CafcassCymruDocumentsEnum> cafcassCymruDocuments;
    @CCD(
            label = "When must the reports be filed?",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate whenReportsMustBeFiled;
    @CCD(
            label = "Does local authority need to provide a report?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCruCaseworkerPrivatelawJudgeRAccess.class, CaseworkerPrivatelawLaRAccess.class}
    )
    @JsonProperty("localAuthorityNeedToProvideReport")
    private final YesOrNo localAuthorityNeedToProvideReport;
    @CCD(
            label = "Local authority needs to produce the following documentation: ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCruCaseworkerPrivatelawJudgeRAccess.class, CaseworkerPrivatelawLaRAccess.class}
    )
    @JsonProperty("localAuthorityMultipleDocuments")
    private final List<LocalAuthorityDocumentsEnum> localAuthorityMultipleDocuments;
    @CCD(
            label = "When must the reports be filed?",
            searchable = false,
            access = {CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCruCaseworkerPrivatelawJudgeRAccess.class, CaseworkerPrivatelawLaCaseworkerPrivatelawSolicitorRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate whenReportsMustBeFiledByLocalAuthority;
    @CCD(
            label = "Does this order end the involvement of Cafcass or Cafcass Cymru in this case?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
    )
    @JsonProperty("orderEndsInvolvementOfCafcassOrCymru")
    private final YesOrNo orderEndsInvolvementOfCafcassOrCymru;
    @CCD(
            label = "Do you want to serve the order now?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCitizenCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    @JsonProperty("doYouWantToServeOrder")
    private final YesOrNo doYouWantToServeOrder;
    @CCD(
            label = "What would you like to do with the order?",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
    )
    @JsonProperty("whatDoWithOrder")
    private final WhatToDoWithOrderEnum whatDoWithOrder;
}
