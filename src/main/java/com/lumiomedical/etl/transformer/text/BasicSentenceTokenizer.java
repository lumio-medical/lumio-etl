package com.lumiomedical.etl.transformer.text;

import com.lumiomedical.flow.actor.transformer.TransformationException;
import com.lumiomedical.flow.actor.transformer.Transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/12/17
 */
public class BasicSentenceTokenizer implements Transformer<String, List<String>>
{
    @Override
    public List<String> transform(String input) throws TransformationException
    {
        List<String> tokens = new ArrayList<>();

        if (input.endsWith("."))
            input = input.substring(0, input.length() - 1);

        StringTokenizer tokenizer = new StringTokenizer(input, " \t\n\r\f,:;?![](){}'\"");

        while (tokenizer.hasMoreElements())
            tokens.add(tokenizer.nextToken());

        return tokens;
    }
}
