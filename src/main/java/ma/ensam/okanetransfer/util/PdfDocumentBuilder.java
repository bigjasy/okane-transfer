package ma.ensam.okanetransfer.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PdfDocumentBuilder {

    private static final float PAGE_WIDTH = 595f;
    private static final float PAGE_HEIGHT = 842f;
    private static final float MARGIN_X = 48f;
    private static final float HEADER_HEIGHT = 56f;
    private static final float BRAND_R = 0.059f;
    private static final float BRAND_G = 0.463f;
    private static final float BRAND_B = 0.431f;
    private static final String METRIC_SEP = "\u0001";
    private static final DateTimeFormatter FOOTER_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private PdfDocumentBuilder() {
    }

    public static byte[] build(PdfDocument document) {
        try {
            String contentStream = render(document);
            List<String> objects = new ArrayList<>();
            objects.add("<< /Type /Catalog /Pages 2 0 R >>");
            objects.add("<< /Type /Pages /Kids [3 0 R] /Count 1 >>");
            objects.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 "
                    + num(PAGE_WIDTH) + " " + num(PAGE_HEIGHT)
                    + "] /Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> /Contents 6 0 R >>");
            objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");
            objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>");
            objects.add("<< /Length " + contentStream.getBytes(StandardCharsets.ISO_8859_1).length
                    + " >>\nstream\n" + contentStream + "\nendstream");
            return assemble(objects);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to generate PDF document", ex);
        }
    }

    public static byte[] build(String title, List<String> lines) {
        PdfDocument.Builder builder = PdfDocument.builder()
                .title(title)
                .subtitle("Document généré par OkaneTransfer")
                .documentRef(shortRef());
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                builder.spacer();
                continue;
            }
            int colon = line.indexOf(':');
            if (colon > 0) {
                builder.row(line.substring(0, colon).trim(), line.substring(colon + 1).trim());
            } else if (line.startsWith("  - ")) {
                builder.tableRow("", line.substring(4).trim());
            } else {
                builder.section(line);
            }
        }
        return build(builder.build());
    }

    public static String shortRef() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private static String render(PdfDocument document) {
        StringBuilder content = new StringBuilder();
        drawHeader(content, document.brand(), document.documentRef());
        float y = PAGE_HEIGHT - HEADER_HEIGHT - 36f;

        content.append("0.15 0.18 0.22 rg\n");
        text(content, "F2", 18, MARGIN_X, y, document.title());
        y -= 22f;

        if (document.subtitle() != null && !document.subtitle().isBlank()) {
            content.append("0.35 0.39 0.45 rg\n");
            text(content, "F1", 10, MARGIN_X, y, document.subtitle());
            y -= 18f;
        }

        drawRule(content, MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y);
        y -= 20f;

        int tableRowIndex = 0;
        for (PdfDocument.Block block : document.blocks()) {
            if (y < 100f) {
                break;
            }
            if (block.type() == PdfDocument.BlockType.TABLE_ROW) {
                y = renderTableRow(content, block, y, tableRowIndex++ % 2 == 1);
            } else {
                y = renderBlock(content, block, y);
            }
        }

        float footerY = 52f;
        drawRule(content, MARGIN_X, footerY + 14f, PAGE_WIDTH - MARGIN_X, footerY + 14f);
        content.append("0.45 0.50 0.55 rg\n");
        String footerLeft = document.footer() + "  |  " + FOOTER_TIME.format(LocalDateTime.now());
        text(content, "F1", 8, MARGIN_X, footerY, footerLeft);
        text(content, "F1", 8, PAGE_WIDTH - MARGIN_X - 130f, footerY, "www.okane-transfer.ma");

        return content.toString();
    }

    private static float renderBlock(StringBuilder content, PdfDocument.Block block, float y) {
        return switch (block.type()) {
            case SPACER -> y - 8f;
            case SECTION -> {
                content.append("0.15 0.18 0.22 rg\n");
                text(content, "F2", 11, MARGIN_X, y, block.label());
                yield y - 18f;
            }
            case HIGHLIGHT -> {
                float boxHeight = 38f;
                content.append("0.93 0.97 0.95 rg\n");
                filledRect(content, MARGIN_X, y + 6f - boxHeight, PAGE_WIDTH - (2 * MARGIN_X), boxHeight);
                content.append("0.06 0.40 0.35 rg\n");
                text(content, "F2", 9, MARGIN_X + 12f, y - 4f, block.label());
                text(content, "F2", 16, MARGIN_X + 12f, y - 22f, block.value());
                yield y - boxHeight - 14f;
            }
            case ROW -> {
                content.append("0.45 0.50 0.55 rg\n");
                text(content, "F2", 9, MARGIN_X, y, block.label());
                content.append("0.15 0.18 0.22 rg\n");
                text(content, "F1", 10, MARGIN_X + 170f, y, block.value());
                yield y - 16f;
            }
            case METRICS -> renderMetrics(content, block, y);
            case TABLE_HEADER -> renderTableHeader(content, block, y);
            case TABLE_ROW -> y;
        };
    }

    private static float renderMetrics(StringBuilder content, PdfDocument.Block block, float y) {
        String[] labels = block.label().split(METRIC_SEP, -1);
        String[] values = block.value().split(METRIC_SEP, -1);
        float gap = 10f;
        float cardWidth = (PAGE_WIDTH - (2 * MARGIN_X) - (2 * gap)) / 3f;
        float cardHeight = 52f;
        float cardTop = y + 4f;

        for (int i = 0; i < 3; i++) {
            float x = MARGIN_X + i * (cardWidth + gap);
            content.append("0.96 0.98 1 rg\n");
            filledRect(content, x, cardTop - cardHeight, cardWidth, cardHeight);
            content.append("0.82 0.86 0.90 RG\n");
            strokeRect(content, x, cardTop - cardHeight, cardWidth, cardHeight);
            content.append("0.45 0.50 0.55 rg\n");
            text(content, "F1", 8, x + 10f, cardTop - 16f, i < labels.length ? labels[i] : "");
            content.append("0.06 0.40 0.35 rg\n");
            text(content, "F2", 13, x + 10f, cardTop - 34f, i < values.length ? values[i] : "");
        }
        return y - cardHeight - 18f;
    }

    private static float renderTableHeader(StringBuilder content, PdfDocument.Block block, float y) {
        float rowHeight = 20f;
        content.append("0.06 0.40 0.35 rg\n");
        filledRect(content, MARGIN_X, y - rowHeight + 6f, PAGE_WIDTH - (2 * MARGIN_X), rowHeight);
        content.append("1 1 1 rg\n");
        text(content, "F2", 9, MARGIN_X + 8f, y - 8f, block.label());
        text(content, "F2", 9, MARGIN_X + 280f, y - 8f, block.value());
        return y - rowHeight - 2f;
    }

    private static float renderTableRow(StringBuilder content, PdfDocument.Block block, float y, boolean alternate) {
        float rowHeight = 18f;
        if (alternate) {
            content.append("0.97 0.98 0.99 rg\n");
            filledRect(content, MARGIN_X, y - rowHeight + 4f, PAGE_WIDTH - (2 * MARGIN_X), rowHeight);
        }
        content.append("0.15 0.18 0.22 rg\n");
        text(content, "F1", 9, MARGIN_X + 8f, y - 8f, block.label());
        text(content, "F1", 9, MARGIN_X + 280f, y - 8f, block.value());
        return y - rowHeight;
    }

    private static void drawHeader(StringBuilder content, String brand, String documentRef) {
        content.append("q\n");
        content.append(BRAND_R).append(' ').append(BRAND_G).append(' ').append(BRAND_B).append(" rg\n");
        filledRect(content, 0, PAGE_HEIGHT - HEADER_HEIGHT, PAGE_WIDTH, HEADER_HEIGHT);
        content.append("1 1 1 rg\n");
        text(content, "F2", 16, MARGIN_X, PAGE_HEIGHT - 34f, brand);
        text(content, "F1", 9, PAGE_WIDTH - MARGIN_X - 200f, PAGE_HEIGHT - 34f, "Transfert d'argent international");
        if (documentRef != null && !documentRef.isBlank()) {
            text(content, "F1", 8, PAGE_WIDTH - MARGIN_X - 200f, PAGE_HEIGHT - 48f, "Réf. " + documentRef);
        }
        content.append("Q\n");
    }

    private static void drawRule(StringBuilder content, float x1, float y1, float x2, float y2) {
        content.append("0.82 0.86 0.90 RG\n");
        content.append("0.6 w\n");
        content.append(x1).append(' ').append(y1).append(" m\n");
        content.append(x2).append(' ').append(y2).append(" l\n");
        content.append("S\n");
    }

    private static void filledRect(StringBuilder content, float x, float y, float width, float height) {
        content.append(x).append(' ').append(y).append(' ')
                .append(width).append(' ').append(height).append(" re\n");
        content.append("f\n");
    }

    private static void strokeRect(StringBuilder content, float x, float y, float width, float height) {
        content.append(x).append(' ').append(y).append(' ')
                .append(width).append(' ').append(height).append(" re\n");
        content.append("S\n");
    }

    private static void text(StringBuilder content, String font, float size, float x, float y, String value) {
        content.append("BT\n");
        content.append('/').append(font).append(' ').append(num(size)).append(" Tf\n");
        content.append("1 0 0 1 ").append(num(x)).append(' ').append(num(y)).append(" Tm\n");
        content.append(latin1(value)).append(" Tj\n");
        content.append("ET\n");
    }

    private static String latin1(String value) {
        if (value == null) {
            return "()";
        }
        byte[] bytes = value.getBytes(StandardCharsets.ISO_8859_1);
        StringBuilder escaped = new StringBuilder("(");
        for (byte raw : bytes) {
            int character = raw & 0xFF;
            switch (character) {
                case '\\' -> escaped.append("\\\\");
                case '(' -> escaped.append("\\(");
                case ')' -> escaped.append("\\)");
                case '\r', '\n', '\t' -> escaped.append(' ');
                default -> {
                    if (character >= 32 && character <= 126) {
                        escaped.append((char) character);
                    } else {
                        escaped.append(String.format("\\%03o", character));
                    }
                }
            }
        }
        escaped.append(')');
        return escaped.toString();
    }

    private static byte[] assemble(List<String> objectBodies) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));

        List<Integer> offsets = new ArrayList<>();
        for (int index = 0; index < objectBodies.size(); index++) {
            offsets.add(output.size());
            output.write((index + 1 + " 0 obj\n").getBytes(StandardCharsets.US_ASCII));
            output.write(objectBodies.get(index).getBytes(StandardCharsets.ISO_8859_1));
            output.write("\nendobj\n".getBytes(StandardCharsets.US_ASCII));
        }

        int xrefOffset = output.size();
        output.write(("xref\n0 " + (objectBodies.size() + 1) + "\n").getBytes(StandardCharsets.US_ASCII));
        output.write("0000000000 65535 f \n".getBytes(StandardCharsets.US_ASCII));
        for (int offset : offsets) {
            output.write(String.format("%010d 00000 n \n", offset).getBytes(StandardCharsets.US_ASCII));
        }
        output.write(("trailer\n<< /Size " + (objectBodies.size() + 1)
                + " /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF\n").getBytes(StandardCharsets.US_ASCII));
        return output.toByteArray();
    }

    private static String num(float value) {
        if (value == (long) value) {
            return Long.toString((long) value);
        }
        return Float.toString(value);
    }
}
