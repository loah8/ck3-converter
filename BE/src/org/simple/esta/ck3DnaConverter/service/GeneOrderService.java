package org.simple.esta.ck3DnaConverter.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.simple.esta.ck3DnaConverter.constants.DNAConstants.FeFile.INDEX_MAP;


public class GeneOrderService {

	private static final String KEY_NAME = "keyName";
	private static final String regex = "(?<" + KEY_NAME + ">.*)=";    // regex : (?<keyName>.*)=
	private static final Pattern pattern = Pattern.compile(regex);
	private static final String FILE_PATH = "resources/00_needFile/genes.txt";
	private final int MXA_LOOP = 1000;

	private final CustomFileWriter customFileWriter;

	public GeneOrderService(CustomFileWriter customFileWriter) {
		this.customFileWriter = customFileWriter;
	}

	private Map<Integer, String> getOrderedGenes() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH));

		int i = 0;
		String currentLine = reader.readLine();

		Map<Integer, String> indexAndKeyMap = new HashMap<>();

		while (currentLine != null && !currentLine.isEmpty() && i < MXA_LOOP) {
			Matcher matcher = pattern.matcher(currentLine);
			if (matcher.find()) {
				String keyName = matcher.group(KEY_NAME).trim();

				System.out.println("index: " + i + ", key: " + keyName);
				indexAndKeyMap.put(i, keyName);

				i++;
			}
			currentLine = reader.readLine();
		}

		System.out.println("result : " + indexAndKeyMap);
		reader.close();

		return indexAndKeyMap;
	}

	public void createGeneOrderMap() throws IOException {
		customFileWriter.jsonFileWrite(INDEX_MAP.getResourcePath(), this.getOrderedGenes());
		Files.copy(Path.of(INDEX_MAP.getResourcePath()), Path.of(INDEX_MAP.getFEPath()), StandardCopyOption.REPLACE_EXISTING);
	}

}
