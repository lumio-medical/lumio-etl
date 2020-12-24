package com.lumiomedical.nlp.etl;

import com.lumiomedical.etl.transformer.text.BasicSentenceSplitter;
import com.lumiomedical.flow.actor.transformer.TransformationException;
import com.lumiomedical.flow.actor.transformer.Transformer;
import com.lumiomedical.nlp.data.Document;
import com.lumiomedical.nlp.data.Sentence;

import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/12/23
 */
public class WikipediaSentenceSplitter implements Transformer<Document, Document>
{
    private final BasicSentenceSplitter splitter;

    public WikipediaSentenceSplitter(Locale locale)
    {
        this.splitter = new BasicSentenceSplitter(locale);
    }

    @Override
    public Document transform(Document document) throws TransformationException
    {
        var sentences = this.splitter.transform(document.getCleanText());

        return document.setSentences(sentences.stream()
            .map(Sentence::new)
            .collect(Collectors.toList())
        );
    }
}
