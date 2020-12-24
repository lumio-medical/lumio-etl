package com.lumiomedical.etl.transformer.text;

import com.lumiomedical.flow.actor.transformer.TransformationException;
import com.lumiomedical.flow.actor.transformer.Transformer;

import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/12/17
 */
public class RegexReplace implements Transformer<String, String>
{
    private final Pattern pattern;
    private final Function<MatchResult, String> replacement;

    public RegexReplace(Pattern pattern, Function<MatchResult, String> replacement)
    {
        this.pattern = pattern;
        this.replacement = replacement;
    }

    public RegexReplace(Pattern pattern, String replacement)
    {
        this(pattern, mr -> replacement);
    }

    public RegexReplace(String regex, Function<MatchResult, String> replacement)
    {
        this(Pattern.compile(regex), replacement);
    }

    public RegexReplace(String regex, String replacement)
    {
        this(Pattern.compile(regex), mr -> replacement);
    }

    @Override
    public String transform(String input) throws TransformationException
    {
        return this.pattern.matcher(input).replaceAll(this.replacement);
    }
}
