package uk.gov.hmcts.reform.prl.mapper.citizen.awp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class CitizenAwpMapperTest {

    private CitizenAwpMapper mapper;

    // reflect: private String categoryForParty(String)
    private Method categoryForParty;

    // reflect: private static List<Element<Document>> toDocElementsWithCategory(List<Document>, String)
    private Method toDocElementsWithCategory;

    @BeforeEach
    void setUp() throws Exception {
        mapper = new CitizenAwpMapper();

        categoryForParty = CitizenAwpMapper.class.getDeclaredMethod("categoryForParty", String.class);
        categoryForParty.setAccessible(true);

        toDocElementsWithCategory = CitizenAwpMapper.class
            .getDeclaredMethod("toDocElementsWithCategory", List.class, String.class);
        toDocElementsWithCategory.setAccessible(true);
    }

    @Test
    void applicantParty_setsApplicantCategoryOnDocs() throws Exception {
        String party = "applicant";
        String category = (String) categoryForParty.invoke(mapper, party);
        Document inputDoc = Document.builder()
            .documentUrl("doc-url")
            .documentBinaryUrl("bin-url")
            .documentFileName("c2.pdf")
            .build();

        @SuppressWarnings("unchecked")
        List<Element<Document>> result =
            (List<Element<Document>>) toDocElementsWithCategory.invoke(null, List.of(inputDoc), category);

        assertEquals(1, result.size());
        Document mapped = result.get(0).getValue();
        assertNotNull(mapped);
        assertEquals("applicationsWithinProceedings", mapped.getCategoryId());
        // sanity: other fields preserved
        assertEquals("c2.pdf", mapped.getDocumentFileName());
    }

    @Test
    void respondentParty_setsRespondentCategoryOnDocs() throws Exception {
        String party = "respondent";
        String category = (String) categoryForParty.invoke(mapper, party);
        Document inputDoc = Document.builder()
            .documentUrl("doc-url")
            .documentBinaryUrl("bin-url")
            .documentFileName("c2.pdf")
            .build();

        @SuppressWarnings("unchecked")
        List<Element<Document>> result =
            (List<Element<Document>>) toDocElementsWithCategory.invoke(null, List.of(inputDoc), category);

        assertEquals(1, result.size());
        Document mapped = result.get(0).getValue();
        assertNotNull(mapped);
        assertEquals("applicationsWithinProceedingsRes", mapped.getCategoryId());
    }

    @Test
    void unknownParty_setsRespondentCategoryOnDocs() throws Exception {
        String party = "some-random-party";
        String category = (String) categoryForParty.invoke(mapper, party);
        Document inputDoc = Document.builder()
            .documentUrl("doc-url")
            .documentBinaryUrl("bin-url")
            .documentFileName("c2.pdf")
            .build();

        @SuppressWarnings("unchecked")
        List<Element<Document>> result =
            (List<Element<Document>>) toDocElementsWithCategory.invoke(null, List.of(inputDoc), category);

        Document mapped = result.get(0).getValue();
        assertNotNull(mapped);
        assertEquals("applicationsFromOtherProceedings", mapped.getCategoryId());
    }

    @Test
    void undefinedParty_setsRespondentCategoryOnDocs() throws Exception {
        String party = "undefined";
        String category = (String) categoryForParty.invoke(mapper, party);
        Document inputDoc = Document.builder()
            .documentUrl("doc-url")
            .documentBinaryUrl("bin-url")
            .documentFileName("c2.pdf")
            .build();

        @SuppressWarnings("unchecked")
        List<Element<Document>> result =
            (List<Element<Document>>) toDocElementsWithCategory.invoke(null, List.of(inputDoc), category);

        Document mapped = result.get(0).getValue();
        assertNotNull(mapped);
        assertEquals("applicationsFromOtherProceedings", mapped.getCategoryId());
    }
}
