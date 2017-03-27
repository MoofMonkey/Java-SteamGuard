package com.moofMonkey.steam;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class SteamBase {
	static class _HEX {
		public static String encode(byte[] b) {
			return DatatypeConverter.printHexBinary(b);
		}
	}

	public static class _HMACSHA1 {
		public static byte[] encode(byte[] key, byte[] value) {
			try {
				SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA1");
				Mac mac = Mac.getInstance("HmacSHA1");
				
				mac.init(signingKey);
				
				return mac.doFinal(value);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static class _Base64 {
		public static byte[] FromBase64String(String s) throws Throwable {
			try {
				return DatatypeConverter.parseBase64Binary(s);
			} catch (Exception ex) {
				throw new Throwable("Invalid Base64 string!", ex);
				//return s.getBytes();
			}
		}

		public static String ToBase64String(byte[] b) {
			return DatatypeConverter.printBase64Binary(b);
		}
	}
	
	public static final TimeCorrector tc = TimeCorrector.getInstance();

	public static String encodeUrlUnsafeChars(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '+' || c == '/' || c == '=')
				sb.append(String.format("%%%02x", new Object[] { Integer.valueOf(c) }));
			else
				sb.append(c);
		}
		return sb.toString();
	}
	
	public ArrayList<String> getResponse(String url, String cookies) throws Throwable {
		URLConnection uc = new URL(url).openConnection();
		uc.setRequestProperty("Cookie", cookies);
		uc.setRequestProperty("User-Agent", "Valve");
		uc.setRequestProperty("Accept-Language", "en-US");
		BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
		ArrayList<String> response = new ArrayList<String>();
		String line;

		while ((line = br.readLine()) != null)
			response.add(line);

		return response;
	}

	public static String extractStringValue(String JSON, String value) {
		char[] json = JSON.toCharArray();
		String tmp = "";
		boolean finded = false;

		for (int i = JSON.indexOf(value) + value.length() + 1; i < json.length; i++) {
			char element = json[i];

			if (element == '"')
				if (finded)
					break;
				else {
					finded = true;
					continue;
				}

			if (finded)
				tmp += element;
		}

		return tmp;
	}
	
	public static String extractGSONRaw(String JSON, String value) {
		value = "\"" + value + "\"";
		char[] json = JSON.toCharArray();
		String tmp = "";

		for (int i = JSON.indexOf(value) + value.length() + 1; i < json.length; i++) {
			char element = json[i];

			if (element == ',')
				break;

			tmp += element;
		}

		return tmp;
	}

	public static String extractGSONStringValue(String JSON, String value) {
		String s = extractGSONRaw(JSON, value);
		return s.substring(1, s.length() - 1);
	}
	
	public static long extractGSONLongValue(String JSON, String value) {
		return Long.parseLong(extractGSONStringValue(JSON, value));
	}
	
	public static boolean extractGSONBooleanValue(String JSON, String value) {
		return Boolean.parseBoolean(extractGSONRaw(JSON, value));
	}

	public static BigInteger extractBigIntegerValue(String JSON, String value) {
		return new BigInteger(extractStringValue(JSON, value));
	}
	
	public static boolean extractBooleanValue(String JSON, String value) {
		return Boolean.parseBoolean(extractStringValue(JSON, value));
	}

	public static long extractLongValue(String JSON, String value) {
		return Long.parseLong(extractStringValue(JSON, value));
	}
}