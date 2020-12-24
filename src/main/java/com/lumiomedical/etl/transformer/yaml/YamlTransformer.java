package com.lumiomedical.etl.transformer.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.lumiomedical.flow.actor.transformer.TransformationException;
import com.lumiomedical.flow.actor.transformer.Transformer;
import com.noleme.json.JsonException;
import com.noleme.json.Yaml;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/08/21
 */
public class YamlTransformer implements Transformer<Object, JsonNode>
{
    @Override
    public JsonNode transform(Object input) throws TransformationException
    {
        try {
            return Yaml.toYaml(input);
        }
        catch (JsonException e) {
            throw new TransformationException(e.getMessage(), e);
        }
    }
}
