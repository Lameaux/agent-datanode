package com.euromoby.agent.datanode.core.utils;

public class Strings {

	public static final String CRLF = "\r\n";

	public static boolean nullOrEmpty(String value) {
		return value == null || value.trim().isEmpty();
	}

	public static String emptyStringIfNull(String value) {
		if (value == null) {
			return "";
		}
		return value;
	}

	public static String trimIfNotEmpty(String value) {
		if (nullOrEmpty(value)) {
			return value;
		}
		return value.trim();
	}

}
