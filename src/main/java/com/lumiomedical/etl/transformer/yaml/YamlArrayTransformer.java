package com.lumiomedical.etl.transformer.yaml;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.lumiomedical.etl.logging.Logging;
import com.lumiomedical.flow.actor.transformer.TransformationException;
import com.lumiomedical.flow.actor.transformer.Transformer;
import com.noleme.json.JsonException;
import com.noleme.json.Yaml;

import java.io.InputStream;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/04/13
 */
public class YamlArrayTransformer implements Transformer<InputStream, ArrayNode>
{
    @Override
    public ArrayNode transform(InputStream input) throws TransformationException
    {
        try {
            Logging.logger.info("Transforming input stream into a YAML array.");
            var yaml = Yaml.parse(input);

            if (!yaml.isArray())
                throw new TransformationException("The provided input could be parsed as YAML but doesn't seem to represent a YAML array.");

            return (ArrayNode) yaml;
        }
        catch (JsonException e) {
            throw new TransformationException("An error occurred while attempting to parse input as a YAML array.", e);
        }
    }
}
