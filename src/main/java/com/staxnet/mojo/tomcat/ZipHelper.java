package com.staxnet.mojo.tomcat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.staxnet.utils.FileHelper;

public class ZipHelper {

    /**
     * Inserts a file into a zipstream using the specified entryName.
     * 
     * @param file
     * @param entryName
     * @param zos
     * @throws IOException
     */
    public static final void addFileToZip(File file, String entryName,
            ZipOutputStream zos) throws IOException {
        byte[] buffer = new byte[8192];
        int read = 0;
        FileInputStream in = new FileInputStream(file);
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        while (-1 != (read = in.read(buffer))) {
            zos.write(buffer, 0, read);
        }
        in.close();
    }

    /**
     * Inserts a stream into a zipstream using the specified entryName.
     * 
     * @param in
     * @param entryName
     * @param zos
     * @throws IOException
     */
    public static final void addFileToZip(InputStream in, String entryName,
            ZipOutputStream zos) throws IOException {
        byte[] buffer = new byte[8192];
        int read = 0;
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        while (-1 != (read = in.read(buffer))) {
            zos.write(buffer, 0, read);
        }
        in.close();
    }

    /**
     * Recursively inserts all files in a directory into a zipstream.
     * 
     * @param directory
     *            the source directory
     * @param base
     *            optional parent directory that should serve as the entry root.
     *            Only path info after this base will be included as part of the
     *            entry name. By default, the directory parameter serves as
     *            root.
     * @param dirPrefix
     *            optional directory prefix to prepend onto each entry name.
     * @param zos
     *            the zip stream to add the files to.
     * @throws IOException
     */
    public static final void addDirectoryToZip(File directory, File base,
            String dirPrefix, ZipOutputStream zos) throws IOException {
        if (base == null)
            base = directory;
        if (dirPrefix == null)
            dirPrefix = "";
        
        //add an entry for the directory itself
        if(!base.equals(directory) && directory.list().length == 0)
        {
            String dirEntryPath = dirPrefix
            + directory.getPath().substring(
                    base.getPath().length() + 1).replace('\\',
                    '/');
            ZipEntry dirEntry = new ZipEntry(dirEntryPath.endsWith("/") ? dirEntryPath : dirEntryPath + "/");
            zos.putNextEntry(dirEntry);
        }
        
        File[] files = directory.listFiles();
        byte[] buffer = new byte[8192];
        int read = 0;
        for (int i = 0, n = files.length; i < n; i++) {
            if (!files[i].isHidden()) {
                if (files[i].isDirectory()) {
                    addDirectoryToZip(files[i], base, dirPrefix, zos);
                } else {
                    FileInputStream in = new FileInputStream(files[i]);
                    ZipEntry entry = new ZipEntry(dirPrefix
                            + files[i].getPath().substring(
                                    base.getPath().length() + 1).replace('\\',
                                    '/'));
                    zos.putNextEntry(entry);
                    while (-1 != (read = in.read(buffer))) {
                        zos.write(buffer, 0, read);
                    }
                    in.close();
                }
            }
        }
    }
    
    public static InputStream getZipEntry(InputStream zipFile, final String entryName) throws IOException
    {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        unzipFile(zipFile, new ZipEntryHandler()
        {
            private boolean unzipped = false;
            public void unzip(ZipEntry entry, InputStream zis)
                    throws IOException {
                if(!unzipped && entry.getName().equals(entryName))
                {
                    StreamHelper.transferStream(zis, bout);
                    unzipped = true;
                }
            }    
        }, false);
        
        return new ByteArrayInputStream(bout.toByteArray());
    }

    public static void unzipFile(InputStream fis, ZipEntryHandler zipHandler,
            boolean closeStream) throws IOException {
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            zipHandler.unzip(entry, StreamHelper.createNoCloseInputStream(zis));
        }
        if (closeStream)
            zis.close();
    }
    
    public static void unzipFile(InputStream fis, final File targetDir, boolean closeStream) throws IOException
    {
        ZipHelper.unzipFile(fis, new UnzipToDirEntryHandler(targetDir), closeStream);
    }

    public interface ZipEntryHandler {
        public void unzip(ZipEntry entry, InputStream zis)
                throws IOException;
    }
    
    private static class UnzipToDirEntryHandler implements ZipEntryHandler
    {
        private File targetDir;
        public UnzipToDirEntryHandler(File targetDir)
        {
            this.targetDir = targetDir;
        }
        public void unzip(ZipEntry entry, InputStream zis) throws IOException {
            unzipEntryToFolder(entry, zis, targetDir);
            /*File entryFile = new File(targetDir, entry.getName().replace('/', File.separatorChar));
            if(entry.isDirectory())
                entryFile.mkdirs();
            else
            {
                File parentFolder = entryFile.getParentFile();
                if(!parentFolder.exists())
                    parentFolder.mkdirs();
                unzipEntryToFolder(entry, zis, parentFolder);
            }*/
        }
    }
    
    private static int BUFFER = 2048;
    public static File unzipEntryToFolder(ZipEntry entry, InputStream zis,
            File destFolder) throws FileNotFoundException, IOException {
        BufferedOutputStream dest;
        if(entry.isDirectory())
        {
            File destFile = new File(destFolder, entry.getName());
            destFile.mkdirs();
            return destFile;
        }
        else
        {
            int count;
            byte data[] = new byte[BUFFER];
            // write the files to the disk
            File destFile = new File(destFolder, entry.getName());
            File parentFolder = destFile.getParentFile(); 
            if(!parentFolder.exists())
                parentFolder.mkdirs();
            FileOutputStream fos = new FileOutputStream(destFile);
            dest = new BufferedOutputStream(fos, BUFFER);
            while ((count = zis.read(data, 0, BUFFER)) != -1) {
                dest.write(data, 0, count);
            }
            dest.flush();
            dest.close();
            
            return destFile;
        }
    }
}
