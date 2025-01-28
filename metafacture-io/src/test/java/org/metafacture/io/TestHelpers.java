/*
 * Copyright 2024 hbz NRW
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

package org.metafacture.io;

import org.metafacture.commons.ResourceUtil;
import org.metafacture.framework.ObjectReceiver;

import org.junit.Assert;
import org.mockito.Mockito;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public final class TestHelpers {

    private TestHelpers() {
    }

    public static void assertFile(final ObjectReceiver<Reader> receiver, final String expected, final File file, final Consumer<FileOpener> consumer) {
        assertReader(receiver, () -> {
            final FileOpener opener = new FileOpener();
            if (consumer != null) {
                consumer.accept(opener);
            }

            opener.setReceiver(receiver);
            opener.process(file.getAbsolutePath());
            opener.closeStream();
        }, expected);
    }

    public static void assertReader(final ObjectReceiver<Reader> receiver, final Runnable runnable, final String... expected) {
        final List<String> actual = new ArrayList<>();
        Mockito.doAnswer(i -> actual.add(ResourceUtil.readAll(i.getArgument(0)))).when(receiver).process(Mockito.any(Reader.class));

        runnable.run();

        Mockito.verify(receiver, Mockito.times(expected.length)).process(Mockito.any(Reader.class));
        Arrays.stream(expected).forEach(i -> Assert.assertEquals(i, actual.remove(0)));
        Assert.assertEquals(0, actual.size());
    }

}
