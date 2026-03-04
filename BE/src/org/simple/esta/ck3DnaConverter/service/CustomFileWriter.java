package org.simple.esta.ck3DnaConverter.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;

public class CustomFileWriter {

	private final CustomJsonParser mapper;

	public CustomFileWriter(CustomJsonParser mapper) {
		this.mapper = mapper;
	}

	public void jsonFileWrite(String filePath, Map<?, ?> content) {
		try {
			File file = new File(filePath);
			file.getParentFile().mkdirs();  // 부모 path 없으면 생성
			file.createNewFile();   // 파일 없으면 만듦

			System.out.println("File " + file.getName() + " is writing");

			String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
			String variableName = fileName.replaceFirst("^\\d+_", "");

			BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
			
			// CustomJsonParser를 사용하여 JSON 문자열 변환
			String jsonContent = mapper.writeValueAsString(content);
			
			writer.write("export const " + variableName + " = ");
			writer.write(jsonContent);
			writer.write(";");
			
			writer.flush();
			writer.close();
		} catch (Exception e) {
			System.err.println("File write error: " + filePath);
			e.printStackTrace();
		}
	}
}
