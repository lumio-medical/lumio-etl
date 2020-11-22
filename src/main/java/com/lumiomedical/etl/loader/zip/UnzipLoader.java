package com.lumiomedical.etl.loader.zip;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.etl.loader.Loader;
import com.lumiomedical.flow.etl.loader.LoadingException;
import com.noleme.commons.stream.Streams;
import com.noleme.commons.stream.Streams.Policy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/03/30
 */
public class UnzipLoader implements Loader<InputStream>
{
    private final String destinationDir;

    /**
     *
     * @param destinationDir
     */
    public UnzipLoader(String destinationDir)
    {
        this.destinationDir = destinationDir;
    }

    @Override
    public void load(InputStream input) throws LoadingException
    {
        ZipInputStream zis = null;
        try {
            Logging.logger.info("Unzipping input into " + this.destinationDir);

            zis = new ZipInputStream(input);
            File dest = new File(this.destinationDir);

            if (!dest.mkdirs())
                throw new LoadingException("The requested destination directory " + this.destinationDir + " could not be created.");

            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null)
            {
                File newFile = newFile(dest, zipEntry);
                Streams.flow(zis, new FileOutputStream(newFile), Policy.CLOSE_OUTPUT);
                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();
        }
        catch (IOException e) {
            throw new LoadingException(e.getMessage(), e);
        }
        finally {
            cleanupStreams(input, zis);
        }
    }

    /**
     *
     * @param destinationDir
     * @param zipEntry
     * @return
     * @throws IOException
     */
    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException
    {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator))
            throw new IOException("ZipEntry is outside of the target dir: " + zipEntry.getName());

        return destFile;
    }

    /**
     *
     * @param is
     * @param zis
     */
    private static void cleanupStreams(InputStream is, ZipInputStream zis)
    {
        try {
            if (zis != null)
            {
                is.readAllBytes();
                zis.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
