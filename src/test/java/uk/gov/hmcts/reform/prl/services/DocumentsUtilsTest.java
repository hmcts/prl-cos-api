package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DocumentsUtilsTest {

    @InjectMocks
    private DocumentUtils documentUtils;

    @Test
    public void testPopulateCafcassOfficerDetails() {

        String test = DocumentUtils.populateAttributeNameFromCategoryId("MIAMCertificate", "test");
        assertEquals("01234567890", test);
    }
}
