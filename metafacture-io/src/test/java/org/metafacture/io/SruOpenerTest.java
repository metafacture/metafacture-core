package org.metafacture.io;

import org.junit.Test;

public class SruOpenerTest {

    private StringBuilder resultCollector = new StringBuilder();
    private int resultCollectorsResetStreamCount;


    @Test
    public void test(){
        SruOpener    sruOpener = new SruOpener();
        RecordReader recordReader = new RecordReader();
        recordReader.setReceiver(new ObjectStdoutWriter<String>());
        sruOpener.setReceiver(recordReader);// {


           /* @Override
            public void process(final XmlReceiver obj) {
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
                }*/
      /*          StreamLiteralFormatter streamLiteralFormatter = new StreamLiteralFormatter();
                ObjectStdoutWriter<String> objectStdoutWriter = new ObjectStdoutWriter<String>();
                XmlElementSplitter xmlElementSplitter = new XmlElementSplitter();
                streamLiteralFormatter.setReceiver(objectStdoutWriter);
                xmlElementSplitter.setReceiver(streamLiteralFormatter);
                xmlDecoder.setReceiver(xmlElementSplitter);*/
             //   System.out.println(rslt.toString());
            //    resultCollector.append(obj);
            //}

        sruOpener.setQuery("dnb.isil%3DDE-Sol1");
      //  sruOpener.setQuery("WVN%3D24A05");
        sruOpener.setRecordSchema("MARC21plus-xml");
        sruOpener.setVersion("1.1");
        sruOpener.setStartRecord("3029");
        sruOpener.setMaximumRecords("5");
        sruOpener.setTotal("6");
      //  sruOpener.process("https://services.dnb.de/sru/dnb");
        sruOpener.process("https://services.dnb.de/sru/zdb");
       // sruOpener.process("https://amsquery.stadt-zuerich.ch/sru/");

//        System.out.println(resultCollector.toString());
    }
}
