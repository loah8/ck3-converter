package org.simple.esta.ck3DnaConverter.constants;

public class DNAConstants {

	// CK3 폴더 내의 파일 파서용
	private static final String TXT = ".txt";
	private static String JS = ".js";

	private static String ABSOLUTE_RESOURCE_PATH = "";
	private static String GENE_JS_PATH = "resources/geneJsResult/";
	private static String FE_RESULT_PATH = "../FE/src/files/";

	private static final String GAME_INSTALL_PATH = "D:\\SteamLibrary\\steamapps\\common\\Crusader Kings III";

	public static String CK_VERSION_FILE = GAME_INSTALL_PATH + "\\launcher\\launcher-settings.json";


	public enum JsonFileInfo {
		// color("00_genes_color", "color_genes"), // 사용하지 않음
		GENE("01_genes_morph", "morph_genes"),
		MISC("02_genes_accessories_misc", "accessory_genes"),

		// special_genes
		HAIRSTYLES("03_genes_special_accessories_hairstyles", "special_genes.accessory_genes"),
		BEARDS("04_genes_special_accessories_beards", "special_genes.accessory_genes"),
		CLOTHES("05_genes_special_accessories_clothes", "special_genes.accessory_genes"),
		HEADGEAR("06_genes_special_accessories_headgear", "special_genes.accessory_genes"),
		PROPS("07_genes_special_accessories_misc", "special_genes.accessory_genes"),
		VISUAL_TRAITS("08_genes_special_visual_traits", "special_genes.morph_genes"),
		SPECIAL_MISC("09_genes_special_misc", "special_genes.morph_genes"),
		;

		private String filaName;
		private String geneDepthPath;

		JsonFileInfo(String filaName, String geneDepthPath) {
			this.filaName = filaName;
			this.geneDepthPath = geneDepthPath;
		}

		public String getFilaName() {
			return filaName;
		}

		public String getGeneDepthPath() {
			return geneDepthPath;
		}

		public String getJsonFile() {
			return filaName + JS;
		}

		public String getTxtFile() {
			return filaName + TXT;
		}

		public String getFullTxtPath() {
			String CK_GENE_FILE_PATH = GAME_INSTALL_PATH + "\\game\\common\\genes\\";
			return CK_GENE_FILE_PATH + getTxtFile();
		}

		public String getAbsoluteJsonPath() {
			return ABSOLUTE_RESOURCE_PATH + GENE_JS_PATH + getJsonFile();
		}
	}



	// FE 프로젝트에 파일 카피뜰 때 사용
	public enum FeFile {
		INDEX_MAP("indexMap"),
		CHILD_INDEX_MAP("childIndexMap"),
		CHILD_NAME_MAP("childNameMap"),
		INFO("info")            // 웹 info file
		;

		private String name;
		
		FeFile(String name) {
			this.name = name;
		}

		public String getFEPath() {
			return FE_RESULT_PATH + name + JS;
		}

		public String getResourcePath() {
			return ABSOLUTE_RESOURCE_PATH + FE_RESULT_PATH + name + JS;
		}
	}

}
