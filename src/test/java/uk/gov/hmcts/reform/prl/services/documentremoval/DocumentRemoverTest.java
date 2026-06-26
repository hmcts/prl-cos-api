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
import java.util.List;
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

    @Test
    void removeDocument_plainDocumentArray() throws IOException {
        Map<String, Object> caseData = Map.of(
            "applicantUploadFiles",
            List.of(
                Map.of("document_url", "http://dm-store:8080/documents/1"),
                Map.of("document_url", "http://dm-store:8080/documents/2")
            )
        );

        Map<String, Object> result = documentRemover.removeDocument(caseData, "1");

        assertThat((List<?>) result.get("applicantUploadFiles")).hasSize(1);
        assertThat((List<?>) result.get("applicantUploadFiles"))
            .extracting("document_url")
            .containsExactly("http://dm-store:8080/documents/2");
    }

    @Test
    void removeDocument_plainDocumentArrayWithCamelCaseDocumentUrl() throws IOException {
        Map<String, Object> caseData = Map.of(
            "applicantUploadFiles",
            List.of(
                Map.of("documentUrl", "http://dm-store:8080/documents/1"),
                Map.of("documentUrl", "http://dm-store:8080/documents/2")
            )
        );

        Map<String, Object> result = documentRemover.removeDocument(caseData, "1");

        assertThat((List<?>) result.get("applicantUploadFiles")).hasSize(1);
        assertThat((List<?>) result.get("applicantUploadFiles"))
            .extracting("documentUrl")
            .containsExactly("http://dm-store:8080/documents/2");
    }

    @Test
    void hasDocument_returnsTrueForCamelCaseDocumentUrl() {
        Map<String, Object> caseData = Map.of(
            "citizenQuarantineDocsList",
            List.of(Map.of(
                "id",
                "collection-id",
                "value",
                Map.of("citizenQuarantineDocument", Map.of("documentUrl", "http://dm-store:8080/documents/1"))
            ))
        );

        assertThat(documentRemover.hasDocument(caseData, "1")).isTrue();
    }

    @Test
    void removeDocument_citizenFrontendOtherProceedingsOrderDocument() throws IOException {
        String documentId = "bfa681c0-552e-47e7-9c40-945d79377484";
        Map<String, Object> caseData = Map.of(
            "op_otherProceedings",
            Map.of(
                "order",
                Map.of(
                    "nonMolestationOrders",
                    List.of(Map.of(
                        "id",
                        "1",
                        "orderCopy",
                        "Yes",
                        "orderDocument",
                        Map.of(
                            "id",
                            documentId,
                            "url",
                            "http://dm-store/documents/" + documentId,
                            "filename",
                            "applicant__non-molestation_order__26062026.docx",
                            "binaryUrl",
                            "http://dm-store/documents/" + documentId + "/binary"
                        )
                    ))
                )
            )
        );

        Map<String, Object> result = documentRemover.removeDocument(caseData, documentId);

        Map<String, Object> otherProceedings = (Map<String, Object>) result.get("op_otherProceedings");
        Map<String, Object> order = (Map<String, Object>) otherProceedings.get("order");
        List<Map<String, Object>> nonMolestationOrders = (List<Map<String, Object>>) order.get("nonMolestationOrders");
        assertThat(nonMolestationOrders.getFirst()).doesNotContainKey("orderDocument");
        assertThat(nonMolestationOrders.getFirst()).containsEntry("orderCopy", "Yes");
    }

    @Test
    void hasDocument_returnsTrueForCitizenFrontendDocumentShape() {
        String documentId = "bfa681c0-552e-47e7-9c40-945d79377484";
        Map<String, Object> caseData = Map.of(
            "orderDocument",
            Map.of(
                "id",
                documentId,
                "url",
                "http://dm-store/documents/" + documentId,
                "filename",
                "applicant__non-molestation_order__26062026.docx",
                "binaryUrl",
                "http://dm-store/documents/" + documentId + "/binary"
            )
        );

        assertThat(documentRemover.hasDocument(caseData, documentId)).isTrue();
    }

    @Test
    void removeDocument_citizenFrontendOtherProceedingsJsonString() throws IOException {
        String documentId = "5473d34f-bd03-4fe9-b4f1-765b199b5255";
        Map<String, Object> otherProceedings = Map.of(
            "op_childrenInvolvedCourtCase",
            "No",
            "op_otherProceedings",
            Map.of(
                "order",
                Map.of(
                    "nonMolestationOrders",
                    List.of(Map.of(
                        "id",
                        "1",
                        "orderCopy",
                        "Yes",
                        "orderDocument",
                        Map.of(
                            "id",
                            documentId,
                            "url",
                            "http://dm-store/documents/" + documentId,
                            "filename",
                            "applicant__non-molestation_order__26062026.docx",
                            "binaryUrl",
                            "http://dm-store/documents/" + documentId + "/binary"
                        )
                    ))
                )
            )
        );
        Map<String, Object> caseData = Map.of(
            "c100RebuildOtherProceedings",
            objectMapper.writeValueAsString(otherProceedings)
        );

        Map<String, Object> result = documentRemover.removeDocument(caseData, documentId);
        Map<String, Object> updatedOtherProceedings = objectMapper.readValue(
            (String) result.get("c100RebuildOtherProceedings"),
            Map.class
        );
        Map<String, Object> opOtherProceedings = (Map<String, Object>) updatedOtherProceedings.get("op_otherProceedings");
        Map<String, Object> order = (Map<String, Object>) opOtherProceedings.get("order");
        List<Map<String, Object>> nonMolestationOrders = (List<Map<String, Object>>) order.get("nonMolestationOrders");

        assertThat(documentRemover.hasDocument(caseData, documentId)).isTrue();
        assertThat(nonMolestationOrders.getFirst()).doesNotContainKey("orderDocument");
        assertThat(nonMolestationOrders.getFirst()).containsEntry("orderCopy", "Yes");
    }
}
