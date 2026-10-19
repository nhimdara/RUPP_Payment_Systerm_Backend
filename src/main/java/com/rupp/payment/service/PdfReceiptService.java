package com.rupp.payment.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.rupp.payment.entity.Payment;
import com.rupp.payment.entity.PaymentDetail;
import com.rupp.payment.entity.Receipt;
import com.rupp.payment.entity.Student;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfReceiptService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Color PRIMARY_COLOR = new Color(11, 60, 93);
    private static final Color SECONDARY_COLOR = new Color(44, 62, 80);
    private static final Color HEADER_BG = new Color(236, 240, 241);
    private static final Color HIGHLIGHT_BG = new Color(240, 244, 248);

    public byte[] generateReceiptPdf(Receipt receipt) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // 1. University Header
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, PRIMARY_COLOR);
            Paragraph title = new Paragraph("ROYAL UNIVERSITY OF PHNOM PENH", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(4);
            document.add(title);

            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, SECONDARY_COLOR);
            Paragraph subTitle = new Paragraph("OFFICIAL TUITION PAYMENT RECEIPT", subTitleFont);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            subTitle.setSpacingAfter(8);
            document.add(subTitle);

            Font systemFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
            Paragraph sysInfo = new Paragraph("RUPP Electronic Payment & Billing System | Finance Office", systemFont);
            sysInfo.setAlignment(Element.ALIGN_CENTER);
            sysInfo.setSpacingAfter(12);
            document.add(sysInfo);

            LineSeparator separator = new LineSeparator(1.5f, 100, PRIMARY_COLOR, Element.ALIGN_CENTER, -2);
            document.add(separator);
            document.add(new Paragraph(" "));

            // 2. Receipt Metadata Section
            Payment payment = receipt.getPayment();
            Student student = payment != null ? payment.getStudent() : null;

            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(15);
            metaTable.setWidths(new float[]{1f, 1f});

            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, SECONDARY_COLOR);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            addMetaCell(metaTable, "Receipt Number:", receipt.getReceiptNumber(), labelFont, valueFont);
            String issueDateStr = receipt.getIssueDate() != null ? receipt.getIssueDate().format(DATE_FORMATTER) : "N/A";
            addMetaCell(metaTable, "Issue Date:", issueDateStr, labelFont, valueFont);

            String statusStr = payment != null && payment.getStatus() != null ? payment.getStatus().name() : "CONFIRMED";
            addMetaCell(metaTable, "Payment Status:", statusStr, labelFont, valueFont);

            String academicYearStr = (student != null && student.getAcademicYear() != null)
                    ? student.getAcademicYear().getYearLabel() : "N/A";
            addMetaCell(metaTable, "Academic Year:", academicYearStr, labelFont, valueFont);

            document.add(metaTable);

            // 3. Student Information Box
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, PRIMARY_COLOR);
            Paragraph studentSectionTitle = new Paragraph("STUDENT INFORMATION", sectionFont);
            studentSectionTitle.setSpacingAfter(6);
            document.add(studentSectionTitle);

            PdfPTable studentTable = new PdfPTable(2);
            studentTable.setWidthPercentage(100);
            studentTable.setSpacingAfter(15);
            studentTable.setWidths(new float[]{1f, 1f});

            String studentName = student != null ? (student.getFirstName() + " " + student.getLastName()) : "N/A";
            String studentIdStr = student != null && student.getId() != null ? student.getId().toString() : "N/A";
            String studentEmail = (student != null && student.getUser() != null) ? student.getUser().getEmail() : "N/A";
            String facultyName = (student != null && student.getFaculty() != null) ? student.getFaculty().getFacultyName() : "N/A";
            String majorName = (student != null && student.getMajor() != null) ? student.getMajor().getMajorName() : "General";

            addMetaCell(studentTable, "Student Name:", studentName, labelFont, valueFont);
            addMetaCell(studentTable, "Student ID:", studentIdStr, labelFont, valueFont);
            addMetaCell(studentTable, "Email Address:", studentEmail, labelFont, valueFont);
            addMetaCell(studentTable, "Faculty / Department:", facultyName, labelFont, valueFont);
            addMetaCell(studentTable, "Major:", majorName, labelFont, valueFont);
            addMetaCell(studentTable, "Payment Date:", payment != null && payment.getPaymentDate() != null
                    ? payment.getPaymentDate().format(DATE_FORMATTER) : "N/A", labelFont, valueFont);

            document.add(studentTable);

            // 4. Payment Details Table
            Paragraph paymentSectionTitle = new Paragraph("PAYMENT BREAKDOWN", sectionFont);
            paymentSectionTitle.setSpacingAfter(6);
            document.add(paymentSectionTitle);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 2f, 2.5f, 2f});
            table.setSpacingAfter(15);

            Font thFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            addTableHeader(table, "Description", thFont);
            addTableHeader(table, "Method", thFont);
            addTableHeader(table, "Reference", thFont);
            addTableHeader(table, "Amount (USD)", thFont);

            String desc = "Tuition Fee (" + facultyName + ")";
            if (payment != null && payment.getDetails() != null && !payment.getDetails().isEmpty()) {
                for (PaymentDetail detail : payment.getDetails()) {
                    table.addCell(createCell(desc, Element.ALIGN_LEFT, valueFont));
                    table.addCell(createCell(detail.getMethod(), Element.ALIGN_CENTER, valueFont));
                    table.addCell(createCell(detail.getTransactionRef(), Element.ALIGN_CENTER, valueFont));
                    table.addCell(createCell("$" + String.format("%.2f", detail.getAmount()), Element.ALIGN_RIGHT, valueFont));
                }
            } else {
                table.addCell(createCell(desc, Element.ALIGN_LEFT, valueFont));
                table.addCell(createCell("ONLINE_PAYMENT", Element.ALIGN_CENTER, valueFont));
                table.addCell(createCell("TXN-" + receipt.getReceiptNumber(), Element.ALIGN_CENTER, valueFont));
                table.addCell(createCell("$" + String.format("%.2f", receipt.getTotalAmount()), Element.ALIGN_RIGHT, valueFont));
            }

            // Total row
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, PRIMARY_COLOR);
            PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL AMOUNT PAID:", totalFont));
            totalLabelCell.setColspan(3);
            totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalLabelCell.setPadding(8);
            totalLabelCell.setBackgroundColor(HIGHLIGHT_BG);
            table.addCell(totalLabelCell);

            PdfPCell totalValCell = new PdfPCell(new Phrase("$" + String.format("%.2f", receipt.getTotalAmount()), totalFont));
            totalValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalValCell.setPadding(8);
            totalValCell.setBackgroundColor(HIGHLIGHT_BG);
            table.addCell(totalValCell);

            document.add(table);

            // 5. Verification Notice & Footer
            document.add(new Paragraph(" "));
            Font footerNoticeFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.DARK_GRAY);
            Paragraph notice = new Paragraph(
                    "Note: This is an official computer-generated receipt from the Royal University of Phnom Penh (RUPP). " +
                            "No physical signature is required. All records are electronically archived and verified.",
                    footerNoticeFont
            );
            notice.setAlignment(Element.ALIGN_CENTER);
            notice.setSpacingAfter(8);
            document.add(notice);

            Font authFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, PRIMARY_COLOR);
            Paragraph authParagraph = new Paragraph("Royal University of Phnom Penh — Finance & Accounting Office", authFont);
            authParagraph.setAlignment(Element.ALIGN_CENTER);
            document.add(authParagraph);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate receipt PDF: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    private void addMetaCell(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        Paragraph p = new Paragraph();
        p.add(new Phrase(label + " ", labelFont));
        p.add(new Phrase(value != null ? value : "", valueFont));
        cell.addElement(p);
        table.addCell(cell);
    }

    private void addTableHeader(PdfPTable table, String title, Font font) {
        PdfPCell header = new PdfPCell(new Phrase(title, font));
        header.setBackgroundColor(HEADER_BG);
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.setPadding(6);
        table.addCell(header);
    }

    private PdfPCell createCell(String content, int alignment, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(content != null ? content : "", font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(6);
        return cell;
    }
}
