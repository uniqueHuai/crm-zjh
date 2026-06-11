package com.crm.ai.export.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportExportService {

    @Value("${ai.export.dir:./temp/reports}")
    private String exportDir;

    private final Map<String, ExportFile> exportCache = new ConcurrentHashMap<>();
    private ScheduledExecutorService cleaner;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(exportDir));
        } catch (IOException e) {
            log.warn("Failed to create export dir: {}", exportDir, e);
        }
        cleaner = Executors.newSingleThreadScheduledExecutor();
        cleaner.scheduleAtFixedRate(this::cleanExpired, 1, 1, TimeUnit.HOURS);
    }

    @PreDestroy
    public void destroy() {
        if (cleaner != null) cleaner.shutdown();
    }

    public String exportExcel(String fileName, List<String> headers, List<List<Object>> rawData) {
        String token = UUID.randomUUID().toString();
        String safeName = sanitize(fileName);
        String filePath = exportDir + File.separator + token + "_" + safeName + ".xlsx";

        List<List<String>> headList = headers.stream().map(Collections::singletonList).collect(Collectors.toList());

        // 将数据中特殊类型（如 Timestamp）转为字符串，避免 EasyExcel 转换异常
        List<List<Object>> data = rawData.stream()
                .map(row -> row.stream()
                        .map(ReportExportService::toSafeCell)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        EasyExcel.write(filePath)
                .head(headList)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet("Sheet1")
                .doWrite(data);

        exportCache.put(token, new ExportFile(filePath, safeName + ".xlsx", System.currentTimeMillis()));
        log.info("Export generated: {} token={}", filePath, token);
        return token;
    }

    public String exportExcelWithSheets(String fileName, List<SheetData> sheets) {
        String token = UUID.randomUUID().toString();
        String safeName = sanitize(fileName);
        String filePath = exportDir + File.separator + token + "_" + safeName + ".xlsx";

        var workbook = EasyExcel.write(filePath).excelType(ExcelTypeEnum.XLSX).build();
        boolean first = true;
        for (SheetData sheet : sheets) {
            List<List<String>> headList = sheet.headers().stream()
                    .map(Collections::singletonList).collect(Collectors.toList());
            List<List<Object>> safeData = sheet.data().stream()
                    .map(row -> row.stream().map(ReportExportService::toSafeCell).collect(Collectors.toList()))
                    .collect(Collectors.toList());
            var writer = EasyExcel.writerSheet(sheet.sheetName()).head(headList).build();
            workbook.write(safeData, writer);
            if (first) first = false;
        }
        workbook.finish();

        exportCache.put(token, new ExportFile(filePath, safeName + ".xlsx", System.currentTimeMillis()));
        log.info("Multi-sheet export generated: {} token={}", filePath, token);
        return token;
    }

    public ExportFile getExportFile(String token) {
        ExportFile ef = exportCache.get(token);
        if (ef == null) return null;
        File f = new File(ef.filePath);
        if (!f.exists()) {
            exportCache.remove(token);
            return null;
        }
        return ef;
    }

    public void removeToken(String token) {
        ExportFile ef = exportCache.remove(token);
        if (ef != null) {
            File f = new File(ef.filePath);
            if (f.exists()) f.delete();
        }
    }

    private void cleanExpired() {
        long now = System.currentTimeMillis();
        long expiry = 3600_000;
        exportCache.entrySet().removeIf(e -> {
            if (now - e.getValue().createdAt > expiry) {
                File f = new File(e.getValue().filePath);
                if (f.exists()) f.delete();
                return true;
            }
            return false;
        });
    }

    private String sanitize(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private static Object toSafeCell(Object value) {
        if (value == null) return "";
        if (value instanceof Timestamp ts) {
            return ts.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (value instanceof java.sql.Date d) {
            return d.toLocalDate().toString();
        }
        if (value instanceof java.util.Date d) {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d);
        }
        return value;
    }

    public record ExportFile(String filePath, String displayName, long createdAt) {}

    public record SheetData(String sheetName, List<String> headers, List<List<Object>> data) {}
}
