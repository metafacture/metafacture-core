/*
 * Copyright 2018 Deutsche Nationalbibliothek
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
package org.metafacture.xslt.mockito;

import org.mockito.ArgumentMatcher;
import org.xml.sax.Attributes;

public class SingleAttributeMatcher
{
    public static ArgumentMatcher<Attributes> hasSingleAttribute(String uri, String localName,
                                                                 String qName,
                                                                 String type, String value)
    {
        return new ArgumentMatcher<Attributes>()
        {
            @Override
            public boolean matches(Attributes argument)
            {
                return argument.getLength() == 1 &&
                        argument.getURI(0).equals(uri) &&
                        argument.getLocalName(0).equals(localName) &&
                        argument.getQName(0).equals(qName) &&
                        argument.getType(0).equals(type) &&
                        argument.getValue(0).equals(value);
            }
        };
    }
}
