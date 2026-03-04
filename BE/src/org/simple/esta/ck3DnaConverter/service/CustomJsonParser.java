package org.simple.esta.ck3DnaConverter.service;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class CustomJsonParser {

	/**
	 * Map 객체를 JSON 문자열로 변환 (CustomFileWriter에서 사용)
	 */
	public String writeValueAsString(Map<?, ?> map) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		Iterator<? extends Map.Entry<?, ?>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<?, ?> entry = it.next();
			sb.append("\"").append(entry.getKey()).append("\":");
			appendValue(sb, entry.getValue());
			if (it.hasNext()) {
				sb.append(",");
			}
		}
		sb.append("}");
		return sb.toString();
	}

	private void appendValue(StringBuilder sb, Object value) {
		if (value == null) {
			sb.append("null");
		} else if (value instanceof Map) {
			sb.append(writeValueAsString((Map<?, ?>) value));
		} else if (value instanceof List) {
			sb.append("[");
			List<?> list = (List<?>) value;
			for (int i = 0; i < list.size(); i++) {
				appendValue(sb, list.get(i));
				if (i < list.size() - 1) sb.append(",");
			}
			sb.append("]");
		} else if (value instanceof Number || value instanceof Boolean) {
			sb.append(value);
		} else {
			sb.append("\"").append(value.toString().replace("\"", "\\\"")).append("\"");
		}
	}

	/**
	 * JSON 문자열을 LinkedHashMap으로 변환 (RawFileToJsonConverter, GeneJsonToObjectConverter에서 사용)
	 * 간단한 형태의 JSON만 지원하도록 구현 (중첩된 객체 포함)
	 */
	public LinkedHashMap<String, Object> readValue(String json, Class<?> clazz) {
		json = json.trim();
		if (json.startsWith("{")) {
			return parseObject(json);
		}
		return new LinkedHashMap<>();
	}

	public LinkedHashMap<String, Object> readValue(File file, Class<?> clazz) {
		try {
			String content = Files.readString(file.toPath());
			return readValue(content, clazz);
		} catch (Exception e) {
			e.printStackTrace();
			return new LinkedHashMap<>();
		}
	}

	private LinkedHashMap<String, Object> parseObject(String json) {
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		json = json.trim();
		if (!json.startsWith("{")) return map;
		
		int lastBrace = json.lastIndexOf("}");
		if (lastBrace <= 0) return map; // {} or invalid
		
		// Remove outer braces
		String content = json.substring(1, lastBrace).trim();
		if (content.isEmpty()) return map;

		int pos = 0;
		while (pos < content.length()) {
			// Find key
			int startQuote = content.indexOf("\"", pos);
			if (startQuote == -1) break;
			int endQuote = content.indexOf("\"", startQuote + 1);
			if (endQuote == -1) break;
			String key = content.substring(startQuote + 1, endQuote).trim();

			// Find colon
			int colon = content.indexOf(":", endQuote + 1);
			if (colon == -1) break;
			pos = colon + 1;

			// Skip whitespace
			while (pos < content.length() && Character.isWhitespace(content.charAt(pos))) {
				pos++;
			}

			// Find value
			Object value;
			if (pos >= content.length()) {
				break;
			}
			if (content.charAt(pos) == '{') {
				int endBrace = findClosingBracket(content, pos, '{', '}');
				if (endBrace >= content.length()) {
					endBrace = content.length() - 1;
				}
				value = parseObject(content.substring(pos, endBrace + 1));
				pos = endBrace + 1;
			} else if (content.charAt(pos) == '[') {
				int endBracket = findClosingBracket(content, pos, '[', ']');
				if (endBracket >= content.length()) {
					endBracket = content.length() - 1;
				}
				value = parseArray(content.substring(pos, endBracket + 1));
				pos = endBracket + 1;
			} else if (content.charAt(pos) == '"') {
				int nextQuote = content.indexOf("\"", pos + 1);
				if (nextQuote == -1) {
					value = content.substring(pos + 1).trim();
					pos = content.length();
				} else {
					value = content.substring(pos + 1, nextQuote).trim();
					pos = nextQuote + 1;
				}
			} else {
				int comma = content.indexOf(",", pos);
				if (comma == -1) comma = content.length();
				String valStr = content.substring(pos, comma).trim();
				if (valStr.equals("true")) value = true;
				else if (valStr.equals("false")) value = false;
				else if (valStr.equals("null")) value = null;
				else {
					try {
						if (valStr.contains(".")) {
							value = Double.parseDouble(valStr);
						} else {
							value = Integer.parseInt(valStr);
						}
					} catch (NumberFormatException e) {
						value = valStr;
					}
				}
				pos = comma;
			}
			map.put(key, value);

			// Skip comma
			int nextComma = content.indexOf(",", pos);
			if (nextComma != -1 && (nextComma < content.indexOf("\"", pos) || content.indexOf("\"", pos) == -1)) {
				pos = nextComma + 1;
			} else {
				// Search for next key
				int nextKey = content.indexOf("\"", pos);
				if (nextKey == -1) break;
				pos = nextKey;
			}
		}

		return map;
	}

	private List<Object> parseArray(String json) {
		List<Object> list = new ArrayList<>();
		json = json.trim();
		if (!json.startsWith("[")) return list;

		int lastBracket = json.lastIndexOf("]");
		if (lastBracket <= 0) return list;

		String content = json.substring(1, lastBracket).trim();
		if (content.isEmpty()) return list;

		int pos = 0;
		while (pos < content.length()) {
			// Skip whitespace
			while (pos < content.length() && Character.isWhitespace(content.charAt(pos))) {
				pos++;
			}
			if (pos >= content.length()) break;

			Object value;
			if (content.charAt(pos) == '{') {
				int endBrace = findClosingBracket(content, pos, '{', '}');
				value = parseObject(content.substring(pos, Math.min(endBrace + 1, content.length())));
				pos = endBrace + 1;
			} else if (content.charAt(pos) == '[') {
				int endBracket = findClosingBracket(content, pos, '[', ']');
				value = parseArray(content.substring(pos, Math.min(endBracket + 1, content.length())));
				pos = endBracket + 1;
			} else if (content.charAt(pos) == '"') {
				int nextQuote = content.indexOf("\"", pos + 1);
				if (nextQuote == -1) {
					value = content.substring(pos + 1).trim();
					pos = content.length();
				} else {
					value = content.substring(pos + 1, nextQuote).trim();
					pos = nextQuote + 1;
				}
			} else {
				int comma = content.indexOf(",", pos);
				if (comma == -1) comma = content.length();
				String valStr = content.substring(pos, comma).trim();
				if (valStr.equals("true")) value = true;
				else if (valStr.equals("false")) value = false;
				else if (valStr.equals("null")) value = null;
				else {
					try {
						if (valStr.contains(".")) value = Double.parseDouble(valStr);
						else value = Integer.parseInt(valStr);
					} catch (NumberFormatException e) {
						value = valStr;
					}
				}
				pos = comma;
			}
			list.add(value);

			int nextComma = content.indexOf(",", pos);
			if (nextComma != -1) {
				pos = nextComma + 1;
			} else {
				break;
			}
		}
		return list;
	}

	private int findClosingBracket(String text, int openPos, char openChar, char closeChar) {
		int closePos = openPos;
		int counter = 1;
		boolean inQuotes = false;
		while (counter > 0 && ++closePos < text.length()) {
			char c = text.charAt(closePos);
			if (c == '"' && text.charAt(closePos - 1) != '\\') {
				inQuotes = !inQuotes;
			}
			if (!inQuotes) {
				if (c == openChar) counter++;
				else if (c == closeChar) counter--;
			}
		}
		return closePos;
	}
}
