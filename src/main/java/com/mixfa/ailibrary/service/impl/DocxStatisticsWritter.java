package com.mixfa.ailibrary.service.impl;


import com.mixfa.ailibrary.misc.Utils;
import com.mixfa.ailibrary.model.statistics.StatisticsRecord;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

public class DocxStatisticsWritter {

    public static ByteArrayOutputStream createReport(StatisticsRecord statRecord) {
        XWPFDocument document = new XWPFDocument();
        var outputStream = new ByteArrayOutputStream();
        try {
            // Add Report Title
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText(statRecord.title());
            titleRun.setBold(true);
            titleRun.setFontFamily("Inter");
            titleRun.setFontSize(20);
            titleRun.addBreak();

            // Add From and To Dates
            XWPFParagraph dateParagraph = document.createParagraph();
            dateParagraph.setAlignment(ParagraphAlignment.LEFT);
            XWPFRun fromRun = dateParagraph.createRun();
            fromRun.setText("From: " + statRecord.from().format(Utils.getDateTimeFormatter()));
            fromRun.setFontFamily("Inter");
            fromRun.setFontSize(12);
            fromRun.addBreak();

            XWPFRun toRun = dateParagraph.createRun();
            toRun.setText("To:   " + statRecord.to().format(Utils.getDateTimeFormatter()));
            toRun.setFontFamily("Inter");
            toRun.setFontSize(12);
            toRun.addBreak();
            toRun.addBreak(); // Add extra break for spacing before table

            XWPFRun totalPaid = dateParagraph.createRun();
            totalPaid.setText("Total money paid:   " + statRecord.totalMoneyPaid().asString());
            totalPaid.setFontFamily("Inter");
            totalPaid.setFontSize(12);
            totalPaid.addBreak();
            totalPaid.addBreak(); // Add extra break for spacing before table

            // Create Table
            XWPFTable table = document.createTable(1, 4); // Start with 1 row (for headers) and 4 columns

            // Set table width to 100%
            CTTblWidth width = table.getCTTbl().addNewTblPr().addNewTblW();
            width.setType(STTblWidth.DXA);
            width.setW(BigInteger.valueOf(9072)); // Standard page width minus margins

            // Set table borders
            table.setInsideHBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000");
            table.setInsideVBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000");
            table.setBottomBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000");
            table.setLeftBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000");
            table.setRightBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000");
            table.setTopBorder(XWPFTable.XWPFBorderType.SINGLE, 4, 0, "000000");

            // Populate table header row
            XWPFTableRow headerRow = table.getRow(0);
            setCellText(headerRow.getCell(0), "Book Title", true);
            setCellText(headerRow.getCell(1), "Book ID", true);
            setCellText(headerRow.getCell(2), "Money Paid", true);
            setCellText(headerRow.getCell(3), "Borrowing Count", true);

            // Populate data rows from BookStatistics list
            for (var stats : statRecord.statistics()) {
                XWPFTableRow dataRow = table.createRow(); // Create a new row for each statistics entry
                setCellText(dataRow.getCell(0), stats.book().title(), false);
                setCellText(dataRow.getCell(1), stats.book().id().toHexString(), false);
                setCellText(dataRow.getCell(2), stats.moneyPaid().asString(), false);
                setCellText(dataRow.getCell(3), String.valueOf(stats.borrowingCount()), false);
            }

            document.write(outputStream);

        } catch (IOException e) {
            System.err.println("An error occurred while creating the DOCX file: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                document.close(); // Close the document to release resources
            } catch (IOException e) {
                System.err.println("Error closing document: " + e.getMessage());
            }
        }

        return outputStream;
    }

    private static void setCellText(XWPFTableCell cell, String text, boolean isBold) {
        // Clear existing paragraphs in the cell (important for clean content)
        while (cell.getParagraphs().size() > 0) {
            cell.removeParagraph(0);
        }

        XWPFParagraph paragraph = cell.addParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER); // Center align cell content
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(isBold);
        run.setFontFamily("Inter");
        run.setFontSize(11);
    }
}
