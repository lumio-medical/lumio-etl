package com.lumiomedical.etl.vault;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lumiomedical.flow.Flow;
import com.lumiomedical.vault.container.Invocation;
import com.lumiomedical.vault.container.definition.Definitions;
import com.lumiomedical.vault.container.definition.ServiceAlias;
import com.lumiomedical.vault.container.definition.ServiceDefinition;
import com.lumiomedical.vault.container.definition.ServiceProvider;
import com.lumiomedical.vault.exception.VaultParserException;
import com.lumiomedical.vault.parser.module.ServiceModule;
import com.lumiomedical.vault.parser.module.VaultModule;
import com.noleme.json.Json;

import java.util.HashSet;
import java.util.Set;

import static com.noleme.commons.function.RethrowConsumer.rethrower;

/**
 * @author Pierre Lecerf (plecerf@lumiomedical.com)
 * Created on 2020/11/24
 */
public class ETLModule implements VaultModule
{
    private final VaultModule serviceModule = new ServiceModule();

    @Override
    public String identifier()
    {
        return "etl";
    }

    @Override
    public void process(ObjectNode node, Definitions definitions) throws VaultParserException
    {
        node.fields().forEachRemaining(rethrower(entry -> {
            checkRequired(entry.getValue(), "id", "scenario", "flows");

            String etlName = entry.getKey();
            ObjectNode json = (ObjectNode) entry.getValue();
            String identifier = json.get("id").asText();
            String scenario = json.get("scenario").asText();
            ObjectNode flows = (ObjectNode) json.get("flows");

            if (!flows.has(scenario))
                throw new VaultParserException("The "+etlName+" ETL has no defined "+scenario+" scenario flow.");

            Set<String> activeActors = this.buildFlow(flows.get(scenario), definitions, identifier, etlName, scenario);

            if (json.has("actors"))
            {
                ObjectNode requiredActorsJson = Json.newObject();

                json.get("actors").fields().forEachRemaining(actorEntry -> {
                    if (!activeActors.contains(actorEntry.getKey()))
                        return;
                    requiredActorsJson.set(actorEntry.getKey(), actorEntry.getValue());
                });

                this.serviceModule.process(requiredActorsJson, definitions);
            }

            definitions.setVariable("_etl."+entry.getKey()+".id", identifier);
            definitions.setVariable("_etl."+entry.getKey()+".scenario", scenario);
        }));
    }

    /**
     *
     * @param flowNode
     * @param definitions
     * @param serviceIdentifier
     * @param etlName
     * @param scenario
     * @return a set of actor identifiers that have been referenced in this scenario's flow
     */
    private Set<String> buildFlow(JsonNode flowNode, Definitions definitions, String serviceIdentifier, String etlName, String scenario)
    {
        Set<String> activeActors = new HashSet<>();

        String previousNodeIdentifier = null;
        for (JsonNode flowEntry : flowNode)
        {
            String currentActorIdentifier = flowEntry.isTextual() ? flowEntry.asText() : flowEntry.get("id").asText();
            String currentNodeIdentifier = "_"+currentActorIdentifier+"."+etlName+"."+scenario+".node";

            ServiceDefinition def;
            if (previousNodeIdentifier == null)
            {
                def = new ServiceProvider(
                    currentNodeIdentifier,
                    Flow.class.getName(),
                    "from"
                ).setMethodArgs(new Object[]{ "@"+currentActorIdentifier });
            }
            else {
                def = new ServiceProvider(
                    currentNodeIdentifier,
                    Flow.class.getName(),
                    "into"
                ).setMethodArgs(new Object[]{
                    "@"+previousNodeIdentifier,
                    "@"+currentActorIdentifier
                });
            }

            if (flowEntry.has("max_parallelism"))
                def.addInvocation(new Invocation("setMaxParallelism", flowEntry.get("max_parallelism").asInt()));

            definitions.setDefinition(currentNodeIdentifier, def);

            activeActors.add(currentActorIdentifier);

            previousNodeIdentifier = currentNodeIdentifier;
        }

        definitions.setDefinition(serviceIdentifier, new ServiceAlias(serviceIdentifier, previousNodeIdentifier));

        return activeActors;
    }

    /**
     *
     * @param json
     * @param keys
     * @throws VaultParserException
     */
    private static void checkRequired(JsonNode json, String... keys) throws VaultParserException
    {
        if (!json.isObject())
            throw new VaultParserException("Each entry in the etl module is expected to be an object.");
        for (String key : keys)
        {
            if (!json.has(key))
                throw new VaultParserException("Each entry in the etl module is expected to provide the following properties: "+String.join(", ", keys));
        }
    }
}
