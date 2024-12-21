package com.api.billmanager.infrastructure.utils;

import org.springframework.web.multipart.MultipartFile;

public class CsvUtils {

    public static final String TYPE = "text/csv";

    public static boolean hasCsvFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }
}