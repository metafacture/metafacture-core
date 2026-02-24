package org.metafacture.solr;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class HashMapListTest {

    private HashMapList<String, String> hashMapList;

    @Before
    public void setUp() throws Exception {
        hashMapList = new HashMapList<>();
    }

    @Test
    public void shouldReturnList() {
        hashMapList.add("key", "value");
        Assert.assertThat(hashMapList.get("key"), CoreMatchers.is(CoreMatchers.instanceOf(List.class)));
    }

    @Test
    public void shouldContainValue() {
        hashMapList.add("key", "value1");
        hashMapList.add("key", "value2");
        final List<String> values = hashMapList.get("key");
        Assert.assertThat(values, CoreMatchers.hasItems("value1", "value2"));
    }

    @Test
    public void containsKey() {
        hashMapList.add("key", "value");
        Assert.assertThat(hashMapList.containsKey("key"), CoreMatchers.equalTo(true));
    }

    @Test
    public void asMap() {
        hashMapList.add("key", "value");
        final Map<String, List<String>> map = hashMapList.asMap();
        Assert.assertThat(map.containsKey("key"), CoreMatchers.equalTo(true));
        Assert.assertThat(map.get("key"), CoreMatchers.hasItem("value"));
    }
}
