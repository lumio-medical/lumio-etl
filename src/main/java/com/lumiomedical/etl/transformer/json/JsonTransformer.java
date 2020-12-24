package com.lumiomedical.etl.transformer.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.lumiomedical.flow.actor.transformer.TransformationException;
import com.lumiomedical.flow.actor.transformer.Transformer;
import com.noleme.json.Json;
import com.noleme.json.JsonException;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/08/21
 */
public class JsonTransformer implements Transformer<Object, JsonNode>
{
    @Override
    public JsonNode transform(Object input) throws TransformationException
    {
        try {
            return Json.toJson(input);
        }
        catch (JsonException e) {
            throw new TransformationException(e.getMessage(), e);
        }
    }
}
