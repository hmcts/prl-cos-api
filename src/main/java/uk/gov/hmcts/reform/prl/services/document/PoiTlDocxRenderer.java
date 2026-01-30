package uk.gov.hmcts.reform.prl.services.document;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PoiTlDocxRenderer {

    // Configure poi-tl to use square brackets [placeholder] instead of {{placeholder}}
    private static final Configure CONFIG = Configure.builder()
        .buildGramer("[", "]")
        .build();

    public byte[] render(byte[] templateDocxBytes, Map<String, Object> data) {
        try (var in = new ByteArrayInputStream(templateDocxBytes);
             var out = new ByteArrayOutputStream()) {
            XWPFTemplate template = XWPFTemplate.compile(in, CONFIG).render(data);
            template.write(out);
            template.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("poi-tl rendering failed", e);
            throw new RuntimeException("poi-tl rendering failed", e);
        }
    }
}
