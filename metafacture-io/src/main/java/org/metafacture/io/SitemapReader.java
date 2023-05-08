/*
 * Copyright 2020, 2022 Fabian Steeg, hbz
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

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

import org.joox.JOOX;
import org.joox.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Reads a sitemap and emits URLs.
 *
 * @author Fabian Steeg (fsteeg)
 */
@Description("Reads an XML sitemap from a URL, sends the sitemap's `loc` URLs to the receiver. " +
        "If the sitemap URL contains a `from=` query string parameter, the reader will keep paging until no more results are returned. " +
        "Set `filter` to send only URLs matching a given regular expression to the receiver (defaults to sending all URLs). " +
        "Set `limit` to limit the total number of URLs to send to the receiver (defaults to sending all URLs, set explicitly with `-1`). " +
        "Set `wait` for the time (in milliseconds) to wait after sending a URL to the receiver (defaults to `1000` i.e. 1 second).")
@In(String.class)
@Out(String.class)
@FluxCommand("read-sitemap")
public final class SitemapReader extends DefaultObjectPipe<String, ObjectReceiver<String>> {

    private static final Logger LOG = LoggerFactory.getLogger(SitemapReader.class);
    private static final int DEFAULT_WAIT = 1000;
    private static final int DEFAULT_LIMIT = Integer.MAX_VALUE;

    private String filter;
    private int limit = DEFAULT_LIMIT;
    private int wait = DEFAULT_WAIT;

    /**
     * Creates an instance of {@link SitemapReader}.
     */
    public SitemapReader() { }

    /**
     * @param filter The regex to match for filtering which URLs should be sent to the receiver.
     */
    public void setFilter(final String filter) {
        this.filter = filter;
    }

    /**
     * @param limit The total number of URLs that should be sent to the receiver (-1 for unlimited).
     */
    public void setLimit(final int limit) {
        this.limit = limit < 0 ? Integer.MAX_VALUE : limit;
    }

    /**
     * @param wait The time (in milliseconds) to wait after a URL has been sent to the receiver.
     */
    public void setWait(final int wait) {
        this.wait = wait;
    }

    @Override
    public void process(final String sitemap) {
        LOG.debug("Processing sitemap URL {}", sitemap);
        try {
            final Match siteMapXml = JOOX.$(new URL(sitemap));
            final List<String> urls = siteMapXml.find("loc")
                    .map(m -> m.element().getTextContent().trim()).stream()
                    .filter(s -> filter == null || s.matches(filter)).collect(Collectors.toList());
            sendAll(urls);
            tryNextPage(sitemap, urls.size());
        }
        catch (final SAXException | IOException e) {
            throw new MetafactureException(e.getMessage(), e);
        }
        catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MetafactureException(e.getMessage(), e);
        }
    }

    private void sendAll(final List<String> urls) throws InterruptedException {
        for (final String url : urls.subList(0, Math.min(limit, urls.size()))) {
            LOG.trace("Processing resource URL {}", url);
            getReceiver().process(url);
            Thread.sleep(wait);
        }
    }

    private void tryNextPage(final String sitemap, final int currentPageSize) {
        final String fromParam = "from=";
        final boolean pagingIsSupported = sitemap.contains(fromParam);
        final boolean isDone = currentPageSize == 0 || limit <= currentPageSize;
        if (pagingIsSupported && !isDone) {
            try (Scanner scanner = new Scanner(
                    sitemap.substring(sitemap.indexOf(fromParam) + fromParam.length()))) {
                if (scanner.hasNextInt()) {
                    final int lastFrom = scanner.nextInt();
                    final int nextFrom = lastFrom + currentPageSize;
                    process(sitemap.replace(fromParam + lastFrom, fromParam + nextFrom));
                }
            }
        }
    }

}
