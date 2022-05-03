package com.partolin;

import com.codeborne.pdftest.PDF;
import com.codeborne.pdftest.matchers.ContainsExactText;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import org.apache.poi.util.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static com.codeborne.selenide.FileDownloadMode.PROXY;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class FilesParsingTest {

    @BeforeAll
    static void beforeAll() {
        Configuration.browser = "chrome";
    }

    ClassLoader cl = FilesParsingTest.class.getClassLoader();
    String pdfName = "TestPDF.pdf";
    String xlsxName = "TestXLSX.xlsx";
    String csvName = "TestCSV.csv";

    @DisplayName("Скачивание и проверка README.md")
    @Test
    void downloadTest() throws Exception {
        open("https://github.com/junit-team/junit5/blob/main/README.md");
        File textfile = $("#raw-url").scrollTo().download();

        try (InputStream is = new FileInputStream(textfile)) {
            byte[] fileContent = IOUtils.toByteArray(is); // вместо
            // byte[] fileContent = is.readAllBytes();
            String strContent = new String(fileContent, StandardCharsets.UTF_8);
            com.codeborne.pdftest.assertj.Assertions.assertThat(strContent).contains("JUnit 5");
        }
    }

    @DisplayName("Проверка количества страниц и содержимого PDF файла")
    @Test
    void pdfParsingTest() throws Exception{
        try (InputStream is = cl.getResourceAsStream("TestPDF.pdf")) {
            PDF pdf = new PDF(is);
            Assertions.assertEquals(10, pdf.numberOfPages);
            com.codeborne.pdftest.assertj.Assertions.assertThat(pdf).containsText("PDF");
        }
    }

    @DisplayName("Проверка содержимого XLS файла")
    @Test
    void xlsParsingTest() throws Exception {
        try (InputStream is = cl.getResourceAsStream("TestXLSX.xlsx")){
            XLS xls = new XLS(is);

            String stringCellValue = xls.excel
                    .getSheetAt(0)
                    .getRow(2)
                    .getCell(0)
                    .getStringCellValue();

            assertThat(stringCellValue).contains("fd2fd2fgdf2");
        }
    }

    @DisplayName("Проверка содержимого CSV файла")
    @Test
    void csvParsingTest() throws Exception {
        try (InputStream is = cl.getResourceAsStream("TestCSV.csv")) {
            CSVReader reader = new CSVReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            List<String[]> content = reader.readAll();

            org.assertj.core.api.Assertions.assertThat(content).contains(
                    new String[]{"Alexey", "Partolin"}
            );
        }
    }

    @DisplayName("Проверка файлов в ZIP архиве")
    @Test
    void zipArchiveParsingTest() throws Exception {
        ZipFile zf = new ZipFile(new File("src/test/resources/Archive.zip"));
        ZipInputStream zis = new ZipInputStream(cl.getResourceAsStream("Archive.zip"));
        ZipEntry entry;

        while((entry = zis.getNextEntry()) != null) {
            try (InputStream is = zf.getInputStream(entry)) {
                // PDF
                if (entry.getName().equals(pdfName)) {
                    PDF pdf = new PDF(is);
                    Assertions.assertEquals(10, pdf.numberOfPages);
                    com.codeborne.pdftest.assertj.Assertions.assertThat(pdf).containsText("PDF");
                }

                // XLSX
                if (entry.getName().equals(xlsxName)) {
                    XLS xls = new XLS(is);

                    String stringCellValue = xls.excel
                            .getSheetAt(0)
                            .getRow(2)
                            .getCell(0)
                            .getStringCellValue();

                    assertThat(stringCellValue).contains("fd2fd2fgdf2");
                }

                // CSV
                if (entry.getName().equals(csvName)) {
                    CSVReader reader = new CSVReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    List<String[]> content = reader.readAll();

                    org.assertj.core.api.Assertions.assertThat(content).contains(
                            new String[]{"Alexey", "Partolin"}
                    );
                }
            }
        }
    }
}