package org.metafacture.biblio;

import org.junit.Test;
import org.metafacture.framework.ObjectReceiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class SruOpenerTest {

    private StringBuilder resultCollector = new StringBuilder();
    private int resultCollectorsResetStreamCount;


    @Test
    public void test(){
        SruOpener    sruOpener = new SruOpener();
        sruOpener.setReceiver(new ObjectReceiver<Reader> () {

            @Override
            public void process(final Reader obj) {
                BufferedReader in = new BufferedReader(obj);
                String line = null;
                StringBuilder rslt = new StringBuilder();
                while (true) {
                    try {
                        if (!((line = in.readLine()) != null)) break;
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    rslt.append(line);
                }
                System.out.println(rslt.toString());
                resultCollector.append(obj);
            }

            @Override
            public void resetStream() {
                ++resultCollectorsResetStreamCount;
            }

            @Override
            public void closeStream() {

            }
        });

       // sruOpener.setQuery("dnb.isil%3DDE-Sol1");
        sruOpener.setQuery("WVN%3D24A05");
        sruOpener.setRecordSchema("MARC21plus-xml");
        sruOpener.setVersion("1.1");
        sruOpener.setStartRecord("1890");
        sruOpener.process("https://services.dnb.de/sru/dnb");
System.out.println(resultCollector.toString());
        System.out.println(resultCollector.toString());
    }
}
