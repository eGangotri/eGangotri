package com.egangotri.filter

import com.egangotri.util.FileUtil

/**
 * Created by user on 2/13/2016.
 */
class NonPre57DirectoryFilter implements FilenameFilter {

    boolean excludePre57

    NonPre57DirectoryFilter() {
        excludePre57 = true
    }

    NonPre57DirectoryFilter(boolean _excludePre57) {
        excludePre57 = _excludePre57
    }

    @Override
    public boolean accept(File dir, String name) {
        if (excludePre57) {
            return dir.isDirectory() && !name.toLowerCase().equals(FileUtil.PRE_57)
        } else {
            return dir.isDirectory() && name.toLowerCase().equals(FileUtil.PRE_57)
        }
    }
}
