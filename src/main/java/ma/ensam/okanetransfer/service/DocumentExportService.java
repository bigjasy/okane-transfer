package ma.ensam.okanetransfer.service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import ma.ensam.okanetransfer.dto.report.CommissionsReportResponse;
import ma.ensam.okanetransfer.dto.report.TransfersReportResponse;
import ma.ensam.okanetransfer.dto.transfer.PayoutReceiptResponse;
import ma.ensam.okanetransfer.dto.transfer.TransferReceiptResponse;
import ma.ensam.okanetransfer.util.PdfDocument;
import ma.ensam.okanetransfer.util.PdfDocumentBuilder;
import org.springframework.stereotype.Service;

@Service
public class DocumentExportService {

    private static final String FOOTER_INTERNAL = "Document confidentiel — usage interne OkaneTransfer";
    private static final String FOOTER_RECEIPT = "Conservez ce reçu comme preuve de transaction.";

    public byte[] transfersReportPdf(TransfersReportResponse report) {
        PdfDocument.Builder document = PdfDocument.builder()
                .title("Rapport des transferts")
                .subtitle("Synthèse opérationnelle")
                .documentRef(PdfDocumentBuilder.shortRef())
                .metrics(
                        "Total transferts", report.getTotalTransfers(),
                        "Volume total", report.getTotalVolume(),
                        "Total frais", report.getTotalFees())
                .row("Généré le", report.getGeneratedAt())
                .row("Format", report.getFormat())
                .section("Répartition par statut")
                .tableHeader("Statut", "Nombre");

        if (report.getTransfersByStatus() == null || report.getTransfersByStatus().isEmpty()) {
            document.tableRow("—", "Aucun transfert sur cette période");
        } else {
            for (Map.Entry<String, Long> entry : report.getTransfersByStatus().entrySet()) {
                document.tableRow(entry.getKey(), entry.getValue());
            }
        }

        return PdfDocumentBuilder.build(document.footer(FOOTER_INTERNAL).build());
    }

    public byte[] commissionsReportPdf(CommissionsReportResponse report) {
        return PdfDocumentBuilder.build(PdfDocument.builder()
                .title("Rapport des commissions")
                .subtitle("Part agence et part centrale")
                .documentRef(PdfDocumentBuilder.shortRef())
                .metrics(
                        "Commissions agence", report.getTotalAgencyCommissions(),
                        "Commissions centrale", report.getTotalCentralCommissions(),
                        "Agence filtrée", report.getAgencyId() != null ? report.getAgencyId() : "Toutes")
                .row("Généré le", report.getGeneratedAt())
                .footer(FOOTER_INTERNAL)
                .build());
    }

    public byte[] transfersReportCsv(TransfersReportResponse report) {
        StringBuilder csv = new StringBuilder("metric,value\n");
        csv.append("generatedAt,").append(report.getGeneratedAt()).append('\n');
        csv.append("totalTransfers,").append(report.getTotalTransfers()).append('\n');
        csv.append("totalVolume,").append(report.getTotalVolume()).append('\n');
        csv.append("totalFees,").append(report.getTotalFees()).append('\n');
        for (Map.Entry<String, Long> entry : report.getTransfersByStatus().entrySet()) {
            csv.append("status_").append(entry.getKey()).append(',').append(entry.getValue()).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] payoutReceiptPdf(PayoutReceiptResponse receipt) {
        return PdfDocumentBuilder.build(PdfDocument.builder()
                .title("Reçu de paiement")
                .subtitle("Confirmation de retrait bénéficiaire")
                .documentRef(receipt.getTransferReference())
                .highlight("Montant payé", receipt.getPaidAmount() + " " + receipt.getCurrency())
                .row("Référence", receipt.getTransferReference())
                .row("Bénéficiaire", receipt.getBeneficiaryName())
                .row("Date de paiement", receipt.getPaidAt())
                .row("Statut", receipt.getStatus())
                .row("Agent", receipt.getAgentName())
                .row("Agence", receipt.getAgencyName())
                .row("Identité", receipt.getMaskedIdentityNumber())
                .footer(FOOTER_RECEIPT)
                .build());
    }

    public byte[] sendReceiptPdf(TransferReceiptResponse receipt) {
        PdfDocument.Builder document = PdfDocument.builder()
                .title("Reçu d'envoi")
                .subtitle("Confirmation de transfert international")
                .documentRef(receipt.getTransferReference())
                .highlight("Montant reçu", receipt.getReceivedAmount() + " " + receipt.getTargetCurrency())
                .row("Référence", receipt.getTransferReference())
                .row("Expéditeur", receipt.getSenderName())
                .row("Bénéficiaire", receipt.getBeneficiaryName())
                .row("Montant envoyé", receipt.getSentAmount() + " " + receipt.getSourceCurrency())
                .row("Frais", receipt.getFeeAmount() + " " + receipt.getSourceCurrency())
                .row("Taux de change", receipt.getExchangeRateApplied())
                .row("Statut", receipt.getStatus())
                .row("Date", receipt.getCreatedAt());

        if (receipt.getWithdrawalCode() != null && !receipt.getWithdrawalCode().isBlank()) {
            document.section("Code de retrait")
                    .highlight("À communiquer au bénéficiaire", receipt.getWithdrawalCode());
        }

        return PdfDocumentBuilder.build(
                document.footer("Présentez le code de retrait au guichet pour le décaissement.").build());
    }
}
