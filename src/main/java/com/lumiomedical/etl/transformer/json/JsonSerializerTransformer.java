package com.lumiomedical.etl.transformer.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.lumiomedical.flow.etl.transformer.TransformationException;
import com.lumiomedical.flow.etl.transformer.Transformer;
import com.noleme.json.Json;
import com.noleme.json.JsonException;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/08/21
 */
public class JsonSerializerTransformer<T> implements Transformer<T, String>
{
    private final boolean prettyPrint;

    public JsonSerializerTransformer()
    {
        this(false);
    }

    /**
     *
     * @param prettyPrint
     */
    public JsonSerializerTransformer(boolean prettyPrint)
    {
        this.prettyPrint = prettyPrint;
    }

    @Override
    public String transform(T input) throws TransformationException
    {
        try {
            JsonNode node = Json.toJson(input);

            return this.prettyPrint
                ? Json.prettyPrint(node)
                : Json.stringify(node)
            ;
        }
        catch (JsonException e) {
            throw new TransformationException(e.getMessage(), e);
        }
    }
}
