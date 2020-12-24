package com.lumiomedical.etl.transformer;

import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.extractor.ExtractionException;
import com.lumiomedical.flow.actor.extractor.Extractor;
import com.lumiomedical.flow.actor.transformer.BiTransformer;
import com.lumiomedical.flow.actor.transformer.TransformationException;
import com.lumiomedical.flow.actor.transformer.Transformer;
import com.lumiomedical.flow.interruption.InterruptionException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/12/19
 */
public final class Transformers
{
    private Transformers() {}

    /**
     *
     * @param extractor
     * @param <O>
     * @return
     * @throws TransformationException
     */
    public static <O> O extract(Extractor<O> extractor) throws TransformationException
    {
        try {
            return extractor.extract();
        }
        catch (ExtractionException e) {
            throw new TransformationException(e.getMessage(), e);
        }
    }

    /**
     *
     * @param transformer
     * @param <I>
     * @param <O>
     * @return
     */
    public static <I, O> Transformer<I, O> nonFatal(Transformer<I, O> transformer)
    {
        return input -> {
            try {
                return transformer.transform(input);
            }
            catch (TransformationException e) {
                Logging.logger.error(e.getMessage(), e);
                throw InterruptionException.interrupt();
            }
        };
    }

    /**
     *
     * @param transformer
     * @param <I1>
     * @param <I2>
     * @param <O>
     * @return
     */
    public static <I1, I2, O> BiTransformer<I1, I2, O> nonFatal(BiTransformer<I1, I2, O> transformer)
    {
        return (a, b) -> {
            try {
                return transformer.transform(a, b);
            }
            catch (TransformationException e) {
                Logging.logger.error(e.getMessage(), e);
                throw InterruptionException.interrupt();
            }
        };
    }

    /**
     * An adapter function for leveraging a given Transformer over a collection of its inputs.
     * It essentially produces a Transformer that will iterate over the input collection and delegate each item to the provided Transformer implementation.
     *
     * @param transformer
     * @param <I>
     * @param <O>
     * @param <C>
     * @return
     */
    public static <I, O, C extends Collection<I>> Transformer<C, List<O>> aggregate(Transformer<I, O> transformer)
    {
        return collection -> {
            List<O> output = new ArrayList<>(collection.size());
            for (I input : collection)
                output.add(transformer.transform(input));
            return output;
        };
    }

    /**
     * An adapter function for leveraging a given BiTransformer over a collection of its inputs.
     * It essentially produces a BiTransformer that will iterate over the input collection and delegate each item to the provided BiTransformer implementation.
     *
     * @param transformer
     * @param <I1>
     * @param <I2>
     * @param <O>
     * @param <C>
     * @return
     */
    public static <I1, I2, O, C extends Collection<I1>> BiTransformer<C, I2, List<O>> aggregate(BiTransformer<I1, I2, O> transformer)
    {
        return (collection, joined) -> {
            List<O> output = new ArrayList<>(collection.size());
            for (I1 input : collection)
                output.add(transformer.transform(input, joined));
            return output;
        };
    }
}
