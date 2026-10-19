package com.rupp.payment.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.rupp.payment.entity.Payment;
import com.rupp.payment.entity.PaymentStatus;
import com.rupp.payment.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Builds the receipt PDF. This class owns the receipt template/layout.
 * Other modules (e.g. the receipt export endpoint) should call
 * {@link #generateByPaymentId(Long)} or {@link #generate(Payment)}.
 */
@Service
public class ReceiptPdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private static final Color BRAND = new Color(0x1F, 0x3A, 0x5F);

    private final PaymentRepository paymentRepository;

    public ReceiptPdfService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public byte[] generateByPaymentId(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Payment " + paymentId + " not found"));
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Receipt is only available for completed payments");
        }
        return generate(payment);
    }

    public byte[] generate(Payment p) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A5, 36, 36, 36, 36);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BRAND);
            Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.GRAY);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);

            Paragraph title = new Paragraph("RUPP Tuition Payment", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            Paragraph sub = new Paragraph("OFFICIAL RECEIPT", subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(18);
            doc.add(sub);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{35, 65});

            addRow(table, "Receipt No.", p.getReceiptNumber(), false);
            addRow(table, "Student ID", p.getStudent().getStudentCode(), false);
            addRow(table, "Student Name", p.getStudent().getFullName(), false);
            addRow(table, "Payment Date", p.getPaidAt() == null ? "-" : p.getPaidAt().format(DATE_FMT), false);
            addRow(table, "Status", p.getStatus().name(), false);
            addRow(table, "Amount Paid", "USD " + p.getAmount().setScale(2).toPlainString(), true);
            doc.add(table);

            Paragraph footer = new Paragraph(
                    "This receipt is computer-generated and valid without a signature.", smallFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(28);
            doc.add(footer);

            doc.close();
            return out.toByteArray();
        } catch (DocumentException | java.io.IOException e) {
            throw new IllegalStateException("Failed to generate receipt PDF", e);
        }
    }

    private void addRow(PdfPTable table, String label, String value, boolean emphasize) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
        Font valueFont = FontFactory.getFont(
                emphasize ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA, emphasize ? 13 : 11, Color.BLACK);

        PdfPCell l = new PdfPCell(new Phrase(label, labelFont));
        PdfPCell v = new PdfPCell(new Phrase(value == null ? "-" : value, valueFont));
        for (PdfPCell c : new PdfPCell[]{l, v}) {
            c.setBorder(Rectangle.BOTTOM);
            c.setBorderColor(new Color(0xDD, 0xDD, 0xDD));
            c.setPaddingTop(7);
            c.setPaddingBottom(7);
        }
        table.addCell(l);
        table.addCell(v);
    }
}
