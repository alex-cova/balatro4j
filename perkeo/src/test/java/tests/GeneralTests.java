package tests;

import com.balatro.LuaRandom;
import com.balatro.cache.Query;
import com.balatro.enums.Edition;
import com.balatro.enums.LegendaryJoker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GeneralTests {

    static ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    @Test
    void testQueryDeserialization() throws JsonProcessingException {
        var query = new Query(LegendaryJoker.Perkeo.getName(), Edition.Foil);
        var json = objectMapper.writeValueAsString(query);

        System.out.println(json);

        var deserializedQuery = objectMapper.readValue(json, Query.class);

        Assertions.assertEquals(query.getItem(), deserializedQuery.getItem());
        Assertions.assertEquals(query.getEdition(), deserializedQuery.getEdition());
    }

    @Test
    void testRandom() {
        Assertions.assertEquals(0.794206292431241, LuaRandom.random(0.0));
    }
}
