/*
 * Copyright 2016 Christoph Böhme
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

package org.metafacture.biblio.marc21;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.metafacture.framework.FormatException;
import org.metafacture.framework.ObjectReceiver;
import org.mockito.Mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link Marc21Decoder}.
 *
 * @author Christoph Böhme
 *
 */
public final class Marc21DecoderTest {

    private static final char SUBFIELD_MARKER = '\u001f';
    private static final char FIELD_SEPARATOR = '\u001e';
    private static final char RECORD_SEPARATOR = '\u001d';

    private static final String RECORD_ID = "identifier";
    private static final String CONTROLFIELD_VALUE = "controlfield";

    private static final String FIELD1 = "AB" + SUBFIELD_MARKER + "1"
            + "value1";
    private static final String FIELD2 = "CD" + SUBFIELD_MARKER + "2"
            + "value2" + SUBFIELD_MARKER + "3" + "value3";

    private static final String RECORD_LABEL = "00128noa a2200073zu 4500";
    private static final String DIRECTORY = "001001100000" + "002001300011"
            + "100001100024" + "200003100035";
    private static final String DATA = RECORD_ID + FIELD_SEPARATOR
            + CONTROLFIELD_VALUE + FIELD_SEPARATOR + FIELD1 + FIELD_SEPARATOR
            + FIELD2 + FIELD_SEPARATOR;
//    private static final String RECORD = RECORD_LABEL + DIRECTORY
//            + FIELD_SEPARATOR + DATA + RECORD_SEPARATOR;


//    private static final String RECORD="03994nas a2200913 c 4500001001000000003000700010005001700017007000300034008004100037016002200078016001700100022001400117022001400131022001400145022001400159022001400173022001400187022001400201022001400215022001400229022001400243022001400257022001400271030001000285035002000295035002100315040002800336041000800364044001000372082002900382084001300411084002700424084004400451210002700495245011300522246000800635246002800643246001800671246002200689246005300711246003200764246004700796246003400843246002800877246003100905246004100936246001700977246002800994246003101022260003101053362001301084500003401097501033301131515003701464550003301501555010101534591004801635650005701683650005901740650005701799650005901856689005501915689005701970689001902027689005502046689005702101689001902158775007002177775008602247780007002333780008002403780007402483780010502557780008402662780006902746780008002815780009902895780008602994010000445DE-10120121114152453.0tu991118c19479999ne u||p|r ||| 0||||0eng c7 2DE-101a0100004457 2DE-600a60-7  a0006-3002  a0005-2728  a0005-2736  a0304-4165  a0167-4838  a1388-1981  a0167-4889  a0167-4781  a0304-419X  a1570-9639  a0925-4439  a1874-9399  aBBACA  a(DE-599)ZDB60-7  a(OCoLC)183277993  a9001bgercDE-101d0029  aeng  cXA-NL74a570a540qDE-600222sdnb  a122ssgn  a570a540qDE-6002sdnb  aHMZ bio 1031144/1145rotaWA 15000]2rvk10aBiochim. Biophys. Acta10aBiochimica et biophysica actabBBA ; international journal of biochemistry, biophysics and molecular biology13aBBA13aMolecular cell research13aBioenergetics13aReviews on cancer13aBiochimica et biophysica acta / General subjects13aLipids and lipid metabolism13aProtein structure and molecular enzymology13aGene structure and expression13aProteins and proteomics13aMolecular basis of disease13aMolecular and cell biology of lipids13aBiomembranes13aReviews on biomembranes13aGene regulatory mechanisms3 aAmsterdam [u.a.]bElsevier0 a1.1947 -  aRepr.: New York, NY : Johnson  aAb 1990 ersch. die früheren Unterreihen und neuere Sektionen wechselweise in einzelnen Bd.; neue Sektion ab 1990: Molecular basis of disease; ab 1998: Molecular and cell biology of lipids; ab 2002: Proteins and proteomics; Reviews in biomembranes ab 2002 enth. in Biomembranes; neue Sektion ab 2008: Gene regulatory mechanisms  a1966 - 1967 auch Issue-Zählung  aHrsg. früher: W.T. Astbury8 aIndex 1972/2000(2002) zur Unterreihe \"Reviews on biomembranes\" in: 1516.2002,1; 51/100.1961/65 -  aAutopsie(keine 4245)Kopie bei 282711;s.Abl. 70(DE-588)4006891-20(DE-101)040068919aBiophysik2gnd 70(DE-588)4067488-50(DE-101)040674886aZeitschrift2gnd 70(DE-588)4006777-40(DE-101)040067777aBiochemie2gnd 70(DE-588)4067488-50(DE-101)040674886aZeitschrift2gnd000(DE-588)4006891-20(DE-101)040068919DsaBiophysik010(DE-588)4067488-50(DE-101)040674886DsaZeitschrift0 5DE-6005DE-600100(DE-588)4006777-40(DE-101)040067777DsaBiochemie110(DE-588)4067488-50(DE-101)040674886DsaZeitschrift1 5DE-6005DE-60008iCD-ROM-Ausg.tBBA on CD-ROMw(DE-600)1434539-0w(DE-101)01928629508iOnline-Ausg.tBiochimica et biophysica actaw(DE-600)1460387-1w(DE-101)01953770000iDarin aufgeg.tBioenergeticsw(DE-600)282711-6w(DE-101)01175997600iDarin aufgeg.tMolecular cell researchw(DE-600)283444-3w(DE-101)01176430900iDarin aufgeg.tReviews on cancerw(DE-600)192424-2w(DE-101)01107018800iDarin aufgeg.tBiochimica et biophysica acta / General subjectsw(DE-600)840755-1w(DE-101)01452479100iDarin aufgeg.tLipids and lipid metabolismw(DE-600)282393-7w(DE-101)01175787600iDarin aufgeg.tBiomembranesw(DE-600)282512-0w(DE-101)01175877500iDarin aufgeg.tReviews on biomembranesw(DE-600)195188-9w(DE-101)01109503200iDarin aufgeg.tProtein structure and molecular enzymologyw(DE-600)283435-2w(DE-101)01176425200iDarin aufgeg.tGene structure and expressionw(DE-600)283437-6w(DE-101)011764260";

    private static final String RECORD ="02602pam a2200529 c 4500001001000000003000700010005001700017007000300034008004100037015003400078016002200112020005800134035002500192040003500217041000800252044001000260084001900270090000600289100006100295245012400356260011200480300002200592490008100614600007900695600007000774600007100844650005400915650007400969650005401043650007401097650005401171650007401225689007701299689005201376689007201428689001901500689006801519689005201587689007201639689001901711689006901730689005201799689007201851689001901923830012301942925000702065\u001E946638705\u001EDE-101\u001E20070429135622.0\u001Etu\u001E960123s2004    gw |||||r|||| 00||||eng  \u001E  \u001Fa05,A03,2104\u001Fz96,N47,0454\u001F2dnb\u001E7 \u001F2DE-101\u001Fa946638705\u001E  \u001Fa0820431125\u001FcPp. : EUR 56.70, sfr 83.00\u001F90-8204-3112-5\u001E  \u001Fa(DE-599)DNB946638705\u001E  \u001Fa1140\u001Fbger\u001FcDE-101\u001Fd9999\u001Ferakwb\u001E  \u001Faeng\u001E  \u001FcXA-DE\u001E  \u001Fa820\u001Fa890\u001F2sdnb\u001E  \u001Fab\u001E1 \u001FaKim, Soonsik\u001F4aut\u001F0(DE-588a)11515454X\u001F0(DE-101)11515454X\u001E10\u001FaColonial and post-colonial discourse in the novels of Yo§am Sang-So§ap, Chinua Achebe and Salman Rushdie\u001FcSoonsik Kim\u001E3 \u001FaNew York\u001FaWashington, D.C./Baltimore\u001FaBern\u001FaFrankfurt am Main\u001FaBerlin\u001FaBrussels\u001FaVienna\u001FaOxford\u001FbLang\u001Fc2004\u001E  \u001FaVI, 214 S.\u001Fc24 cm\u001E1 \u001FaComparative cultures and literatures\u001FvVol. 94201 Literaturverz. S. 197 - 207\u001E17\u001F0(DE-588c)4806527-4\u001F0(DE-101)129612111\u001F2swd\u001FaYo§am, Sang-so§ap\u001Fd1897-1963\u001E17\u001F0(DE-588c)4084672-6\u001F0(DE-101)118646680\u001F2swd\u001FaAchebe, Chinua\u001Fd1930-\u001E17\u001F0(DE-588c)4217069-2\u001F0(DE-101)118873520\u001F2swd\u001FaRushdie, Salman\u001Fd1947-\u001E 7\u001F0(DE-588c)4050479-7\u001F0(DE-101)040504794\u001F2swd\u001FaRoman\u001E 7\u001F0(DE-588c)4681702-5\u001F0(DE-101)964233509\u001F2swd\u001FaPostkolonialismus <Motiv>\u001E 7\u001F0(DE-588c)4050479-7\u001F0(DE-101)040504794\u001F2swd\u001FaRoman\u001E 7\u001F0(DE-588c)4681702-5\u001F0(DE-101)964233509\u001F2swd\u001FaPostkolonialismus <Motiv>\u001E 7\u001F0(DE-588c)4050479-7\u001F0(DE-101)040504794\u001F2swd\u001FaRoman\u001E 7\u001F0(DE-588c)4681702-5\u001F0(DE-101)964233509\u001F2swd\u001FaPostkolonialismus <Motiv>\u001E00\u001FAp\u001F0(DE-588c)4806527-4\u001F0(DE-101)129612111\u001FaYo§am, Sang-so§ap\u001Fd1897-1963\u001E01\u001FAs\u001F0(DE-588c)4050479-7\u001F0(DE-101)040504794\u001FaRoman\u001E02\u001FAs\u001F0(DE-588c)4681702-5\u001F0(DE-101)964233509\u001FaPostkolonialismus <Motiv>\u001E0 \u001F5DE-101\u001F5DE-101\u001E10\u001FAp\u001F0(DE-588c)4084672-6\u001F0(DE-101)118646680\u001FaAchebe, Chinua\u001Fd1930-\u001E11\u001FAs\u001F0(DE-588c)4050479-7\u001F0(DE-101)040504794\u001FaRoman\u001E12\u001FAs\u001F0(DE-588c)4681702-5\u001F0(DE-101)964233509\u001FaPostkolonialismus <Motiv>\u001E1 \u001F5DE-101\u001F5DE-101\u001E20\u001FAp\u001F0(DE-588c)4217069-2\u001F0(DE-101)118873520\u001FaRushdie, Salman\u001Fd1947-\u001E21\u001FAs\u001F0(DE-588c)4050479-7\u001F0(DE-101)040504794\u001FaRoman\u001E22\u001FAs\u001F0(DE-588c)4681702-5\u001F0(DE-101)964233509\u001FaPostkolonialismus <Motiv>\u001E2 \u001F5DE-101\u001F5DE-101\u001E 0\u001FaComparative cultures and literatures\u001FvVol. 94201 Literaturverz. S. 197 - 207\u001Fw(DE-101)025300458\u001Fw(DE-600)2126467-3\u001F919\u001Er \u001Fara\u001E\u001D";

//    private static final String RECORD= "02602pam a2200529 c 4500001001000000003000700010005001700017007000300034008004100037015003400078016002200112020005800134035002500192040003500217041000800252044001000260084001900270090000600289100006100295245012400356260011200480300002200592490008100614600007900695600007000774600007100844650005400915650007400969650005401043650007401097650005401171650007401225689007701299689005201376689007201428689001901500689006801519689005201587689007201639689001901711689006901730689005201799689007201851689001901923830012301942925000702065\u001E946638705\u001EDE-101\u001E20070429135622.0\u001Etu\u001E960123s2004    gw |||||r|||| 00||||eng  \u001E  \u001Fa05,A03,2104\u001Fz96,N47,0454\u001F2dnb\u001E7 \u001F2DE-101\u001Fa946638705\u001E  \u001Fa0820431125\u001FcPp. : EUR 56.70, sfr 83.00\u001F90-8204-3112-5\u001E  \u001Fa(DE-599)DNB946638705\u001E  \u001Fa1140\u001Fbger\u001FcDE-101\u001Fd9999\u001Ferakwb\u001E  \u001Faeng\u001E  \u001FcXA-DE\u001E  \u001Fa820\u001Fa890\u001F2sdnb\u001E  \u001Fab\u001E1 \u001FaKim, Soonsik\u001F4aut\u001F0(DE-588a)11515454X\u001F0(DE-101)11515454X\u001E10\u001FaColonial and post-colonial discourse in the novels of Yo§am Sang-So§ap, Chinua Achebe and Salman Rushdie\u001FcSoonsik Kim\u001E3 \u001FaNew York\u001FaWashington, D.C./Baltimore\u001FaBern\u001FaFrankfurt am Main\u001FaBerlin\u001FaBrussels\u001FaVienna\u001FaOxford\u001FbLang\u001Fc2004\u001E  \u001FaVI, 214 S.\u001Fc24 cm\u001E1 \u001FaComparative cultures and literatures\u001FvVol. 94201 Literaturverz. S. 197 - 207\u001E17\u001F0(DE-588c)4806527-4\u001F0(DE-101)129612111\u001F2swd\u001FaYo§am, Sang-so§ap\u001Fd1897-1963\u001E17\u001F0(DE-588c)4084672-6\u001F0(DE-101)118646680\u001F2swd\u001FaAchebe, Chinua\u001Fd1930-\u001E17\u001F0(DE-588c)4217069-2\u001F0(DE-101)118873520\u001F2swd\u001FaRushdie, Salman\u001Fd1947-\u001E 7\u001F0(DE-588c)4050479-7\u001F0(DE-101)040504794\u001F2swd\u001FaRoman\u001E 7\u001F0(DE-588c)4681702-5\u001F0(DE-101)964233509\u001F2swd\u001FaPostkolonialismus <Motiv>\u001E 7\u001F0(DE-588c)4050479-7\u001F0(DE-101)040504794\u001F2swd\u001FaRoman\u001E 7\u001F0(DE-588c)4681702-5\u001F0(DE-101)964233509\u001F2swd\u001FaPostkolonialismus <Motiv>\u001E 7\u001F0(DE-588c)4050479-7\u001F0(DE-101)040504794\u001F2swd\u001FaRoman\u001E 7\u001F0(DE-588c)4681702-5\u001F0(DE-101)964233509\u001F2swd\u001FaPostkolonialismus <Motiv>\u001E00\u001FAp\u001F0(DE-588c)4806527-4\u001F0(DE-101)129612111\u001FaYo§am, Sang-so§ap\u001Fd1897-1963\u001E01\u001FAs\u001F0(DE-588c)4050479-7\u001F0(DE-101)040504794\u001FaRoman\u001E02\u001FAs\u001F0(DE-588c)4681702-5\u001F0(DE-101)964233509\u001FaPostkolonialismus <Motiv>\u001E0 \u001F5DE-101\u001F5DE-101\u001E10\u001FAp\u001F0(DE-588c)4084672-6\u001F0(DE-101)118646680\u001FaAchebe, Chinua\u001Fd1930-\u001E11\u001FAs\u001F0(DE-588c)4050479-7\u001F0(DE-101)040504794\u001FaRoman\u001E12\u001FAs\u001F0(DE-588c)4681702-5\u001F0(DE-101)964233509\u001FaPostkolonialismus <Motiv>\u001E1 \u001F5DE-101\u001F5DE-101\u001E20\u001FAp\u001F0(DE-588c)4217069-2\u001F0(DE-101)118873520\u001FaRushdie, Salman\u001Fd1947-\u001E21\u001FAs\u001F0(DE-588c)4050479-7\u001F0(DE-101)040504794\u001FaRoman\u001E22\u001FAs\u001F0(DE-588c)4681702-5\u001F0(DE-101)964233509\u001FaPostkolonialismus <Motiv>\u001E2 \u001F5DE-101\u001F5DE-101\u001E 0\u001FaComparative cultures and literatures\u001FvVol. 94201 Literaturverz. S. 197 - 207\u001Fw(DE-101)025300458\u001Fw(DE-600)2126467-3\u001F919\u001Er \u001Fara\u001E\u001D";
    private Marc21Decoder marc21Decoder;
    private Marc21XmlEncoder marcXmlHandlerWrapper;
  //  private static  String RECORD="";

    @Mock
    private ObjectReceiver<String> receiver;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
     //   HttpOpener httpOpener = new HttpOpener();
      //  LineReader lineReader = new LineReader();
 //       httpOpener.setReceiver(lineReader);
       marc21Decoder = new Marc21Decoder();
      /*   Marc21Encoder marc21Encoder = new Marc21Encoder();
        marc21Encoder.setReceiver(receiver);
        marc21Decoder.setReceiver(marc21Encoder);*/
    //    lineReader.setReceiver(marc21Decoder);


        marcXmlHandlerWrapper =new Marc21XmlEncoder();

         marc21Decoder.setReceiver(marcXmlHandlerWrapper);
         marcXmlHandlerWrapper.setReceiver(receiver);

     //   httpOpener.process("https://raw.githubusercontent.com/gbv/Catmandu-Tutorial/master/data/marc.mrc");

    }

    @After
    public void cleanup() {
        marc21Decoder.closeStream();
    }

    @Test
    public void shouldProcessMarc21Record() {
        marc21Decoder.process(RECORD);

      //  final InOrder ordered = inOrder(receiver);
//        ordered.verify(receiver).startRecord(RECORD_ID);
//        ordered.verify(receiver).startEntity("leader");
//        ordered.verify(receiver).literal("status", "n");
//        ordered.verify(receiver).literal("type", "o");
//        ordered.verify(receiver).literal("bibliographicLevel", "a");
//        ordered.verify(receiver).literal("typeOfControl", " ");
//        ordered.verify(receiver).literal("characterCodingScheme", "a");
//        ordered.verify(receiver).literal("encodingLevel", "z");
//        ordered.verify(receiver).literal("catalogingForm", "u");
//        ordered.verify(receiver).literal("multipartLevel", " ");
//        ordered.verify(receiver).endEntity();
//        ordered.verify(receiver).literal("001", RECORD_ID);
//        ordered.verify(receiver).literal("002", CONTROLFIELD_VALUE);
//        ordered.verify(receiver).startEntity("100AB");
//        ordered.verify(receiver).literal("1", "value1");
//        ordered.verify(receiver).endEntity();
//        ordered.verify(receiver).startEntity("200CD");
//        ordered.verify(receiver).literal("2", "value2");
//        ordered.verify(receiver).literal("3", "value3");
//        ordered.verify(receiver).endEntity();
//        ordered.verify(receiver).endRecord();
    }

    @Test
    public void shouldIgnoreEmptyRecords() {
        marc21Decoder.process("");
        verifyZeroInteractions(receiver);
    }

    @Test(expected = FormatException.class)
    public void shouldThrowFormatExceptionIfRecordIsNotMarc21() {
        marc21Decoder.process("00026RIMPL1100024SYS3330" + FIELD_SEPARATOR
                + RECORD_SEPARATOR);
    }

    @Test(expected = FormatException.class)
    public void shouldThrowFormatExceptionIfRecordIsTooShort() {
        marc21Decoder.process("00005");
    }

}
