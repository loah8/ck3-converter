package org.simple.esta.ck3DnaConverter.service;


import org.simple.esta.ck3DnaConverter.constants.DNAConstants;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.simple.esta.ck3DnaConverter.constants.DNAConstants.CK_VERSION_FILE;
import static org.simple.esta.ck3DnaConverter.constants.DNAConstants.FeFile.INFO;


public class RawFileToJsonConverter {

	private static final String openObject = "{";
	private static final String closeObject = "}";
	private static final String parseToken = "=";

	private final CustomJsonParser mapper;
	private final CustomFileWriter customFileWriter;

	public RawFileToJsonConverter(CustomJsonParser mapper, CustomFileWriter customFileWriter) {
		this.mapper = mapper;
		this.customFileWriter = customFileWriter;
	}

	public void convertAllJson() {
		Arrays.stream(DNAConstants.JsonFileInfo.values()).distinct().forEach(this::convertJson);
	}

	public void setVersions() {
		this.setVersion();
	}

	private void setVersion() {
		try {
			String rawVersion = "rawVersion";
			String lastUpdate = "lastUpdate";

			LinkedHashMap<String, Object> versionMap = mapper.readValue(new File(CK_VERSION_FILE), LinkedHashMap.class);
			String version = (String) versionMap.get(rawVersion);
			ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());

			Map<String, String> resultMap = Map.of(
					rawVersion, version,
					lastUpdate, now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
			);

			customFileWriter.jsonFileWrite(INFO.getResourcePath(), resultMap);
			Files.copy(Path.of(INFO.getResourcePath()), Path.of(INFO.getFEPath()), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.err.println("Version setting error");
			e.printStackTrace();
		}
	}

	private void convertJson(DNAConstants.JsonFileInfo jsonFileInfo) {
		try {
			File resultFile = new File(jsonFileInfo.getAbsoluteJsonPath());
			resultFile.getParentFile().mkdirs();

			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(jsonFileInfo.getFullTxtPath())));
			BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile.getAbsolutePath(), false));

			String fileName = resultFile.getName().substring(0, resultFile.getName().lastIndexOf("."));
			String variableName = fileName.replaceFirst("^\\d+_", "");

			String line;    // line 단위로 읽어온다.
			writer.write("export const " + variableName + " = ");
			writer.write(openObject);
			String lastLine = null;
			while ((line = reader.readLine()) != null) {
				String trimmedLine = line.trim();
				if (trimmedLine.isEmpty() || isCommentedString(trimmedLine) || isOneLineData(trimmedLine) || isWhiteButNotChecked(trimmedLine)) {
					continue;
				}
				String resultLine = this.resultLine(trimmedLine);
				if (resultLine != null) {
					if (lastLine != null) {
						writer.write(lastLine);
					}
					lastLine = resultLine;
				}
				writer.flush();
			}
			if (lastLine != null) {
				// 마지막 라인에서 콤마 제거
				if (lastLine.endsWith(",")) {
					lastLine = lastLine.substring(0, lastLine.length() - 1);
				}
				writer.write(lastLine);
			}
			writer.write(closeObject);
			writer.write(";");
			reader.close();
			writer.close();
		} catch (IOException e) {
			System.err.println("File convert error: " + jsonFileInfo.getFilaName());
			e.printStackTrace();
		}
	}

	private String resultLine(final String line) {
		String thisString = parsedCommentString(line);

		if (this.isCloseLine(thisString)) {
			return "},";
		}
		if (!thisString.contains(parseToken)) {
			return null;
		}
		String[] data = thisString.split(parseToken);
		StringBuffer sb = new StringBuffer();
		sb.append('"');
		sb.append(data[0].trim());
		sb.append('"');
		sb.append(':');
		if (data[1].contains("{")) {
			sb.append(data[1].trim());
			return sb.toString();
		}
		if (data[1].contains("\"")) {
			sb.append(data[1].trim());
			sb.append(",");
			return sb.toString();
		}
		sb.append('"');
		sb.append(data[1].trim());
		sb.append('"');
		sb.append(",");
		return sb.toString();
	}

	// 주석인지 체크
	private boolean isCommentedString(String line) {
		return line.startsWith("@") || line.startsWith("#") || line.startsWith("\uFEFF");
	}

	// 한줄짜리 데이터는 필요없음
	private boolean isOneLineData(String line) {
		return line.contains(openObject) && line.contains(closeObject);
	}

	// 객체가 닫히는 라인인지 체크
	private boolean isCloseLine(String line) {
		return line.contains(closeObject);
	}

	// 눈에 안 보이는 공백인지 체크
	private boolean isWhiteButNotChecked(String line) {
		if (line.length() > 1) {
			return false;
		}
		return (line.getBytes()[0] == -17);
	}

	// 주석이랑 혼용된 경우, 주석 이전만 리턴한다.
	private String parsedCommentString(String line) {
		if (!line.contains("#")) {
			return line;
		}
		String[] data = line.split("#");
		return data[0];
	}

}
