package com.lumiomedical.etl.extractor;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.extractor.ExtractionException;
import com.lumiomedical.flow.actor.extractor.Extractor;
import com.lumiomedical.flow.interruption.InterruptionException;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/12/19
 */
public final class Extractors
{
    private Extractors() {}

    /**
     *
     * @param extractor
     * @param <O>
     * @return
     */
    public static <O> Extractor<O> nonFatal(Extractor<O> extractor)
    {
        return () -> {
            try {
                return extractor.extract();
            }
            catch (ExtractionException e) {
                Logging.logger.error(e.getMessage(), e);
                throw InterruptionException.interrupt();
            }
        };
    }
}
