package uk.gov.hmcts.reform.prl.services;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.regex.Pattern;

@Service
public class PopulateRichTextFieldsService {

    private static final String PARAGRAPH_OPEN_TAG = "<p>";
    private static final String PARAGRAPH_CLOSE_TAG = "</p>";
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[a-zA-Z/][^<>]*>");

    public String populateRichTextFieldAsParagraph(String plainText) {

        if (StringUtils.isBlank(plainText) || HTML_TAG_PATTERN.matcher(plainText).find()) {
            return plainText;
        }

        String newRichTextValue = HtmlUtils.htmlEscape(plainText).replace("\n", "<br/>");

        return PARAGRAPH_OPEN_TAG + newRichTextValue + PARAGRAPH_CLOSE_TAG;
    }
}
