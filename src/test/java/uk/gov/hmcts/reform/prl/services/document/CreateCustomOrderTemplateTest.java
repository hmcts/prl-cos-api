package uk.gov.hmcts.reform.prl.services.document;

import org.apache.poi.xwpf.usermodel.*;
import org.junit.jupiter.api.Test;
import java.io.FileOutputStream;

class CreateCustomOrderTemplateTest {

    @Test
    void createTemplate() throws Exception {
        XWPFDocument doc = new XWPFDocument();
        
        // Title
        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = title.createRun();
        titleRun.setBold(true);
        titleRun.setText("IN THE FAMILY COURT");
        
        // Court name
        XWPFParagraph court = doc.createParagraph();
        court.setAlignment(ParagraphAlignment.CENTER);
        court.createRun().setText("[courtName]");
        
        // Case number
        XWPFParagraph caseNo = doc.createParagraph();
        caseNo.setAlignment(ParagraphAlignment.CENTER);
        caseNo.createRun().setText("Case No: [caseNumber]");
        
        doc.createParagraph();
        
        // Judge
        XWPFParagraph judge = doc.createParagraph();
        judge.createRun().setText("Before: [judgeName]");
        
        doc.createParagraph();
        
        // Parties
        doc.createParagraph().createRun().setText("Between:");
        
        XWPFParagraph applicant = doc.createParagraph();
        applicant.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun appRun = applicant.createRun();
        appRun.setBold(true);
        appRun.setText("[applicantName]");
        
        XWPFParagraph appLabel = doc.createParagraph();
        appLabel.setAlignment(ParagraphAlignment.RIGHT);
        appLabel.createRun().setText("Applicant");
        
        XWPFParagraph and = doc.createParagraph();
        and.setAlignment(ParagraphAlignment.CENTER);
        and.createRun().setText("and");
        
        XWPFParagraph respondent = doc.createParagraph();
        respondent.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun respRun = respondent.createRun();
        respRun.setBold(true);
        respRun.setText("[respondent1Name]");
        
        XWPFParagraph respLabel = doc.createParagraph();
        respLabel.setAlignment(ParagraphAlignment.RIGHT);
        respLabel.createRun().setText("Respondent");
        
        doc.createParagraph();
        
        // Children section
        doc.createParagraph().createRun().setText("The child(ren):");
        
        // Children table
        XWPFTable table = doc.createTable(2, 3);
        
        // Header row
        XWPFTableRow headerRow = table.getRow(0);
        headerRow.getCell(0).setText("The full name(s) of the child(ren)");
        headerRow.getCell(1).setText("Boy or Girl");
        headerRow.getCell(2).setText("Date(s) of Birth");
        
        // Data row with poi-tl loop
        XWPFTableRow dataRow = table.getRow(1);
        dataRow.getCell(0).setText("[#children][fullName]");
        dataRow.getCell(1).setText("[gender]");
        dataRow.getCell(2).setText("[dob][/children]");
        
        doc.createParagraph();
        
        // Order title
        XWPFParagraph orderTitle = doc.createParagraph();
        orderTitle.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun orderRun = orderTitle.createRun();
        orderRun.setBold(true);
        orderRun.setText("[nameOfOrder]");
        
        doc.createParagraph();
        
        // Save
        try (FileOutputStream out = new FileOutputStream("src/main/resources/templates/CustomOrderHeader.docx")) {
            doc.write(out);
        }
        doc.close();
        
        System.out.println("Template created at src/main/resources/templates/CustomOrderHeader.docx");
    }
}
