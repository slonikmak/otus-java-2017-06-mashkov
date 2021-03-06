package jsonParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.otus.app.InnerTestClass;
import ru.otus.app.TestClass;
import ru.otus.app.TestClassAdapter;
import ru.otus.jsonparser.JsonParserException;
import ru.otus.jsonparser.SimpleJsonParser;
import ru.otus.jsonparser.SimpleJsonParserBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;


class JsonParserTest {
    SimpleJsonParser parser;
    Gson gson;
    TestClass obj;

    @BeforeEach
    void setup(){
        parser = new SimpleJsonParserBuilder()
                .setTypeAdapter(new TestClassAdapter(), InnerTestClass.class)
                .build();
        gson = new GsonBuilder().registerTypeAdapter(InnerTestClass.class, new InnerTypeAdapter()).create();
        obj = new TestClass();
    }

    @Test
    void parse() {
        String expected = gson.toJson(obj);
        System.out.println(expected);
        String actual = null;
        try {
            actual = parser.toJson(obj);
        } catch (JsonParserException e) {
            e.printStackTrace();
        }
        System.out.println(actual);
        assertEquals(expected, actual);
    }



}