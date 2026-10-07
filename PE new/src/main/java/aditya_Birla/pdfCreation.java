package aditya_Birla;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class pdfCreation {
	public static void main(String[] args) throws Exception {
		// Sample script details
		List<String[]> data = new ArrayList<>();
		data.add(new String[] { "1", "Root Module / LocoBuzz", "SCR4846 - GetNetworkLogs", "FAIL", "00:00:00:201" });
		data.add(new String[] { "2", "Root Module / LocoBuzz", "SCR4860 - HeadlEss", "PASS", "00:00:03:670" });
		data.add(new String[] { "3", "Root Module / LocoBuzz", "SCR4867 - Tewest1", "N/A", "00:00:00:00" });
		data.add(
				new String[] { "4", "Root Module / LowrewetcoBuzz", "SCR486wrer7 - Tewerwest1", "N/A", "00:00:00:00" });

		// Execution summary fields
		String exeID = "EXEC2439";
		String suiteName = "Issue suite";
		String projectName = "Sample";
		String status = "Failed";
		String executedOn = "11 Sep 2024 10:21:24 AM (UTC+05:30)";
		String executionType = "Sequential";
		String totalDuration = "00:00:04:903";
		String machine = "TYSS-Srinivas";
		String browser = "Google Chrome 128.0.6613.120";
		String os = "Windows 11 Enterprise";

		// Call the method
		String pdfPath = create(data, exeID, suiteName, projectName, status, executedOn, executionType, totalDuration,
				machine, browser, os);

		System.out.println("PDF created at: " + pdfPath);
	}

	public static String create(List<String[]> data, String exeID, String suiteName, String projectName, String status,
			String executedOn, String executionType, String totalDuration, String machine, String browser, String os)
			throws DocumentException, IOException {
		Document document = new Document();
		String file = System.getProperty("user.home") + File.separator + "ExecutionReport.pdf";
		FileOutputStream output = new FileOutputStream(file);
		PdfWriter.getInstance(document, output);
		document.open();

		// Logo + Title in a horizontal table
		PdfPTable logoTable = new PdfPTable(2);
		logoTable.setWidthPercentage(100);
		logoTable.setWidths(new float[] { 2, 5 });

		Image logo = getLogoFromBase64(
				"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");
		logo.scaleToFit(150, 150);
		PdfPCell logoCell = new PdfPCell(logo, false);
		logoCell.setBorder(Rectangle.NO_BORDER);
		logoCell.setRowspan(2);
		logoCell.setVerticalAlignment(Element.ALIGN_TOP);

		Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
		PdfPCell titleCell = new PdfPCell(new Phrase("Execution Report", titleFont));
		titleCell.setBorder(Rectangle.NO_BORDER);
		titleCell.setVerticalAlignment(Element.ALIGN_RIGHT);
		titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

		logoTable.addCell(logoCell);
		logoTable.addCell(titleCell);

		document.add(logoTable);

		// Execution Details - Section Header
		Font sectionFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
		Paragraph detailsHeader = new Paragraph("\n\n" + "Execution Status : " + status + "\nExecution Details :    ",
				sectionFont);
		detailsHeader.setSpacingAfter(10f);
		document.add(detailsHeader);

		// Summary Table 1
		PdfPTable summaryTable1 = new PdfPTable(4);
		summaryTable1.setWidthPercentage(100);
		summaryTable1.setSpacingAfter(5f);
		summaryTable1.addCell(getCell("Execution ID", true));
		summaryTable1.addCell(getCell("Suite Name", true));
		summaryTable1.addCell(getCell("Project Name", true));
		summaryTable1.addCell(getCell("Status", true));
		summaryTable1.addCell(getCell(exeID));
		summaryTable1.addCell(getCell(suiteName));
		summaryTable1.addCell(getCell(projectName));
		summaryTable1.addCell(getCell(status));
		document.add(summaryTable1);

		// Summary Table 2 (Updated: Removed "Run Initiated By")
		PdfPTable summaryTable2 = new PdfPTable(3);
		summaryTable2.setWidthPercentage(100);
		summaryTable2.setSpacingAfter(5f);
		summaryTable2.addCell(getCell("Executed On", true));
		summaryTable2.addCell(getCell("Execution Type", true));
		summaryTable2.addCell(getCell("Total Duration", true));
		summaryTable2.addCell(getCell(executedOn));
		summaryTable2.addCell(getCell(executionType));
		summaryTable2.addCell(getCell(totalDuration));
		document.add(summaryTable2);

		// Summary Table 3
		PdfPTable summaryTable3 = new PdfPTable(3);
		summaryTable3.setWidthPercentage(100);
		summaryTable3.setSpacingAfter(10f);
		summaryTable3.addCell(getCell("Machine", true));
		summaryTable3.addCell(getCell("Browser", true));
		summaryTable3.addCell(getCell("OS", true));
		summaryTable3.addCell(getCell(machine));
		summaryTable3.addCell(getCell(browser));
		summaryTable3.addCell(getCell(os));
		document.add(summaryTable3);

		// Scripts Table Header
		Paragraph scriptsHeader = new Paragraph("Scripts Details : ", sectionFont);
		scriptsHeader.setSpacingBefore(10f);
		scriptsHeader.setSpacingAfter(5f);
		document.add(scriptsHeader);

		// Scripts Table
		PdfPTable table = new PdfPTable(5);
		table.setWidthPercentage(100);
		table.setSpacingBefore(5f);

		table.addCell(getCell("SI No", true));
		table.addCell(getCell("Module Name", true));
		table.addCell(getCell("Script Name", true));
		table.addCell(getCell("Status", true));
		table.addCell(getCell("Duration", true));

		for (String[] row : data) {
			for (int i = 0; i < row.length; i++) {
				String cell = row[i];
				if (i == 3) { // Status column
					if (cell.equalsIgnoreCase("PASS")) {
						table.addCell(getCell(cell, BaseColor.GREEN));
					} else if (cell.equalsIgnoreCase("FAIL")) {
						table.addCell(getCell(cell, BaseColor.RED));
					} else {
						table.addCell(getCell(cell, BaseColor.ORANGE));
					}
				} else {
					table.addCell(getCell(cell));
				}
			}
		}

		document.add(table);
		document.close();
		output.close();

		System.out.println("Styled Execution Report PDF generated.");
		return file;
	}

	// === Helper methods ===
	private static PdfPCell getCell(String text) {
		return getCell(text, false);
	}

	private static PdfPCell getCell(String text, boolean header) {
		Font font = new Font(Font.FontFamily.HELVETICA, 10, header ? Font.BOLD : Font.NORMAL);
		PdfPCell cell = new PdfPCell(new Phrase(text, font));
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setPadding(5f);
		return cell;
	}

	private static PdfPCell getCell(String text, BaseColor bgColor) {
		Font font = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
		PdfPCell cell = new PdfPCell(new Phrase(text, font));
		cell.setBackgroundColor(bgColor);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setPadding(5f);
		return cell;
	}

	private static Image getLogoFromBase64(String base64) throws IOException, BadElementException {
		byte[] decodedBytes = Base64.getDecoder().decode(base64);
		Image logo = Image.getInstance(decodedBytes);
		logo.scaleToFit(60, 60); // Resize if needed
		logo.setAlignment(Element.ALIGN_LEFT);
		return logo;
	}

}
