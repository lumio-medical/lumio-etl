package com.lumiomedical.etl.transformer.archive;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.transformer.TransformationException;
import com.lumiomedical.flow.actor.transformer.Transformer;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/11/24
 */
public class TarGzipTransformer implements Transformer<InputStream, InputStream>
{
    @Override
    public InputStream transform(InputStream input) throws TransformationException
    {
        try {
            Logging.logger.info("Producing unzip compressed input stream...");

            InputStream gzi = new GzipCompressorInputStream(input);
            ArchiveInputStream ai = new TarArchiveInputStream(gzi);

            return ai;
        }
        catch (IOException e) {
            throw new TransformationException(e.getMessage(), e);
        }
    }
}
