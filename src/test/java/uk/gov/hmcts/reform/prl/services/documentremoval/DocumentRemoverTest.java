package uk.gov.hmcts.reform.prl.services.documentremoval;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentRemoverTest {
    private DocumentRemover documentRemover;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = CcdObjectMapper.getObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.registerModule(new ParameterNamesModule());
        documentRemover = new DocumentRemover(objectMapper);
    }

    @Test
    void removeDocument_c1a() throws IOException {
        Map<String, Object> caseData = DocumentRemovalTestUtils.createTestCaseMap();

        Map<String, Object> result = documentRemover.removeDocument(caseData, "1");

        CaseData updatedCaseData = objectMapper.convertValue(result, CaseData.class);
        assertThat(updatedCaseData.getC1ADocument()).isNull();
    }

    @Test
    void removeDocument_order1() throws IOException {
        Map<String, Object> caseData = DocumentRemovalTestUtils.createTestCaseMap();

        Map<String, Object> result = documentRemover.removeDocument(caseData, "2");

        CaseData updatedCaseData = objectMapper.convertValue(result, CaseData.class);
        assertThat(updatedCaseData.getC1ADocument()).isNotNull();
        assertThat(updatedCaseData.getOrderCollection()).hasSize(2);
        assertThat(updatedCaseData.getOrderCollection())
            .extracting(Element::getValue)
            .extracting("typeOfOrder")
            .containsExactlyInAnyOrder("test", "test");
        assertThat(updatedCaseData.getOrderCollection().getFirst().getValue().getOrderDocument()).isNull();
        assertThat(updatedCaseData.getOrderCollection().get(1).getValue().getOrderDocument()).isNotNull();
    }

    @Test
    void removeDocument_order2() throws IOException {
        Map<String, Object> caseData = DocumentRemovalTestUtils.createTestCaseMap();

        Map<String, Object> result = documentRemover.removeDocument(caseData, "3");

        CaseData updatedCaseData = objectMapper.convertValue(result, CaseData.class);
        assertThat(updatedCaseData.getC1ADocument()).isNotNull();
        assertThat(updatedCaseData.getOrderCollection()).hasSize(2);
        assertThat(updatedCaseData.getOrderCollection())
            .extracting(Element::getValue)
            .extracting("typeOfOrder")
            .containsExactlyInAnyOrder("test", "test");
        assertThat(updatedCaseData.getOrderCollection().getFirst().getValue().getOrderDocument()).isNotNull();
        assertThat(updatedCaseData.getOrderCollection().get(1).getValue().getOrderDocument()).isNull();
    }

    @Test
    void removeDocument_servedOrder1() throws IOException {
        Map<String, Object> caseData = DocumentRemovalTestUtils.createTestCaseMap();

        Map<String, Object> result = documentRemover.removeDocument(caseData, "4");

        CaseData updatedCaseData = objectMapper.convertValue(result, CaseData.class);
        assertThat(updatedCaseData.getC1ADocument()).isNotNull();
        assertThat(updatedCaseData.getOrderCollection()).hasSize(2);
        assertThat(updatedCaseData.getOrderCollection())
            .extracting(Element::getValue)
            .extracting("orderDocument")
            .doesNotContainNull();

        var servedApps = updatedCaseData.getFinalServedApplicationDetailsList();
        assertThat(servedApps.getFirst().getValue().getEmailNotificationDetails().getFirst().getValue().getDocs()).hasSize(2);
        assertThat(servedApps.get(1).getValue().getEmailNotificationDetails().getFirst().getValue().getDocs()).hasSize(3);
        assertThat(servedApps.get(2).getValue().getEmailNotificationDetails().getFirst().getValue().getDocs()).hasSize(3);
    }

    @Test
    void removeDocument_servedOrder2() throws IOException {
        Map<String, Object> caseData = DocumentRemovalTestUtils.createTestCaseMap();

        Map<String, Object> result = documentRemover.removeDocument(caseData, "5");

        CaseData updatedCaseData = objectMapper.convertValue(result, CaseData.class);
        assertThat(updatedCaseData.getC1ADocument()).isNotNull();
        assertThat(updatedCaseData.getOrderCollection()).hasSize(2);
        assertThat(updatedCaseData.getOrderCollection())
            .extracting(Element::getValue)
            .extracting("orderDocument")
            .doesNotContainNull();

        var servedApps = updatedCaseData.getFinalServedApplicationDetailsList();
        assertThat(servedApps.getFirst().getValue().getEmailNotificationDetails().getFirst().getValue().getDocs()).hasSize(3);
        assertThat(servedApps.get(1).getValue().getEmailNotificationDetails().getFirst().getValue().getDocs()).hasSize(2);
        assertThat(servedApps.get(2).getValue().getEmailNotificationDetails().getFirst().getValue().getDocs()).hasSize(3);
    }

    @Test
    void removeDocument_servedOrder3() throws IOException {
        Map<String, Object> caseData = DocumentRemovalTestUtils.createTestCaseMap();

        Map<String, Object> result = documentRemover.removeDocument(caseData, "6");

        CaseData updatedCaseData = objectMapper.convertValue(result, CaseData.class);
        assertThat(updatedCaseData.getC1ADocument()).isNotNull();
        assertThat(updatedCaseData.getOrderCollection()).hasSize(2);
        assertThat(updatedCaseData.getOrderCollection())
            .extracting(Element::getValue)
            .extracting("orderDocument")
            .doesNotContainNull();

        var servedApps = updatedCaseData.getFinalServedApplicationDetailsList();
        assertThat(servedApps.getFirst().getValue().getEmailNotificationDetails().getFirst().getValue().getDocs()).hasSize(3);
        assertThat(servedApps.get(1).getValue().getEmailNotificationDetails().getFirst().getValue().getDocs()).hasSize(3);
        assertThat(servedApps.get(2).getValue().getEmailNotificationDetails().getFirst().getValue().getDocs()).hasSize(2);
    }
}
