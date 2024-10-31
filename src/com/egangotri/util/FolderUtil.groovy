package com.egangotri.util

    import java.nio.file.Files
    import java.nio.file.Path
    import java.nio.file.Paths
    import java.nio.file.DirectoryStream
    import java.nio.file.SimpleFileVisitor
    import java.nio.file.FileVisitResult
    import java.nio.file.Files
    import java.nio.file.attribute.BasicFileAttributes
    import java.nio.file.FileVisitOption

    import java.util.EnumSet
    import java.util.List
class FolderUtil {

    static class FolderLister extends SimpleFileVisitor<Path> {
        List<Path> folders = []

        @Override
        FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            folders << dir // Add the current directory to the list
            return FileVisitResult.CONTINUE
        }
    }

    static List<Path> listAllSubfolders(String absolutePath) {
        Path startPath = Paths.get(absolutePath)
        FolderLister folderLister = new FolderLister()

        // Use Files.walkFileTree to traverse the directory tree
        Files.walkFileTree(startPath, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, folderLister)

        return folderLister.folders
    }



}
