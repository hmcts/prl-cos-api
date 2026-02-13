package uk.gov.hmcts.reform.prl.services.document;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
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

    // LoopRowTableRenderPolicy for dynamic table row generation
    // onSameLine=true means the marker {{children}} is in the same row as the field placeholders
    private static final LoopRowTableRenderPolicy LOOP_ROW_POLICY = new LoopRowTableRenderPolicy(true);

    // Configure poi-tl with default {{placeholder}} grammar
    // Bind 'children' and 'respondents' to LoopRowTableRenderPolicy for dynamic table rows
    // Loop row fields use [fieldName] syntax (built into LoopRowTableRenderPolicy)
    private static final Configure CONFIG = Configure.builder()
        .bind("children", LOOP_ROW_POLICY)
        .bind("respondents", LOOP_ROW_POLICY)
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
