/**
 * 
 */
package com.fzj.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fzj.bean.Fitness;



/**
 * @author Hongten
 * @created 2014-5-20
 */
public class ExcelUtil {
	
	public void writeExcel(List<Fitness> list, String path,String sheetName) throws Exception {
		if (list == null) {
			return;
		} else if (path == null || Common.EMPTY.equals(path)) {
			return;
		} else {
			String postfix = Util.getPostfix(path);
			if (!Common.EMPTY.equals(postfix)) {
				if (Common.OFFICE_EXCEL_2003_POSTFIX.equals(postfix)) {
					writeXls(list, path, sheetName);
				} else if (Common.OFFICE_EXCEL_2010_POSTFIX.equals(postfix)) {
					writeXlsx(list, path, sheetName);
				}
			}else{
				System.out.println(path + Common.NOT_EXCEL_FILE);
			}
		}
	}
	
	



	

	@SuppressWarnings("static-access")
	private String getValue(XSSFCell xssfRow) {
		if (xssfRow.getCellType() == xssfRow.CELL_TYPE_BOOLEAN) {
			return String.valueOf(xssfRow.getBooleanCellValue());
		} else if (xssfRow.getCellType() == xssfRow.CELL_TYPE_NUMERIC) {
			return String.valueOf(xssfRow.getNumericCellValue());
		} else {
			return String.valueOf(xssfRow.getStringCellValue());
		}
	}

	@SuppressWarnings("static-access")
	private String getValue(HSSFCell hssfCell) {
		if (hssfCell.getCellType() == hssfCell.CELL_TYPE_BOOLEAN) {
			return String.valueOf(hssfCell.getBooleanCellValue());
		} else if (hssfCell.getCellType() == hssfCell.CELL_TYPE_NUMERIC) {
			return String.valueOf(hssfCell.getNumericCellValue());
		} else {
			return String.valueOf(hssfCell.getStringCellValue());
		}
	}
	
	public void writeXls(List<Fitness> list, String path,String sheetName) throws Exception {
		if (list == null) {
			return;
		}
		int countColumnNum = list.size();
		HSSFWorkbook book = new HSSFWorkbook();
		HSSFSheet sheet = book.createSheet(sheetName);
		// option at first row.
		HSSFRow firstRow = sheet.createRow(0);
		HSSFCell[] firstCells = new HSSFCell[countColumnNum];
		String[] options = { "id", "fitness"};
		for (int j = 0; j < options.length; j++) {
			firstCells[j] = firstRow.createCell(j);
			firstCells[j].setCellValue(new HSSFRichTextString(options[j]));
		}
		//
		for (int i = 0; i < countColumnNum; i++) {
			HSSFRow row = sheet.createRow(i + 1);
			Fitness f = list.get(i);
			for (int column = 0; column < options.length; column++) {
				HSSFCell id = row.createCell(0);
				HSSFCell fitness = row.createCell(1);
				
				id.setCellValue(f.getId());
				fitness.setCellValue(f.getFitness());
				
			}
		}
		File file = new File(path);
		OutputStream os = new FileOutputStream(file);
		System.out.println(Common.WRITE_DATA + path);
		book.write(os);
		os.close();
	}
	
	public void writeXlsx(List<Fitness> list, String path,String sheetName) throws Exception {
		if (list == null) {
			return;
		}
		//XSSFWorkbook
		int countColumnNum = list.size();
		XSSFWorkbook book = new XSSFWorkbook();
		XSSFSheet sheet = book.createSheet(sheetName);
		// option at first row.
		XSSFRow firstRow = sheet.createRow(0);
		XSSFCell[] firstCells = new XSSFCell[countColumnNum];
		String[] options = { "id","fitness"};
		for (int j = 0; j < options.length; j++) {
			firstCells[j] = firstRow.createCell(j);
			firstCells[j].setCellValue(new XSSFRichTextString(options[j]));
		}
		//
		for (int i = 0; i < countColumnNum; i++) {
			XSSFRow row = sheet.createRow(i + 1);
			Fitness f = list.get(i);
			for (int column = 0; column < options.length; column++) {
				XSSFCell id = row.createCell(0);
				XSSFCell fitness = row.createCell(1);
				
				id.setCellValue(f.getId());
				fitness.setCellValue(f.getFitness());
			}
		}
		File file = new File(path);
		OutputStream os = new FileOutputStream(file);
		System.out.println(Common.WRITE_DATA + path);
		book.write(os);
		os.close();
	}
}
