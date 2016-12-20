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
package org.culturegraph.mf.metamorph.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 * tests {@link ISBN}
 *
 * @author Markus Michael Geipel
 */

public final class ISBNTest {

	private static final String ISBN13A = "9781933988313";
	private static final String ISBN10A = "1933988312";
	private static final String ISBN10B = "3406548407";

	private static final String ISBN10A_DIRTY = "ISBN: 1-.93.3-988-31-2 EUro 17.70";
	private static final String ISBN10C_DIRTY = "ISBN 3-7691-3150-9 1. Aufl. 2006";
	private static final String ISBN13D_DIRTY = "ISBN 978-3-608-91086-5 (Klett-Cotta) ab der 7. Aufl.";
	private static final String ISBN10F_DIRTY = "ISBN 88-7336-210-9 35.00 EUR";

	private static final String ISBN_INCORRECT_CHECK13 = "9781933988314";
	private static final String ISBN_INCORRECT_CHECK10 = "1933988311";


	private static final String ISBN_INCORRECT_SIZE1 = "12345678901234";
	private static final String ISBN_INCORRECT_SIZE2 = "123456789012";
	private static final String ISBN_INCORRECT_SIZE3 = "123456789";
	private static final String ERROR = "invalid";



	@Test
	public void testProcess(){
		final ISBN isbn = new ISBN();
		isbn.setTo("isbn13");
		assertEquals(ISBN13A, isbn.process(ISBN10A));
		isbn.setTo("isbn10");
		assertEquals(ISBN10A, isbn.process(ISBN13A));
		isbn.setTo("cleanse");
		assertEquals(ISBN10A, isbn.process(ISBN10A_DIRTY));

	}

	@Test
	public void testTo13() {
		assertEquals(ISBN13A, ISBN.isbn10to13(ISBN10A));
	}

	@Test
	public void testTo10() {
		assertEquals(ISBN10A,ISBN.isbn13to10(ISBN13A));
	}

	@Test
	public void testCleanse() {
		assertEquals(ISBN10A,ISBN.cleanse(ISBN10A_DIRTY));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidInputTo13() {
		ISBN.isbn10to13(ISBN_INCORRECT_SIZE3);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidInputTo10() {
		ISBN.isbn13to10(ISBN_INCORRECT_SIZE2);
	}

	@Test
	public void testIsValid() {
		assertFalse(ISBN.isValid(ISBN_INCORRECT_CHECK13));
		assertFalse(ISBN.isValid(ISBN_INCORRECT_CHECK10));
		assertFalse(ISBN.isValid(ISBN_INCORRECT_SIZE1));
		assertFalse(ISBN.isValid(ISBN_INCORRECT_SIZE2));
		assertFalse(ISBN.isValid(ISBN_INCORRECT_SIZE3));

		assertTrue(ISBN.isValid(ISBN10B));
		assertTrue(ISBN.isValid(ISBN10A));
		assertTrue(ISBN.isValid(ISBN13A ));
		assertTrue(ISBN.isValid(ISBN.cleanse(ISBN10C_DIRTY)));
		assertTrue(ISBN.isValid(ISBN.cleanse(ISBN13D_DIRTY)));
		assertTrue(ISBN.isValid(ISBN.cleanse(ISBN10F_DIRTY)));
	}


	public void testCleanseException1(){
		final ISBN isbn = new ISBN();
		isbn.setErrorString(ERROR);
		assertTrue(isbn.process(ISBN_INCORRECT_SIZE3).startsWith(ERROR));
	}


	public void testCleanseException2(){
		final ISBN isbn = new ISBN();
		isbn.setErrorString(ERROR);
		assertTrue(isbn.process(ISBN_INCORRECT_SIZE1).startsWith(ERROR));
	}

}
