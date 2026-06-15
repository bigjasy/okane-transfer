package ma.ensam.okanetransfer.util;

import java.util.ArrayList;
import java.util.List;

public final class PdfDocument {

    public enum BlockType { ROW, HIGHLIGHT, SECTION, SPACER, METRICS, TABLE_HEADER, TABLE_ROW }

    public static final class Block {
        private final BlockType type;
        private final String label;
        private final String value;

        private Block(BlockType type, String label, String value) {
            this.type = type;
            this.label = label;
            this.value = value;
        }

        public static Block row(String label, String value) {
            return new Block(BlockType.ROW, label, value);
        }

        public static Block highlight(String label, String value) {
            return new Block(BlockType.HIGHLIGHT, label, value);
        }

        public static Block section(String title) {
            return new Block(BlockType.SECTION, title, "");
        }

        public static Block spacer() {
            return new Block(BlockType.SPACER, "", "");
        }

        public static Block metrics(String labels, String values) {
            return new Block(BlockType.METRICS, labels, values);
        }

        public static Block tableHeader(String left, String right) {
            return new Block(BlockType.TABLE_HEADER, left, right);
        }

        public static Block tableRow(String left, String right) {
            return new Block(BlockType.TABLE_ROW, left, right);
        }

        public BlockType type() {
            return type;
        }

        public String label() {
            return label;
        }

        public String value() {
            return value;
        }
    }

    private final String brand;
    private final String title;
    private final String subtitle;
    private final String documentRef;
    private final List<Block> blocks;
    private final String footer;

    private PdfDocument(String brand, String title, String subtitle, String documentRef,
                        List<Block> blocks, String footer) {
        this.brand = brand;
        this.title = title;
        this.subtitle = subtitle;
        this.documentRef = documentRef;
        this.blocks = List.copyOf(blocks);
        this.footer = footer;
    }

    public String brand() {
        return brand;
    }

    public String title() {
        return title;
    }

    public String subtitle() {
        return subtitle;
    }

    public String documentRef() {
        return documentRef;
    }

    public List<Block> blocks() {
        return blocks;
    }

    public String footer() {
        return footer;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private static final String METRIC_SEP = "\u0001";

        private String brand = "OKANE TRANSFER";
        private String title = "Document";
        private String subtitle = "";
        private String documentRef = "";
        private final List<Block> blocks = new ArrayList<>();
        private String footer = "OkaneTransfer — Plateforme de transfert sécurisée";

        public Builder brand(String brand) {
            this.brand = brand;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder subtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public Builder documentRef(String documentRef) {
            this.documentRef = documentRef;
            return this;
        }

        public Builder row(String label, Object value) {
            blocks.add(Block.row(label, value == null ? "" : String.valueOf(value)));
            return this;
        }

        public Builder highlight(String label, Object value) {
            blocks.add(Block.highlight(label, value == null ? "" : String.valueOf(value)));
            return this;
        }

        public Builder section(String title) {
            blocks.add(Block.section(title));
            return this;
        }

        public Builder spacer() {
            blocks.add(Block.spacer());
            return this;
        }

        public Builder metrics(String label1, Object v1, String label2, Object v2, String label3, Object v3) {
            String labels = label1 + METRIC_SEP + label2 + METRIC_SEP + label3;
            String values = str(v1) + METRIC_SEP + str(v2) + METRIC_SEP + str(v3);
            blocks.add(Block.metrics(labels, values));
            return this;
        }

        public Builder tableHeader(String left, String right) {
            blocks.add(Block.tableHeader(left, right));
            return this;
        }

        public Builder tableRow(String left, Object right) {
            blocks.add(Block.tableRow(left, str(right)));
            return this;
        }

        public Builder footer(String footer) {
            this.footer = footer;
            return this;
        }

        public PdfDocument build() {
            return new PdfDocument(brand, title, subtitle, documentRef, blocks, footer);
        }

        private static String str(Object value) {
            return value == null ? "" : String.valueOf(value);
        }
    }
}
