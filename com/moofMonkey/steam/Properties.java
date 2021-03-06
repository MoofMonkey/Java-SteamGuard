package com.moofMonkey.steam;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Properties extends SteamBase {
	public String identity_secret, shared_secret, machineName, username, password, revocation_code, browser_cookies;
	public long steamid64;
	private File settings;
	
	public Properties ( // For creating
			String _identity_secret,
			String _shared_secret,
			String _machineName,
			String _username,
			String _password,
			String _revocation_code,
			File _settings
	) throws Throwable {
		this (
			_identity_secret,
			_shared_secret,
			_machineName,
			_username,
			_password,
			0,
			_revocation_code,
			"",
			_settings
		);
	}
	
	public Properties ( // For importing
			String _identity_secret,
			String _shared_secret,
			String _machineName,
			String _username,
			String _password,
			long _steamid64,
			String _revocation_code,
			String _browser_cookies,
			File _settings
	) throws Throwable {
		identity_secret = _identity_secret;
		shared_secret = _shared_secret;
		machineName = _machineName;
		username = _username;
		password = _password;
		steamid64 = _steamid64;
		browser_cookies = _browser_cookies;
		revocation_code = _revocation_code;
		settings = _settings;
	}
	
	public static Properties getProps(File settings) throws Throwable {
		String identity_secret = "", shared_secret = "", machineName = "", userName = "", password = "", browser_cookies = "", revocation_code = "";
		long steamid64 = 0;
		BufferedReader in;

		Properties props = null;
		if (!settings.exists()) {
			in = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Please write your username");
			userName = in.readLine();
			System.out.println("Please write your password");
			password = in.readLine();
			System.out.println("Please write your identity_secret");
			identity_secret = in.readLine();
			System.out.println("Please write your shared_secret");
			shared_secret = in.readLine();
			System.out.println("Please write your revocation code");
			revocation_code = in.readLine();
			System.out.println("Please write your machineName (optional, you can just press ENTER)");
			machineName = in.readLine();
			
			if(machineName.length() == 0) {
				SecureRandom secRand = new SecureRandom();
				byte[] b = new byte[256];
				secRand.nextBytes(b);
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				machineName = "android:" + _HEX.encode(md.digest(b)).toLowerCase();
			}
			in.close();
			
			props = new Properties(identity_secret, shared_secret, machineName, userName, password, revocation_code, settings);
			Object[] data = SteamCookies.getData(props);
			props.browser_cookies = (String) data[0];
			props.steamid64 = extractGSONLongValue((String) data[1], "steamid");
			props.settings = settings;
			props.saveProps();
		} else {
			in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(settings))));
			String json = in.readLine();
			in.close();
			json = new String(_Base64.FromBase64String(json));
			identity_secret = extractStringValue(json, "identify_secret");
			shared_secret = extractStringValue(json, "shared_secret");
			machineName = extractStringValue(json, "machineName");
			userName = extractStringValue(json, "username");
			password = extractStringValue(json, "password");
			steamid64 = extractLongValue(json, "steamid64");
			revocation_code = extractStringValue(json, "revocation_code");
			browser_cookies = extractStringValue(json, "browser_cookies");
		}

		return
				props == null
					? new Properties(identity_secret, shared_secret, machineName, userName, password, steamid64, revocation_code, browser_cookies, settings)
					: props;
	}

	public void saveProps() throws Throwable {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(settings))));
		bw.write(_Base64.ToBase64String(toString().getBytes()));
		bw.flush();
		bw.close();
	}

	@Override
	public String toString() {
		return    "{"
				+ "identify_secret=\"" + identity_secret + "\", "
				+ "shared_secret=\"" + shared_secret + "\", "
				+ "machineName=\"" + machineName + "\", "
				+ "username=\"" + username + "\", "
				+ "password=\"" + password + "\", "
				+ "steamid64=\"" + steamid64 + "\", "
				+ "browser_cookies=\"" + browser_cookies + "\", "
				+ "revocation_code=\"" + revocation_code + "\", "
				+ "}";
	}
}
