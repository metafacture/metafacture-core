/*
 * Copyright 2019 hbz
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
package org.metafacture.biblio.pica;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.metafacture.commons.StringUtil;
import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MissingIdException;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultObjectPipe;

/**
 * Parser plain pica. For example, when run on the input
 * 
 * <pre>
 * 003@ $01234
 * 028A $aAndy$dWarhol
 * </pre>
 *
 * the {@code PicaPlainDecoder} will produce the following sequence of events:
 * 
 * <pre>
 * {@literal
 * start-record "1234"
 * start-entity "003@"
 * literal "0": 1234
 * end-entity
 * start-entity "028A"
 * literal "a": Andy
 * literal "d": Warhol
 * end-entity
 * end-record
 * }
 * </pre>
 *
 * The parser assumes that the input is utf-8 encoded. The parser does not
 * support other pica encodings.
 *
 * @author Pascal Christoph (dr0i)
 *
 */
@Description("Parses plain pica records. The parser only parses single records. "
		+ "A string containing multiple records must be split into "
		+ "individual records before passing it to PicaPlainDecoder.")
@In(String.class)
@Out(StreamReceiver.class)
@FluxCommand("decode-plain-pica")
public final class PicaPlainDecoder extends DefaultObjectPipe<String, StreamReceiver> {

	private char read;
	private AtomicInteger id = new AtomicInteger();

	@Override
	public void process(final String record) {
		assert !isClosed();
		getReceiver().startRecord(String.valueOf(id.getAndIncrement()));
		try {
			String[] recordArr = record.split("\n");
			for (int x = 0; x < recordArr.length; x++) {
				int i = 0;
				for (; i < recordArr[x].length(); ++i) {// find field name
					read = recordArr[x].charAt(i);
					if (read == ' ' || read == '$')
						break;
				}
				getReceiver().startEntity(recordArr[x].substring(0, i)); // field name
				if (recordArr[x].length() >= i + 2) {
					String[] subfields;
					subfields = recordArr[x].substring(i + 2).split("\\$");
					for (i = 0; i < subfields.length; i++) {
						if (subfields[i].length() > 0)
							getReceiver().literal(subfields[i].substring(0, 1), subfields[i].substring(1));
					}
				}
				getReceiver().endEntity();
			}
		} catch (Exception e) {
			System.out.println(record);
			e.printStackTrace();
		}
		getReceiver().endRecord();
	}

}
