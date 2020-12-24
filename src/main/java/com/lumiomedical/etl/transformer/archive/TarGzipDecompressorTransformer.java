package com.lumiomedical.etl.transformer.archive;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.transformer.TransformationException;
import com.lumiomedical.flow.actor.transformer.Transformer;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/11/24
 */
public class TarGzipDecompressorTransformer implements Transformer<InputStream, InputStream>
{
    @Override
    public InputStream transform(InputStream input) throws TransformationException
    {
        try (InputStream gzi = new GzipCompressorInputStream(input) ; ArchiveInputStream ai = new TarArchiveInputStream(gzi))
        {
            var os = new ByteArrayOutputStream();

            Logging.logger.info("Unzipping compressed input stream...");

            IOUtils.copy(ai, os);

            return new ByteArrayInputStream(os.toByteArray());
        }
        catch (IOException e) {
            throw new TransformationException(e.getMessage(), e);
        }
    }
}
