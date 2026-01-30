package uk.gov.hmcts.reform.prl.services.document;

import com.deepoove.poi.XWPFTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PoiTlDocxRenderer {
    public byte[] render(byte[] templateDocxBytes, Map<String, Object> data) {
        try (var in = new ByteArrayInputStream(templateDocxBytes);
             var out = new ByteArrayOutputStream()) {
            XWPFTemplate template = XWPFTemplate.compile(in).render(data);
            template.write(out);
            template.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("poi-tl rendering failed", e);
        }
    }
}
