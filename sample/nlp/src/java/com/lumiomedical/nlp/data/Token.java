package com.lumiomedical.nlp.data;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/12/23
 */
public class Token
{
    public final String value;
    public final int frequency;

    public Token(String value, int frequency)
    {
        this.value = value;
        this.frequency = frequency;
    }
}
