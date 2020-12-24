package com.lumiomedical.etl.transformer.json;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.transformer.TransformationException;
import com.lumiomedical.flow.actor.transformer.Transformer;
import com.noleme.json.Json;
import com.noleme.json.JsonException;

import java.io.InputStream;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/04/13
 */
public class JsonObjectTransformer implements Transformer<InputStream, ObjectNode>
{
    @Override
    public ObjectNode transform(InputStream input) throws TransformationException
    {
        try {
            Logging.logger.info("Transforming input stream into a JSON object.");
            var json = Json.parse(input);

            if (!json.isObject())
                throw new TransformationException("The provided input could be parsed as JSON but doesn't seem to represent a JSON object.");

            return (ObjectNode) json;
        }
        catch (JsonException e) {
            throw new TransformationException("An error occurred while attempting to parse input as a JSON object.", e);
        }
    }
}
