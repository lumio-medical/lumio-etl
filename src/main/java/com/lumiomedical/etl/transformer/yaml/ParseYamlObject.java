package com.lumiomedical.etl.transformer.yaml;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
public class ParseYamlObject implements Transformer<InputStream, ObjectNode>
{
    @Override
    public ObjectNode transform(InputStream input) throws TransformationException
    {
        try {
            Logging.logger.info("Transforming input stream into a YAML object.");
            var yaml = Yaml.parse(input);

            if (!yaml.isObject())
                throw new TransformationException("The provided input could be parsed as YAML but doesn't seem to represent a YAML object.");

            return (ObjectNode) yaml;
        }
        catch (JsonException e) {
            throw new TransformationException("An error occurred while attempting to parse input as a YAML object.", e);
        }
    }
}
