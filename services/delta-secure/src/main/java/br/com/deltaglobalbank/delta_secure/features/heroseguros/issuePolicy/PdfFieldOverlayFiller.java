package br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public final class PdfFieldOverlayFiller {

    private static final float VALUE_FONT_SIZE = 9f;
    private static final float DEFAULT_VALUE_OFFSET_Y = 14f;

    private static final Map<String, Float> CUSTOM_OFFSETS_Y = Map.of(
        "SEGURADO", 19f
    );

    private PdfFieldOverlayFiller() {
    }

    public static byte[] fill(byte[] templateBytes, Map<String, String> fieldValues) {
        return fill(templateBytes, fieldValues, 0);
    }

    public static byte[] fill(byte[] templateBytes, Map<String, String> fieldValues, int pageIndex) {
        try (PDDocument document = Loader.loadPDF(templateBytes)) {
            Map<String, LabelPosition> positions = locateLabels(document, fieldValues.keySet(), pageIndex);
            PDPage page = document.getPage(pageIndex);

            try (PDPageContentStream contentStream = new PDPageContentStream(
                document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), VALUE_FONT_SIZE);

                for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
                    String label = entry.getKey();
                    String value = entry.getValue();
                    LabelPosition position = positions.get(label);
                    if (position != null && value != null && !value.isBlank()) {
                        float offsetY = CUSTOM_OFFSETS_Y.getOrDefault(label, DEFAULT_VALUE_OFFSET_Y);
                        contentStream.beginText();
                        contentStream.newLineAtOffset(position.x(), position.y() - offsetY);
                        contentStream.showText(sanitize(value));
                        contentStream.endText();
                    }
                }
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            document.save(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static Map<String, LabelPosition> locateLabels(PDDocument document, Set<String> labels, int pageIndex) throws IOException {
        LabelLocatorStripper stripper = new LabelLocatorStripper(labels);
        stripper.setStartPage(pageIndex + 1);
        stripper.setEndPage(pageIndex + 1);
        stripper.getText(document);
        return stripper.getFoundPositions();
    }

    private static String sanitize(String value) {
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            builder.append(c <= 0xFF ? c : '?');
        }
        return builder.toString();
    }

    private record LabelPosition(float x, float y) {
    }

    private static final class LabelLocatorStripper extends PDFTextStripper {

        private final List<String> pendingLabels;
        private final Map<String, LabelPosition> foundPositions = new LinkedHashMap<>();

        LabelLocatorStripper(Set<String> labels) throws IOException {
            this.pendingLabels = List.copyOf(labels);
            setSortByPosition(true);
        }

        Map<String, LabelPosition> getFoundPositions() {
            return foundPositions;
        }

        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
            String upperText = text.toUpperCase();
            for (String label : pendingLabels) {
                if (foundPositions.containsKey(label)) {
                    continue;
                }
                int index = upperText.indexOf(label.toUpperCase());
                if (index >= 0 && index < textPositions.size()) {
                    TextPosition anchor = textPositions.get(index);
                    float pageHeight = getCurrentPage().getMediaBox().getHeight();
                    float pdfY = pageHeight - anchor.getYDirAdj();
                    foundPositions.put(label, new LabelPosition(anchor.getXDirAdj(), pdfY));
                }
            }
            super.writeString(text, textPositions);
        }
    }
}
