package com.center.waterorder.controller;

import com.center.waterorder.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {
    
    private final ExportService exportService;
    
    @GetMapping("/excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportExcel() throws IOException {
        byte[] excelData = exportService.exportOrdersToExcel();
        
        String filename = "danh-sach-don-hang-" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + 
                ".xlsx";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }
}
