package uk.gov.hmcts.reform.prl.services;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PopulateRichTextFieldsServiceTest {

    private final PopulateRichTextFieldsService populateRichTextFieldsService = new PopulateRichTextFieldsService();

    @Test
    public void testPlainTextIsWrappedInParagraphTag() {
        assertThat(populateRichTextFieldsService.populateRichTextFieldAsParagraph("recitals or preamble"))
            .isEqualTo("<p>recitals or preamble</p>");
    }

    @Test
    public void testSpecialCharactersAreEscapedBeforeWrapping() {
        assertThat(populateRichTextFieldsService.populateRichTextFieldAsParagraph("mother & father"))
            .isEqualTo("<p>mother &amp; father</p>");
    }

    @Test
    public void testNewLinesAreConvertedToBreakTags() {
        assertThat(populateRichTextFieldsService.populateRichTextFieldAsParagraph("line one\nline two"))
            .isEqualTo("<p>line one<br/>line two</p>");
    }

    @Test
    public void testStrayOpeningAngleBracketWithNoTagNameIsTreatedAsPlainText() {
        assertThat(populateRichTextFieldsService.populateRichTextFieldAsParagraph("< this is some text"))
            .isEqualTo("<p>&lt; this is some text</p>");
    }

    @Test
    public void testNumericComparisonIsTreatedAsPlainText() {
        assertThat(populateRichTextFieldsService.populateRichTextFieldAsParagraph("5 < 10 and 10 > 3"))
            .isEqualTo("<p>5 &lt; 10 and 10 &gt; 3</p>");
    }

    @Test
    public void testValueAlreadyContainingAnHtmlTagIsReturnedUnchanged() {
        String alreadyRichText = "<p>Already rich text</p>";
        assertThat(populateRichTextFieldsService.populateRichTextFieldAsParagraph(alreadyRichText))
            .isEqualTo(alreadyRichText);
    }

    @Test
    public void testValueContainingAClosingTagIsReturnedUnchanged() {
        String value = "some text</p>";
        assertThat(populateRichTextFieldsService.populateRichTextFieldAsParagraph(value))
            .isEqualTo(value);
    }
}
