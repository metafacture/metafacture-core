package org.metafacture.solr;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class HashMapListTest {

    private HashMapList<String,String> hashMapList;

    @Before
    public void setUp() throws Exception {
        hashMapList = new HashMapList<>();
    }

    @Test
    public void shouldReturnList() {
        hashMapList.add("key", "value");
        assertThat(hashMapList.get("key"), is(instanceOf(List.class)));
    }

    @Test
    public void shouldContainValue() {
        hashMapList.add("key", "value1");
        hashMapList.add("key", "value2");
        List<String> values = hashMapList.get("key");
        assertThat(values, hasItems("value1", "value2"));
    }

    @Test
    public void containsKey() {
        hashMapList.add("key", "value");
        assertThat(hashMapList.containsKey("key"), equalTo(true));
    }

    @Test
    public void asMap() {
        hashMapList.add("key", "value");
        Map<String,List<String>> map = hashMapList.asMap();
        assertThat(map.containsKey("key"), equalTo(true));
        assertThat(map.get("key"), hasItem("value"));
    }
}
