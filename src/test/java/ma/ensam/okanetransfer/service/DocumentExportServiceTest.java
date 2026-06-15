package ma.ensam.okanetransfer.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ma.ensam.okanetransfer.dto.report.CommissionsReportResponse;
import ma.ensam.okanetransfer.dto.report.TransfersReportResponse;
import ma.ensam.okanetransfer.dto.transfer.PayoutReceiptResponse;
import ma.ensam.okanetransfer.dto.transfer.TransferReceiptResponse;
import ma.ensam.okanetransfer.enums.TransferStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

class DocumentExportServiceTest {

    private final DocumentExportService service = new DocumentExportService();

    @Test
    void shouldGeneratePolishedTransfersReportPdf() {
        TransfersReportResponse report = new TransfersReportResponse();
        report.setGeneratedAt(LocalDateTime.now());
        report.setFormat("PDF");
        report.setTotalTransfers(42L);
        report.setTotalVolume(new BigDecimal("125000.50"));
        report.setTotalFees(new BigDecimal("3125.00"));
        report.setTransfersByStatus(Map.of("AVAILABLE", 10L, "PAID", 32L));

        byte[] pdf = service.transfersReportPdf(report);
        assertValidPdf(pdf);
    }

    @Test
    void shouldGeneratePolishedSendReceiptPdf() {
        TransferReceiptResponse receipt = new TransferReceiptResponse();
        receipt.setTransferReference("OKN-2026-0042");
        receipt.setSenderName("Client Demo");
        receipt.setBeneficiaryName("Yassine El Amrani");
        receipt.setSentAmount(new BigDecimal("1200"));
        receipt.setFeeAmount(new BigDecimal("25"));
        receipt.setReceivedAmount(new BigDecimal("73200"));
        receipt.setSourceCurrency("MAD");
        receipt.setTargetCurrency("XOF");
        receipt.setExchangeRateApplied(new BigDecimal("61"));
        receipt.setStatus(TransferStatus.AVAILABLE);
        receipt.setCreatedAt(LocalDateTime.now());
        receipt.setWithdrawalCode("847291");

        byte[] pdf = service.sendReceiptPdf(receipt);
        assertValidPdf(pdf);
    }

    @Test
    void shouldGeneratePolishedPayoutReceiptPdf() {
        PayoutReceiptResponse receipt = new PayoutReceiptResponse();
        receipt.setTransferReference("OKN-2026-0042");
        receipt.setBeneficiaryName("Yassine El Amrani");
        receipt.setPaidAmount(new BigDecimal("73200"));
        receipt.setCurrency("XOF");
        receipt.setPaidAt(LocalDateTime.now());
        receipt.setStatus(TransferStatus.PAID);
        receipt.setAgentName("Agent Okane");
        receipt.setAgencyName("Okane Dakar Plateau");
        receipt.setMaskedIdentityNumber("P1****56");

        byte[] pdf = service.payoutReceiptPdf(receipt);
        assertValidPdf(pdf);
    }

    @Test
    void shouldGenerateCommissionsReportPdf() {
        CommissionsReportResponse report = new CommissionsReportResponse();
        report.setGeneratedAt(LocalDateTime.now());
        report.setAgencyId(1L);
        report.setTotalAgencyCommissions(new BigDecimal("480"));
        report.setTotalCentralCommissions(new BigDecimal("720"));

        byte[] pdf = service.commissionsReportPdf(report);
        assertValidPdf(pdf);
    }

    private static void assertValidPdf(byte[] pdf) {
        assertTrue(pdf.length > 800, "PDF should contain branded layout");
        assertTrue(new String(pdf, 0, Math.min(8, pdf.length)).startsWith("%PDF-1.4"));
        assertTrue(new String(pdf).contains("OKANE TRANSFER"));
    }
}
