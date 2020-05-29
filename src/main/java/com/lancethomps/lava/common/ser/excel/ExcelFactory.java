package com.lancethomps.lava.common.ser.excel;
// CHECKSTYLE.OFF: OpenCSV

import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.substring;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.PackageHelper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.lancethomps.lava.common.Collect;
import com.lancethomps.lava.common.file.FileUtil;
import com.lancethomps.lava.common.logging.Logs;
import com.opencsv.CSVParser;

public class ExcelFactory {

  private static final String DEFAULT_SHEET_NAME = "data";
  private static final Logger LOG = LogManager.getLogger(ExcelFactory.class);
  private static final int MAX_CELL_CHARS = 32767;
  private static String defaultXlsxBase64;
  private static boolean hasDefaultXlsx = true;

  public static <T extends Workbook> T convertCsvToWorkbook(String data, T book) {
    try {
      Sheet sheet = createOrGetFirstSheet(book);
      String[] lines = split(data, "\n");
      CSVParser parser = Collect.getCsvParser(',');
      int row = 0;
      for (String line : lines) {
        String[] cells = parser.parseLine(line);
        Row xlsRow = sheet.createRow(row);
        for (int i = 0; i < cells.length; i++) {
          createCell(xlsRow, i, cells[i]);
        }
        row++;
      }
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Issue converting CSV to XLS!");
    }
    return book;
  }

  public static HSSFWorkbook convertCsvToXls(String data) {
    return convertCsvToWorkbook(data, createNewXls());
  }

  public static XSSFWorkbook convertCsvToXlsx(String data) {
    return convertCsvToWorkbook(data, createNewXlsx());
  }

  public static Cell createCell(Row row, int pos, String val) {
    String cellVal = (val == null) || (val.length() < MAX_CELL_CHARS) ? val : substring(val, 0, MAX_CELL_CHARS);
    Cell cell = row.createCell(pos);
    cell.setCellValue(cellVal);
    return cell;
  }

  public static HSSFWorkbook createNewXls() {
    HSSFWorkbook book = new HSSFWorkbook();
    book.createSheet(DEFAULT_SHEET_NAME);
    return book;
  }

  public static XSSFWorkbook createNewXlsx() {
    try {
      if (hasDefaultXlsx) {
        OPCPackage defaultXlsxBook = getDefaultXlsx();
        if (defaultXlsxBook != null) {
          return new XSSFWorkbook(defaultXlsxBook);
        }
        hasDefaultXlsx = false;
      }
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Issue creating default xlsx!");
      hasDefaultXlsx = false;
    }
    XSSFWorkbook book = new XSSFWorkbook();
    book.createSheet(DEFAULT_SHEET_NAME);
    return book;
  }

  public static Sheet createOrGetFirstSheet(Workbook book) {
    try {
      return book.getSheetAt(0);
    } catch (IllegalArgumentException e) {
      return book.createSheet(DEFAULT_SHEET_NAME);
    }
  }

  public static void quoteAllCsvValues(File file) throws Exception {
    Pattern pattern = Pattern.compile("(^|\n|,)(.*?)(?=$|,|\r|\n)");
    Matcher matcher = pattern.matcher(FileUtil.readFile(file));
    String updated = matcher.replaceAll("$1\"$2\"");
    FileUtil.writeFile(file, updated);
  }

  public static HSSFWorkbook readXls(File file) {
    try {
      return new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(file)));
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Issue reading XLS file [%s]", file);
    }
    return null;
  }

  public static XSSFWorkbook readXlsx(File file) {
    try {
      return new XSSFWorkbook(new FileInputStream(file));
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Issue reading XLS file [%s]", file);
    }
    return null;
  }

  private static OPCPackage getDefaultXlsx() {
    try {
      String b64 = defaultXlsxBase64;
      if (StringUtils.isBlank(b64)) {
        URL url = ExcelFactory.class.getResource("xlsx-base64.txt");
        Logs.logDebug(LOG, "Loading default XLSX from [%s]", url);
        try (InputStream is = url.openStream()) {
          b64 = IOUtils.toString(is, StandardCharsets.UTF_8);
          if (StringUtils.isNotBlank(b64)) {
            defaultXlsxBase64 = b64;
          }
        }
      }
      ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(b64));
      return PackageHelper.open(bais);
    } catch (Throwable e) {
      Logs.logError(LOG, e, "Error creating book!");
    }
    return null;
  }

}
