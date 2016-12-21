/*
 * Copyright 2013, 2014 Deutsche Nationalbibliothek
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
package org.culturegraph.mf.formeta;

import org.culturegraph.mf.formeta.formatter.Formatter;
import org.culturegraph.mf.formeta.formatter.FormatterStyle;
import org.culturegraph.mf.framework.FluxCommand;
import org.culturegraph.mf.framework.ObjectReceiver;
import org.culturegraph.mf.framework.StreamReceiver;
import org.culturegraph.mf.framework.annotations.Description;
import org.culturegraph.mf.framework.annotations.In;
import org.culturegraph.mf.framework.annotations.Out;
import org.culturegraph.mf.framework.helpers.DefaultStreamPipe;

/**
 * Encodes streams in formeta format.
 *
 * @author Christoph Böhme
 *
 */
@Description("Encodes streams in formeta format.")
@In(StreamReceiver.class)
@Out(String.class)
@FluxCommand("encode-formeta")
public final class FormetaEncoder extends
		DefaultStreamPipe<ObjectReceiver<String>> {

	private FormatterStyle style = FormatterStyle.CONCISE;
	private Formatter formatter = style.createFormatter();

	public FormatterStyle getStyle() {
		return style;
	}

	public void setStyle(final FormatterStyle formatterStyle) {
		this.style = formatterStyle;
		formatter = formatterStyle.createFormatter();
	}


	@Override
	public void startRecord(final String identifier) {
		formatter.reset();
		formatter.startGroup(identifier);
	}

	@Override
	public void endRecord() {
		formatter.endGroup();
		getReceiver().process(formatter.toString());
	}

	@Override
	public void startEntity(final String name) {
		formatter.startGroup(name);
	}

	@Override
	public void endEntity() {
		formatter.endGroup();
	}

	@Override
	public void literal(final String name, final String value) {
		formatter.literal(name, value);
	}

}
