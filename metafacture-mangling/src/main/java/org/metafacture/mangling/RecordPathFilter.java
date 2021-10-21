/*
 * Copyright 2021 hbz NRW
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

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultStreamPipe;

/**
  Splits a stream into records based on entity path.
 */
@Description("Splits a stream into records based on entity path")
@In(StreamReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("filter-records-by-path")
public final class RecordPathFilter extends DefaultStreamPipe<StreamReceiver> {

    public static final String DEFAULT_RECORD_ID_FORMAT = "%s[%d]";

    public static final String ROOT_PATH = "";

    private final EntityPathTracker entityPathTracker = new EntityPathTracker();

    private String path = ROOT_PATH;
    private String recordIdFormat = DEFAULT_RECORD_ID_FORMAT;
    private String recordIdentifier;
    private boolean inMatch;
    private boolean recordStarted;
    private int recordCount;

    public RecordPathFilter() {
    }

    /**
     * Constructs a RecordPathFilter with a given path.
     *
     * @param path the name of the path
     */
    public RecordPathFilter(final String path) {
        setPath(path);
    }

    public void setEntitySeparator(final String entitySeparator) {
        entityPathTracker.setEntitySeparator(entitySeparator);
    }

    public String getEntitySeparator() {
        return entityPathTracker.getEntitySeparator();
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setRecordIdFormat(final String recordIdFormat) {
        this.recordIdFormat = recordIdFormat;
    }

    public String getRecordIdFormat() {
        return recordIdFormat;
    }

    @Override
    public void startRecord(final String identifier) {
        assert !isClosed();

        recordCount = 0;
        recordIdentifier = identifier;
        entityPathTracker.startRecord(identifier);
    }

    @Override
    public void endRecord() {
        assert !isClosed();

        endRecordIfNeeded();
        entityPathTracker.endRecord();
    }

    @Override
    public void startEntity(final String name) {
        entityPathTracker.startEntity(name);

        if (inMatch()) {
            startRecordIfNeeded();
            getReceiver().startEntity(name);
        }
        else if (pathMatching()) {
            inMatch = true;
        }
    }

    @Override
    public void endEntity() {
        if (pathMatching()) {
            endRecordIfNeeded();
            inMatch = false;
        }
        else if (inMatch()) {
            getReceiver().endEntity();
        }

        entityPathTracker.endEntity();
    }

    @Override
    public void literal(final String name, final String value) {
        if (inMatch()) {
            startRecordIfNeeded();
            getReceiver().literal(name, value);
        }
    }

    @Override
    public void onResetStream() {
        entityPathTracker.resetStream();
        resetRecord();
    }

    @Override
    public void onCloseStream() {
        entityPathTracker.closeStream();
    }

    private void resetRecord() {
        recordStarted = false;
        inMatch = false;
    }

    private void startRecordIfNeeded() {
        if (!recordStarted) {
            getReceiver().startRecord(String.format(recordIdFormat, recordIdentifier, ++recordCount));
            recordStarted = true;
        }
    }

    private void endRecordIfNeeded() {
        if (recordStarted) {
            getReceiver().endRecord();
            resetRecord();
        }
    }

    private boolean pathMatching() {
        return path.equals(entityPathTracker.getCurrentPath());
    }

    private boolean inMatch() {
        return inMatch || path.equals(ROOT_PATH);
    }

}
