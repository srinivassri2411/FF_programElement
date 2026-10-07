package specializedAutomation2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyss.optimize.common.util.CommonConstants;
import com.tyss.optimize.nlp.util.Nlp;
import com.tyss.optimize.nlp.util.NlpException;
import com.tyss.optimize.nlp.util.NlpRequestModel;
import com.tyss.optimize.nlp.util.NlpResponseModel;
import com.tyss.optimize.nlp.util.annotation.InputParam;
import com.tyss.optimize.nlp.util.annotation.InputParams;
import com.tyss.optimize.nlp.util.annotation.ReturnType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ExcelToExcelComparisonBasedOnTolerance implements Nlp {

    @InputParams({
        @InputParam(name = "Tolerance JSON Path", type = "java.lang.String"),
        @InputParam(name = "OCP Excel Path", type = "java.lang.String"),
        @InputParam(name = "EBS Excel Path", type = "java.lang.String"),
        @InputParam(name = "Output Excel Path", type = "java.lang.String")
    })
    @ReturnType(name = "Output Path", type = "java.lang.String")

    @Override
    public List<String> getTestParameters() throws NlpException {
        return new ArrayList<>();
    }

    @Override
    public StringBuilder getTestCode() throws NlpException {
        return new StringBuilder();
    }

    @Override
    public NlpResponseModel execute(NlpRequestModel nlpRequestModel) throws NlpException {
        Map<String, Object> attributes = nlpRequestModel.getAttributes();
        NlpResponseModel nlpResponseModel = new NlpResponseModel();

        try {
            String jsonPath = (String) attributes.get("Tolerance JSON Path");
            String ocpPath = (String) attributes.get("OCP Excel Path");
            String ebsPath = (String) attributes.get("EBS Excel Path");
            String outputPath = (String) attributes.get("Output Excel Path");

            // Execute the comparison
            performComparison(jsonPath, ocpPath, ebsPath, outputPath);

            nlpResponseModel.setMessage("Comparison completed successfully. Output file: " + outputPath);
            nlpResponseModel.setStatus(CommonConstants.pass);
            nlpResponseModel.getAttributes().put("Output Path", outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            nlpResponseModel.setMessage("Comparison failed: " + e.getMessage());
            nlpResponseModel.setStatus(CommonConstants.fail);
        }

        return nlpResponseModel;
    }

    // ------------------ Core comparison logic ------------------

    private void performComparison(String jsonPath, String ocpPath, String ebsPath, String outputPath) throws IOException {
        // 1. Read tolerance JSON
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Double> toleranceMap = mapper.readValue(new File(jsonPath), Map.class);

        // 2. Read Excel files
        List<RowData> ocpRows = readExcel(ocpPath, true);   // strip 'E'
        List<RowData> ebsRows = readExcel(ebsPath, false);

        // 3. Group by assignment -> element -> list of values (in order)
        Map<String, Map<String, List<Double>>> ocpData = groupRows(ocpRows);
        Map<String, Map<String, List<Double>>> ebsData = groupRows(ebsRows);

        // 4. Compare and collect rows that exceed tolerance
        List<ResultRow> resultRows = new ArrayList<>();

        for (String assignment : ocpData.keySet()) {
            Map<String, List<Double>> ocpElemMap = ocpData.get(assignment);
            Map<String, List<Double>> ebsElemMap = ebsData.getOrDefault(assignment, new HashMap<>());

            for (String element : ocpElemMap.keySet()) {
                if (!toleranceMap.containsKey(element)) continue; // only elements with tolerance

                List<Double> ocpVals = ocpElemMap.get(element);
                List<Double> ebsVals = ebsElemMap.getOrDefault(element, new ArrayList<>());

                int size = Math.min(ocpVals.size(), ebsVals.size());
                for (int i = 0; i < size; i++) {
                    double ocp = ocpVals.get(i);
                    double ebs = ebsVals.get(i);
                    double diff = ocp - ebs;
                    double tol = toleranceMap.get(element);
                    if (Math.abs(diff) > tol) {
                        resultRows.add(new ResultRow(assignment, element, ocp, ebs, diff));
                    }
                }
                // If counts differ, extra values are ignored (could be logged if needed)
            }
        }

        // 5. Write output workbook
        try (Workbook wb = new XSSFWorkbook()) {
            // --- Consolidated sheet ---
            Sheet consSheet = wb.createSheet("Consolidated");
            createHeaderRow(consSheet, "Assignment Number", "Element Name",
                    "OCP Pay Value", "EBS Pay Value", "Difference");
            int rowNum = 1;
            for (ResultRow r : resultRows) {
                Row row = consSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.assignment);
                row.createCell(1).setCellValue(r.element);
                row.createCell(2).setCellValue(r.ocpValue);
                row.createCell(3).setCellValue(r.ebsValue);
                row.createCell(4).setCellValue(r.difference);
            }

            // --- Per‑element sheets (only for elements that have at least one result row) ---
            Map<String, List<ResultRow>> rowsByElement = resultRows.stream()
                    .collect(Collectors.groupingBy(r -> r.element));

            for (String element : toleranceMap.keySet()) {
                List<ResultRow> elementRows = rowsByElement.getOrDefault(element, new ArrayList<>());
                if (elementRows.isEmpty()) continue; // skip empty sheets

                Sheet sheet = wb.createSheet(element);
                createHeaderRow(sheet, "Assignment Number", "OCP Pay Value",
                        "EBS Pay Value", "Difference");
                int r = 1;
                for (ResultRow row : elementRows) {
                    Row dataRow = sheet.createRow(r++);
                    dataRow.createCell(0).setCellValue(row.assignment);
                    dataRow.createCell(1).setCellValue(row.ocpValue);
                    dataRow.createCell(2).setCellValue(row.ebsValue);
                    dataRow.createCell(3).setCellValue(row.difference);
                }
            }

            // Save
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                wb.write(fos);
            }
        }
    }

    // ------------------ Helper methods ------------------

    private void createHeaderRow(Sheet sheet, String... headers) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }
    }

    private List<RowData> readExcel(String filePath, boolean stripE) throws IOException {
        List<RowData> rows = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = wb.getSheetAt(0);
            Iterator<Row> it = sheet.iterator();
            if (it.hasNext()) it.next(); // skip header

            while (it.hasNext()) {
                Row row = it.next();
                String assignment = getCellString(row, 0);
                String element = getCellString(row, 1);
                Double value = getCellNumeric(row, 2);
                if (assignment == null || element == null || value == null) continue;

                if (stripE && assignment.startsWith("E")) {
                    assignment = assignment.substring(1);
                }
                rows.add(new RowData(assignment, element, value));
            }
        }
        return rows;
    }

    private String getCellString(Row row, int idx) {
        Cell cell = row.getCell(idx);
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private Double getCellNumeric(Row row, int idx) {
        Cell cell = row.getCell(idx);
        if (cell == null) return null;
        try {
            return cell.getNumericCellValue();
        } catch (IllegalStateException e) {
            String str = cell.getStringCellValue().trim();
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    private Map<String, Map<String, List<Double>>> groupRows(List<RowData> rows) {
        Map<String, Map<String, List<Double>>> grouped = new LinkedHashMap<>();
        for (RowData rd : rows) {
            grouped.computeIfAbsent(rd.assignment, k -> new LinkedHashMap<>())
                   .computeIfAbsent(rd.element, k -> new ArrayList<>())
                   .add(rd.value);
        }
        return grouped;
    }

    // ------------------ Inner data classes ------------------

    static class RowData {
        String assignment;
        String element;
        double value;
        RowData(String assignment, String element, double value) {
            this.assignment = assignment;
            this.element = element;
            this.value = value;
        }
    }

    static class ResultRow {
        String assignment;
        String element;
        double ocpValue;
        double ebsValue;
        double difference;
        ResultRow(String assignment, String element, double ocp, double ebs, double diff) {
            this.assignment = assignment;
            this.element = element;
            this.ocpValue = ocp;
            this.ebsValue = ebs;
            this.difference = diff;
        }
    }
}