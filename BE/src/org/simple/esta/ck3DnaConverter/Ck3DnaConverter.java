package org.simple.esta.ck3DnaConverter;


import org.simple.esta.ck3DnaConverter.service.*;

public class Ck3DnaConverter {

	public static void main(String[] args) {
		CustomJsonParser mapper = new CustomJsonParser();
		CustomFileWriter customFileWriter = new CustomFileWriter(mapper);
		
		RawFileToJsonConverter rawFileToJsonConverter = new RawFileToJsonConverter(mapper, customFileWriter);
		GeneOrderService geneOrderService = new GeneOrderService(customFileWriter);
		GeneJsonToObjectConverter geneJsonToObjectConverter = new GeneJsonToObjectConverter(mapper, customFileWriter);

		try {
			// 1. Convert raw game files to JS
			rawFileToJsonConverter.convertAllJson();
			
			// 2. Set version info
			rawFileToJsonConverter.setVersions();
			
			// 3. Create gene order map
			geneOrderService.createGeneOrderMap();
			
			// 4. Create FE maps from converted JS files
			geneJsonToObjectConverter.createFEMapFiles();
			
			System.out.println("Conversion completed successfully!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
