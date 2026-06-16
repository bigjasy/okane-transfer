#!/usr/bin/env python3
"""Generate RECETTES_DE_TEST_OKANE_TRANSFER.pdf from the markdown source."""

from __future__ import annotations

import sys
from pathlib import Path

try:
    from fpdf import FPDF
except ImportError:
    import subprocess

    subprocess.check_call([sys.executable, "-m", "pip", "install", "fpdf2", "-q"])
    from fpdf import FPDF

ROOT = Path(__file__).resolve().parents[1]
MD = ROOT / "docs" / "RECETTES_DE_TEST_OKANE_TRANSFER.md"
OUT = ROOT / "docs" / "RECETTES_DE_TEST_OKANE_TRANSFER.pdf"


class RecipePdf(FPDF):
    def header(self):
        self.set_font("Helvetica", "B", 9)
        self.set_text_color(80, 80, 80)
        self.cell(0, 8, f"OkaneTransfer - Recettes de test - Page {self.page_no()}", align="C", new_x="LMARGIN", new_y="NEXT")
        self.set_draw_color(15, 118, 110)
        self.line(self.l_margin, self.get_y(), self.w - self.r_margin, self.get_y())
        self.ln(4)

    def footer(self):
        self.set_y(-12)
        self.set_font("Helvetica", "I", 8)
        self.set_text_color(120, 120, 120)
        self.cell(0, 8, "Document de test - Equipe OkaneTransfer", align="C")

    def write_line(self, text: str, size: int = 9, style: str = "", h: float = 5) -> None:
        self.set_x(self.l_margin)
        self.set_font("Helvetica", style, size)
        self.set_text_color(0, 0, 0)
        safe = text.encode("latin-1", "replace").decode("latin-1")
        self.multi_cell(self.epw, h, safe)


def write_md_as_pdf(pdf: RecipePdf, content: str) -> None:
    pdf.set_auto_page_break(auto=True, margin=18)
    in_code = False

    for raw_line in content.splitlines():
        line = raw_line.rstrip()
        line = line.replace("\u2014", "-").replace("\u2013", "-").replace("\u2192", "->")

        if line.startswith("```"):
            in_code = not in_code
            continue

        if in_code:
            pdf.write_line("  " + line, size=8, style="", h=4)
            continue

        if line.startswith("# "):
            pdf.ln(3)
            pdf.set_text_color(15, 118, 110)
            pdf.write_line(line[2:], size=15, style="B", h=8)
            continue

        if line.startswith("## "):
            pdf.ln(2)
            pdf.write_line(line[3:], size=12, style="B", h=7)
            continue

        if line.startswith("### "):
            pdf.ln(1)
            pdf.write_line(line[4:], size=11, style="B", h=6)
            continue

        if line.startswith("|") and "|" in line[1:]:
            cells = [c.strip() for c in line.strip("|").split("|")]
            if all(set(c) <= {"-"} for c in cells):
                continue
            pdf.write_line(" | ".join(cells), size=8, h=4)
            continue

        if line.startswith("---"):
            pdf.ln(2)
            continue

        if line.startswith("- "):
            pdf.write_line("- " + line[2:], size=9, h=5)
            continue

        if not line.strip():
            pdf.ln(2)
            continue

        pdf.write_line(line.replace("**", ""), size=9, h=5)


def main() -> None:
    if not MD.exists():
        print(f"Missing source file: {MD}")
        sys.exit(1)

    pdf = RecipePdf()
    pdf.set_margins(12, 15, 12)
    pdf.add_page()
    write_md_as_pdf(pdf, MD.read_text(encoding="utf-8"))
    OUT.parent.mkdir(parents=True, exist_ok=True)
    pdf.output(str(OUT))
    print(f"Generated: {OUT}")


if __name__ == "__main__":
    main()
