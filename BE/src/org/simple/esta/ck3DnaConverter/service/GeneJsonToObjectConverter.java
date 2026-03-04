package org.simple.esta.ck3DnaConverter.service;


import org.simple.esta.ck3DnaConverter.constants.DNAConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.simple.esta.ck3DnaConverter.constants.DNAConstants.FeFile.*;


public class GeneJsonToObjectConverter {

	private static Map<String, Map<String, String>> childIndexMap = new HashMap<>();
	private static Map<String, Map<String, String>> childNameMap = new HashMap<>();

	private final CustomJsonParser mapper;
	private final CustomFileWriter customFileWriter;

	public GeneJsonToObjectConverter(CustomJsonParser mapper, CustomFileWriter customFileWriter) {
		this.mapper = mapper;
		this.customFileWriter = customFileWriter;
	}

	public void createFEMapFiles() {
		try {
			Arrays.stream(DNAConstants.JsonFileInfo.values()).distinct().forEach(this::convert);
			this.createFEMapFile();
		} catch (Exception e) {
			System.err.println("Error creating FE map files");
			e.printStackTrace();
		}
	}


	private void convert(DNAConstants.JsonFileInfo jsonFileInfo) {
		try {
			System.out.println("JS File is Converting to Map: " + jsonFileInfo.getFilaName());

			File jsFile = new File(jsonFileInfo.getAbsoluteJsonPath());
			String content = Files.readString(jsFile.toPath());
			
			// Extract JSON from "export const variable = { ... };"
			int firstBrace = content.indexOf("{");
			int lastSemiColon = content.lastIndexOf(";");
			if (firstBrace == -1 || lastSemiColon == -1 || firstBrace >= lastSemiColon) {
				System.err.println("Invalid JS file format: " + jsonFileInfo.getFilaName());
				return;
			}
			String jsonContent = content.substring(firstBrace, lastSemiColon).trim();

			LinkedHashMap<String, Object> map = mapper.readValue(jsonContent, LinkedHashMap.class);

			String rootName = jsonFileInfo.getGeneDepthPath();
			if (!rootName.contains(".")) {
				this.convert3DepthMaps((LinkedHashMap<String, Object>) map.get(jsonFileInfo.getGeneDepthPath()), jsonFileInfo);
				return;
			}

			// Depth 만큼 파고 들어서 진행한다.
			String[] depth = rootName.split("\\.");

			LinkedHashMap<String, Object> tempMap = map;
			for (int i = 0; i < depth.length; i++) {
				tempMap = (LinkedHashMap<String, Object>) tempMap.get(depth[i]);
			}

			this.convert3DepthMaps(tempMap, jsonFileInfo);
		} catch (IOException e) {
			System.err.println("Error converting JS to object: " + jsonFileInfo.getFilaName());
			e.printStackTrace();
		}
	}

/*
"gene_chin_forward": {
	"group": "face",
	"chin_forward_neg": {
		"index": "0",
				"visible": "no",
				"male": {},
		"female": {},
		"boy": {},
		"girl": {}
	},
	"chin_forward_pos": {
		"index": "1",
				"male": {},
		"female": {},
		"boy": {},
		"girl": {}
	}
},
.....
.....
*/
	private void convert3DepthMaps(LinkedHashMap<String, Object> ancestorMap, DNAConstants.JsonFileInfo jsonFileInfo) {
		if (ancestorMap == null) {
			return;
		}
		for (Map.Entry<String, Object> entry : ancestorMap.entrySet()) {
			// values
			LinkedHashMap<String, Object> parentMap = (LinkedHashMap<String, Object>) entry.getValue();

			// Index 넣기
			String key = entry.getKey();

			Map<String, String> resultIndexMap = new HashMap<>();
			for (Map.Entry<String, Object> childEntry : parentMap.entrySet()) {
				this.setChildIndexMap(childEntry.getKey(), childEntry, resultIndexMap);
			}
			childIndexMap.put(key, resultIndexMap);

			Map<String, String> resultNameMap = new HashMap<>();
			for (Map.Entry<String, Object> childEntry : parentMap.entrySet()) {
				this.setChildNameMap(childEntry.getKey(), childEntry, resultNameMap);
			}
			childNameMap.put(key, resultNameMap);
		}

	}

	private void setChildIndexMap(String keyName, Map.Entry<String, Object> childEntry, Map<String, String> resultMap) {
		if (!(childEntry.getValue() instanceof Map)) {
			return;
		}
		HashMap<String, Object> map = (HashMap<String, Object>) childEntry.getValue();
		resultMap.put((String) map.get("index"), keyName);
	}

	private void setChildNameMap(String keyName, Map.Entry<String, Object> childEntry, Map<String, String> resultMap) {
		if (!(childEntry.getValue() instanceof Map)) {
			return;
		}
		HashMap<String, Object> map = (HashMap<String, Object>) childEntry.getValue();
		resultMap.put(keyName, (String) map.get("index"));
	}


	private void createFEMapFile() throws IOException {
		customFileWriter.jsonFileWrite(CHILD_INDEX_MAP.getResourcePath(), childIndexMap);
		customFileWriter.jsonFileWrite(CHILD_NAME_MAP.getResourcePath(), childNameMap);

		Files.copy(Path.of(INDEX_MAP.getResourcePath()), Path.of(INDEX_MAP.getFEPath()), StandardCopyOption.REPLACE_EXISTING);
		Files.copy(Path.of(CHILD_INDEX_MAP.getResourcePath()), Path.of(CHILD_INDEX_MAP.getFEPath()), StandardCopyOption.REPLACE_EXISTING);
		Files.copy(Path.of(CHILD_NAME_MAP.getResourcePath()), Path.of(CHILD_NAME_MAP.getFEPath()), StandardCopyOption.REPLACE_EXISTING);
	}

}
