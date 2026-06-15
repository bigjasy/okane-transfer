package ma.ensam.okanetransfer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.ensam.okanetransfer.dto.report.AgencyReportResponse;
import ma.ensam.okanetransfer.dto.report.CommissionsReportResponse;
import ma.ensam.okanetransfer.dto.report.TransfersReportResponse;
import ma.ensam.okanetransfer.service.DocumentExportService;
import ma.ensam.okanetransfer.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "Operational reports for admin and manager dashboards")
@SecurityRequirement(name = "BearerAuth")
public class ReportController {

    private final ReportService reportService;
    private final DocumentExportService documentExportService;

    public ReportController(ReportService reportService, DocumentExportService documentExportService) {
        this.reportService = reportService;
        this.documentExportService = documentExportService;
    }

    @GetMapping("/transfers")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Transfers summary report")
    public ResponseEntity<?> getTransfersReport(
            Authentication authentication,
            @RequestParam(name = "format", required = false, defaultValue = "JSON") String format,
            @RequestParam(name = "agencyId", required = false) Long agencyId
    ) {
        TransfersReportResponse report = reportService.getTransfersReport(
                authentication.getName(), format, agencyId);
        return exportTransfers(report, format);
    }

    @GetMapping("/agencies/{agencyId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Agency performance report")
    public ResponseEntity<AgencyReportResponse> getAgencyReport(
            Authentication authentication,
            @PathVariable("agencyId") Long agencyId,
            @RequestParam(name = "format", required = false, defaultValue = "JSON") String format
    ) {
        return ResponseEntity.ok(reportService.getAgencyReport(
                authentication.getName(), agencyId, format));
    }

    @GetMapping("/commissions")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(summary = "Commissions report")
    public ResponseEntity<?> getCommissionsReport(
            Authentication authentication,
            @RequestParam(name = "format", required = false, defaultValue = "JSON") String format,
            @RequestParam(name = "agencyId", required = false) Long agencyId
    ) {
        CommissionsReportResponse report = reportService.getCommissionsReport(
                authentication.getName(), format, agencyId);
        if ("PDF".equalsIgnoreCase(format)) {
            byte[] pdf = documentExportService.commissionsReportPdf(report);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=commissions-report.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        }
        return ResponseEntity.ok(report);
    }

    private ResponseEntity<?> exportTransfers(TransfersReportResponse report, String format) {
        if ("PDF".equalsIgnoreCase(format)) {
            byte[] pdf = documentExportService.transfersReportPdf(report);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transfers-report.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        }
        if ("CSV".equalsIgnoreCase(format)) {
            byte[] csv = documentExportService.transfersReportCsv(report);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transfers-report.csv")
                    .contentType(new MediaType("text", "csv"))
                    .body(csv);
        }
        return ResponseEntity.ok(report);
    }
}
