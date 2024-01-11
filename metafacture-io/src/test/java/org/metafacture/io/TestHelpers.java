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
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public final class TestHelpers {

    public static void assertFile(final ObjectReceiver<Reader> receiver, final String expected, final File file, final Consumer<FileOpener> consumer) {
        assertReader(receiver, expected, () -> {
            final FileOpener opener = new FileOpener();
            if (consumer != null) {
                consumer.accept(opener);
            }

            opener.setReceiver(receiver);
            opener.process(file.getAbsolutePath());
            opener.closeStream();

            return 1;
        });
    }

    public static void assertReader(final ObjectReceiver<Reader> receiver, final String expected, final IntSupplier supplier) {
        final StringBuilder sb = new StringBuilder();

        Mockito.doAnswer(i -> {
            sb.delete(0, sb.length());
            sb.append(ResourceUtil.readAll(i.getArgument(0)));

            return null;
        }).when(receiver).process(Mockito.any(Reader.class));

        final int times = supplier.getAsInt();

        Mockito.verify(receiver, Mockito.times(times)).process(Mockito.any(Reader.class));
        Assert.assertEquals(expected, sb.toString());
    }

}
