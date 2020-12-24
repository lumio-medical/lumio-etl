package com.lumiomedical.etl.transformer;

import com.lumiomedical.flow.actor.transformer.Transformer;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/12/22
 */
public final class Adapter <I, O, AI, AO>
{
    private final Function<I, AI> adapter;
    private final BiFunction<I, AO, O> handler;

    public Adapter(Function<I, AI> adapter, BiFunction<I, AO, O> handler)
    {
        this.adapter = adapter;
        this.handler = handler;
    }

    /**
     *
     * @param transformer
     * @return
     */
    public Transformer<I, O> adapt(Transformer<AI, AO> transformer)
    {
        return input -> {
            var adapted = this.adapter.apply(input);
            var output = transformer.transform(adapted);
            return this.handler.apply(input, output);
        };
    }
}
