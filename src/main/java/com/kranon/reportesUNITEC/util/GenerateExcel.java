package com.kranon.reportesUNITEC.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;


public class GenerateExcel {
	
	@SuppressWarnings("resource")
	public static void writeWithColumnAsociationCSV(String path, HashMap<String, Object> header,List<HashMap<String, Object>> content) {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Java book");
		 
		int rowCount = -1;
		HSSFRow row = sheet.createRow(++rowCount);
		for (Entry<String, Object> entry : header.entrySet()) {
			String value = entry.getKey();
			Object index = entry.getValue();
			// Object value=row_o_map.get(key);
			HSSFCell cell = row.createCell((Integer) index);
			if (value instanceof String) {
				cell.setCellValue((String) value);
			}
		}
		for (HashMap<String, Object> row_o_map : content) {
			row = sheet.createRow(++rowCount);
			for (Entry<String, Object> entry : header.entrySet()) {
				String key = entry.getKey();
				Object index = entry.getValue();
				Object value = row_o_map.get(key);
				HSSFCell cell = row.createCell((Integer) index);
				if (value instanceof String) {
					cell.setCellValue((String) value);
				} else if (value instanceof Integer) {
					cell.setCellValue((Integer) value);
				}
			}
		}	
		File file = new File(path);
		try (FileOutputStream outputStream = new FileOutputStream(file)){						
			if (file.exists()) file.delete();
			workbook.write(outputStream);
			outputStream.flush();
			outputStream.close();
			xlsxtoCSV(file);
		} catch (FileNotFoundException e) {
			Log.GuardaLog("[" + new Date() + "][writeWithColumnAsociationCSV][ERROR] ---> " + e.getMessage());
		}catch (IOException e) {
			Log.GuardaLog("[" + new Date() + "][writeWithColumnAsociationCSV][ERROR] ---> " + e.getMessage());
		}
	}
	
	@SuppressWarnings("deprecation")
	private static void xlsxtoCSV(File inputFile) {
		StringBuffer data = new StringBuffer();
		try {
			String path = inputFile.getParent();
			String name = inputFile.getName().replaceFirst("[.][^.]+$", "");
			HSSFWorkbook wBook = new HSSFWorkbook(new FileInputStream(inputFile.getAbsolutePath()));
			// Get first sheet from the workbook
			HSSFSheet sheet = wBook.getSheetAt(0);
			Row row;
			Cell cell;
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				row = rowIterator.next();
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()) {
					cell = cellIterator.next();
					switch (cell.getCellType()) {
					case Cell.CELL_TYPE_BOOLEAN:
						data.append(cell.getBooleanCellValue() + ",");
						break;
					case Cell.CELL_TYPE_NUMERIC:
						data.append(cell.getNumericCellValue() + ",");
						break;
					case Cell.CELL_TYPE_STRING:
						if (!cell.getStringCellValue().equals("N/A"))
							data.append(cell.getStringCellValue() + ",");
						else
							data.append("" + ",");
						break;
					case Cell.CELL_TYPE_BLANK:
						data.append("0" + ",");
						break;
					default:
						data.append(cell + ",");
					}
				}
				data.append("\n");
			}
			inputFile.delete();
			FileOutputStream fos = new FileOutputStream(path + "/" + name + ".csv");
			fos.write(data.toString().getBytes());
			fos.close();
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
	}

}
