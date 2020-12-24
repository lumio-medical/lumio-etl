package com.lumiomedical.nlp.etl;

import com.lumiomedical.etl.transformer.text.BasicSentenceTokenizer;
import com.lumiomedical.flow.actor.transformer.TransformationException;
import com.lumiomedical.flow.actor.transformer.Transformer;
import com.lumiomedical.nlp.data.Document;
import com.lumiomedical.nlp.data.Sentence;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/12/23
 */
public class WikipediaSentenceTokenizer implements Transformer<Document, Document>
{
    private final BasicSentenceTokenizer tokenizer;

    public WikipediaSentenceTokenizer()
    {
        this.tokenizer = new BasicSentenceTokenizer();
    }

    @Override
    public Document transform(Document document) throws TransformationException
    {
        for (Sentence sentence : document.getSentences())
            sentence.setTokens(this.tokenizer.transform(sentence.getText()));

        return document;
    }
}