package com.rupp.payment.controller;

import com.rupp.payment.service.ReceiptPdfService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

    private final ReceiptPdfService receiptPdfService;

    public ReceiptController(ReceiptPdfService receiptPdfService) {
        this.receiptPdfService = receiptPdfService;
    }

    /** GET /api/receipts/{paymentId}/pdf            -> opens inline
     *  GET /api/receipts/{paymentId}/pdf?download=true -> forces download */
    @GetMapping("/{paymentId}/pdf")
    public ResponseEntity<byte[]> receiptPdf(@PathVariable Long paymentId,
                                             @RequestParam(defaultValue = "false") boolean download) {
        byte[] pdf = receiptPdfService.generateByPaymentId(paymentId);

        ContentDisposition disposition = (download
                ? ContentDisposition.attachment()
                : ContentDisposition.inline())
                .filename("receipt-" + paymentId + ".pdf")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
