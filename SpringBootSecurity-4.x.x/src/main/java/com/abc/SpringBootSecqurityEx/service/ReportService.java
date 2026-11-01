package com.abc.SpringBootSecqurityEx.service;


import com.abc.SpringBootSecqurityEx.entity.GeneratedReport;
import com.abc.SpringBootSecqurityEx.repository.GeneratedReportRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.pdf.JRPdfExporter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportService {

    private final GeneratedReportRepository reportRepository;

    public ReportService(GeneratedReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Transactional(readOnly = true)
    public Page<GeneratedReport> history(Pageable pageable) {
        return reportRepository.findAll(pageable);
    }

    // Spec §33: JasperReports PDF generation, audit-logged, parameterized.
    @Transactional
    public byte[] generateVinReport(String vin, String model, String buildDate, String qualityStatus, String generatedBy) throws JRException {
        throw new UnsupportedOperationException(
                "VIN genealogy report requires a persisted vehicle genealogy record; a certificate cannot be generated from caller-supplied fields.");
    }
}
