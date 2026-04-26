package com.center.waterorder.service;

import com.center.waterorder.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final OrderService orderService;

    public byte[] exportOrdersToExcel() throws IOException {
        List<OrderDto> orders = orderService.getAllOrders(null, null);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh sách đơn hàng");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"STT", "Tên thành viên", "Danh mục", "Tên đồ uống", "Ghi chú", "Trạng thái lấy nước"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Group orders by category
            Map<String, List<OrderDto>> ordersByCategory = orders.stream()
                    .collect(Collectors.groupingBy(OrderDto::getCategoryName));

            int rowNum = 1;
            int stt = 1;

            // Write data rows grouped by category
            for (Map.Entry<String, List<OrderDto>> entry : ordersByCategory.entrySet()) {
                for (OrderDto order : entry.getValue()) {
                    Row row = sheet.createRow(rowNum++);

                    row.createCell(0).setCellValue(stt++);
                    row.createCell(1).setCellValue(order.getMemberName());
                    row.createCell(2).setCellValue(order.getCategoryName());
                    row.createCell(3).setCellValue(order.getMenuItemName());
                    row.createCell(4).setCellValue(order.getNote() != null ? order.getNote() : "");
                    row.createCell(5).setCellValue(order.getPickedUp() ? "Đã lấy" : "Chưa lấy");
                }
            }

            // Set column widths manually (autoSizeColumn requires AWT/display, fails on headless servers)
            int[] columnWidths = {8, 25, 20, 30, 30, 20};
            for (int i = 0; i < columnWidths.length; i++) {
                sheet.setColumnWidth(i, columnWidths[i] * 256);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
