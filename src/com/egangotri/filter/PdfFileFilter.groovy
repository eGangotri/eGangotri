package com.egangotri.filter

/**
 * Created by user on 2/13/2016.
 */
class PdfFileFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(".pdf");
    }
}