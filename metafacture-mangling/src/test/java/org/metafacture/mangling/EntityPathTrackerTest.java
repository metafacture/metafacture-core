/*
 * Copyright 2016 Deutsche Nationalbibliothek
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.metafacture.mangling;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for class {@link EntityPathTracker}.
 *
 * @author Christoph Böhme
 *
 */
public class EntityPathTrackerTest {

    private EntityPathTracker pathTracker;

    public EntityPathTrackerTest() {
    }

    @Before
    public void initSystemUnderTest() {
        pathTracker = new EntityPathTracker();
    }

    @Test
    public void getCurrentPathShouldReturnEmptyPathIfProcessingHasNotStarted() {
        Assert.assertTrue(pathTracker.getCurrentPath().isEmpty());
    }

    @Test
    public void getCurrentPathShouldReturnEmptyPathIfNotInRecord() {
        pathTracker.startRecord("1");
        pathTracker.endRecord();
        Assert.assertTrue(pathTracker.getCurrentPath().isEmpty());
    }

    @Test
    public void getCurrentPathShouldReturnPathToCurrentEntity() {
        pathTracker.startRecord("1");
        Assert.assertEquals("", pathTracker.getCurrentPath());
        pathTracker.startEntity("granny");
        Assert.assertEquals("granny", pathTracker.getCurrentPath());
        pathTracker.startEntity("mommy");
        Assert.assertEquals("granny.mommy", pathTracker.getCurrentPath());
        pathTracker.startEntity("me");
        Assert.assertEquals("granny.mommy.me", pathTracker.getCurrentPath());
        pathTracker.endEntity();
        Assert.assertEquals("granny.mommy", pathTracker.getCurrentPath());
        pathTracker.startEntity("my-sister");
        Assert.assertEquals("granny.mommy.my-sister", pathTracker.getCurrentPath());
        pathTracker.endEntity();
        Assert.assertEquals("granny.mommy", pathTracker.getCurrentPath());
        pathTracker.endEntity();
        Assert.assertEquals("granny", pathTracker.getCurrentPath());
        pathTracker.endEntity();
        Assert.assertEquals("", pathTracker.getCurrentPath());
    }

    @Test
    public void startRecordShouldResetPath() {
        pathTracker.startRecord("1");
        pathTracker.startEntity("entity");
        Assert.assertEquals("entity", pathTracker.getCurrentPath());

        pathTracker.startRecord("2");
        Assert.assertTrue(pathTracker.getCurrentPath().isEmpty());
    }

    @Test
    public void resetStreamShouldResetPath() {
        pathTracker.startRecord("1");
        pathTracker.startEntity("entity");
        Assert.assertEquals("entity", pathTracker.getCurrentPath());

        pathTracker.resetStream();
        Assert.assertTrue(pathTracker.getCurrentPath().isEmpty());
    }

    @Test
    public void closeStreamShouldResetPath() {
        pathTracker.startRecord("1");
        pathTracker.startEntity("entity");
        Assert.assertEquals("entity", pathTracker.getCurrentPath());

        pathTracker.closeStream();
        Assert.assertTrue(pathTracker.getCurrentPath().isEmpty());
    }

    @Test
    public void getCurrentPathWithShouldAppendLiteralNameToPath() {
        pathTracker.startRecord("1");
        pathTracker.startEntity("entity");

        Assert.assertEquals("entity.literal", pathTracker.getCurrentPathWith("literal"));
    }

    @Test
    public void getCurrentPathWithShouldReturnOnlyLiteralNameIfNotInEntity() {
        pathTracker.startRecord("1");

        Assert.assertEquals("literal", pathTracker.getCurrentPathWith("literal"));
    }

    @Test
    public void getCurrentEntityNameShouldReturnNullIfProcessingNotStarted() {
        Assert.assertNull(pathTracker.getCurrentEntityName());
    }

    @Test
    public void getCurrentEntityNameShouldReturnNullIfNotInRecord() {
        pathTracker.startRecord("1");
        pathTracker.endRecord();

        Assert.assertNull(pathTracker.getCurrentEntityName());
    }

    @Test
    public void getCurrentEntityNameShouldReturnNameOfCurrentEntity() {
        pathTracker.startRecord("1");
        Assert.assertNull(pathTracker.getCurrentEntityName());
        pathTracker.startEntity("grandad");
        Assert.assertEquals("grandad", pathTracker.getCurrentEntityName());
        pathTracker.startEntity("daddy");
        Assert.assertEquals("daddy", pathTracker.getCurrentEntityName());
        pathTracker.endEntity();
        Assert.assertEquals("grandad", pathTracker.getCurrentEntityName());
        pathTracker.endEntity();
        Assert.assertNull(pathTracker.getCurrentEntityName());
        pathTracker.endRecord();
    }

}
