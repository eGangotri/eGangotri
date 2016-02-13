package com.egangotri.filter

/**
 * Created by user on 2/13/2016.
 */
class DirectoryFileFilter implements FilenameFilter{
    @Override
    public boolean accept(File dir, String name) {
        return dir.isDirectory();
    }
}
