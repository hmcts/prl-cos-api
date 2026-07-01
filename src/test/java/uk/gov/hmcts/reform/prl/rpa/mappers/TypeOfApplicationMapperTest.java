package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.PermissionRequiredEnum;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import javax.json.JsonObject;
import javax.json.JsonValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.class)
public class TypeOfApplicationMapperTest {

    @InjectMocks
    TypeOfApplicationMapper typeOfApplicationMapper;


    @Test
    public void testForTypeOfApplicationMapper() {
        List<OrderTypeEnum> orderType = new ArrayList<>();
        orderType.add(OrderTypeEnum.childArrangementsOrder);
        orderType.add(OrderTypeEnum.prohibitedStepsOrder);
        Document document = Document.builder().documentFileName("file.pdf").build();
        CaseData caseDataInput = CaseData.builder().ordersApplyingFor(orderType).typeOfChildArrangementsOrder(
            ChildArrangementOrderTypeEnum.liveWithOrder).natureOfOrder("test")
            .consentOrder(Yes).applicationPermissionRequired(
                PermissionRequiredEnum.getValue("Yes")).applicationPermissionRequiredReason("Need Permission")
            .uploadOrderDocForPermission(document).applicationDetails("Done").build();
        assertNotNull(typeOfApplicationMapper.map(caseDataInput));
    }

    @Test
    public void testForTypeOfApplicationMapperOption1() {
        List<OrderTypeEnum> orderType = new ArrayList<>();
        orderType.add(OrderTypeEnum.childArrangementsOrder);
        orderType.add(OrderTypeEnum.prohibitedStepsOrder);
        Document document = Document.builder().documentFileName("file.pdf").build();
        CaseData caseDataInput = CaseData.builder().ordersApplyingFor(orderType).typeOfChildArrangementsOrder(
                ChildArrangementOrderTypeEnum.liveWithOrder).natureOfOrder("test")
            .consentOrder(Yes).applicationPermissionRequired(
                PermissionRequiredEnum.getValue("No")).applicationPermissionRequiredReason("Need Permission")
            .uploadOrderDocForPermission(document).applicationDetails("Done").build();
        assertNotNull(typeOfApplicationMapper.map(caseDataInput));
    }

    @Test
    public void testForTypeOfApplicationMapperAnotherOption2() {
        List<OrderTypeEnum> orderType = new ArrayList<>();
        orderType.add(OrderTypeEnum.childArrangementsOrder);
        orderType.add(OrderTypeEnum.prohibitedStepsOrder);
        Document document = Document.builder().documentFileName("file.pdf").build();
        CaseData caseDataInput = CaseData.builder().ordersApplyingFor(orderType).typeOfChildArrangementsOrder(
                ChildArrangementOrderTypeEnum.liveWithOrder).natureOfOrder("test")
            .consentOrder(Yes).applicationPermissionRequired(PermissionRequiredEnum.getValue("noNotRequired"))
            .applicationPermissionRequiredReason("Need Permission")
            .uploadOrderDocForPermission(document).applicationDetails("Done").build();
        assertNotNull(typeOfApplicationMapper.map(caseDataInput));
    }

    @Test
    public void testForNoDataTypeOfApplicationMapper() {
        CaseData caseDataInput = CaseData.builder().build();
        assertNotNull(typeOfApplicationMapper.map(caseDataInput));
    }

    @Test
    public void testUploadOrderDocForPermission_Present() {

        Document document = Document.builder()
            .documentFileName("file.pdf")
            .build();

        CaseData caseDataInput = CaseData.builder()
            .uploadOrderDocForPermission(document)
            .build();

        JsonObject result = typeOfApplicationMapper.map(caseDataInput);

        assertEquals("file.pdf",
                     result.getString("uploadOrderDocForPermission"));
    }

    @Test
    public void testUploadOrderDocForPermission_Null() {

        CaseData caseDataInput = CaseData.builder().build();

        JsonObject result = typeOfApplicationMapper.map(caseDataInput);

        assertEquals(
            JsonValue.NULL,
            result.get("uploadOrderDocForPermission"));
    }

}
