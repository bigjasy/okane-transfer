package ma.ensam.okanetransfer.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PdfDocumentBuilderTest {

    @Test
    void shouldGenerateBrandedReceiptWithFrenchAccents() {
        byte[] pdf = PdfDocumentBuilder.build(PdfDocument.builder()
                .title("Reçu d'envoi")
                .subtitle("Synthèse opérationnelle")
                .documentRef("OKN-2026-0042")
                .highlight("Montant reçu", "73 200 XOF")
                .row("Généré le", "15/06/2026")
                .row("Bénéficiaire", "Yassine El Amrani")
                .section("Code de retrait")
                .highlight("À communiquer", "847291")
                .footer("Document confidentiel — OkaneTransfer")
                .build());

        String header = new String(pdf, 0, Math.min(8, pdf.length));
        String body = new String(pdf);

        assertTrue(pdf.length > 1200);
        assertTrue(header.startsWith("%PDF-1.4"));
        assertTrue(body.contains("OKANE TRANSFER"));
    }
}
