package com.lumiomedical.etl.transformer.text;

import com.lumiomedical.flow.actor.transformer.TransformationException;
import com.lumiomedical.flow.actor.transformer.Transformer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/12/17
 */
public class BasicTokenFrequency <C extends Collection<String>> implements Transformer<C, Map<String, Integer>>
{
    @Override
    public Map<String, Integer> transform(C tokens) throws TransformationException
    {
        Map<String, Integer> frequency = new HashMap<>();
        for (String token : tokens)
        {
            if (!frequency.containsKey(token))
                frequency.put(token, 1);
            else
                frequency.put(token, frequency.get(token) + 1);
        }
        return frequency;
    }
}
