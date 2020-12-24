package com.lumiomedical.etl.loader;

import com.lumiomedical.flow.actor.loader.Loader;

import java.util.Collection;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/12/19
 */
public final class Loaders
{
    private Loaders() {}

    /**
     * An adapter function for leveraging a given Loader over a collection of its inputs.
     * It essentially produces a Loader that will iterate over the input collection and delegate each item to the provided Loader implementation.
     *
     * @param loader
     * @param <I>
     * @param <C>
     * @return
     */
    public static <I, C extends Collection<I>> Loader<C> aggregate(Loader<I> loader)
    {
        return collection -> {
            for (I input : collection)
                loader.load(input);
        };
    }
}
