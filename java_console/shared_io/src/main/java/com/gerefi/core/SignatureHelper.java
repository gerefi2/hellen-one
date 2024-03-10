package com.gerefi.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import static com.gerefi.core.FileUtil.gerefi_SETTINGS_FOLDER;

public class SignatureHelper {
    private final static String LOCAL_INI = gerefi_SETTINGS_FOLDER + File.separator + "ini_database";

    // todo: find a way to reference Fields.PROTOCOL_SIGNATURE_PREFIX
    private static final String PREFIX = "gerefi ";
    private static final char SLASH = '/';

    public static Pair<String, String> getUrl(String signature) {
        gerefiSignature s = parse(signature);
        if (s == null)
            return null;

        String fileName = s.getHash() + ".ini";
        return new Pair("https://gerefi.com/online/ini/gerefi/" + s.getBranch() + SLASH + s.getYear() + SLASH +
                s.getMonth() + SLASH +
                s.getDay() + SLASH +
                s.getBundleTarget() + SLASH +
                fileName, fileName);
    }

    public static String downloadIfNotAvailable(Pair<String, String> p) {
        if (p == null)
            return null;
        new File(LOCAL_INI).mkdirs();
        String localIniFile = LOCAL_INI + File.separator + p.second;
        File file = new File(localIniFile);
        if (file.exists() && file.length() > 10000)
            return localIniFile;
        try (BufferedInputStream in = new BufferedInputStream(new URL(p.first).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(localIniFile)) {
            byte[] dataBuffer = new byte[32 * 1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, dataBuffer.length)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            return localIniFile;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public static gerefiSignature parse(String signature) {
        if (signature == null || !signature.startsWith(PREFIX))
            return null;
        signature = signature.substring(PREFIX.length()).trim();
        String[] elements = signature.split("\\.");
        if (elements.length != 6)
            return null;

        String branch = elements[0];
        String year = elements[1];
        String month = elements[2];
        String day = elements[3];
        String bundleTarget = elements[4];
        String hash = elements[5];

        return new gerefiSignature(branch, year, month, day, bundleTarget, hash);
    }
}
