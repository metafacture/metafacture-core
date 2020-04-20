# Module to process bibliopgraphic formats/serializations
Bibliographic records come in a great variety of formats and serializations. This is a list of which of these can be already processed by metafacture.

## MAB
### decoder/reader/handler
- *AlephMabXmlHandler*: An Aleph-MAB-XML reader
- *AseqDecoder*: Parses a raw Aseq stream
- *MabDecoder*: Parses a raw Mab2 stream
### encoder
- not supported

## MARC
### decoder/reader/handler
- *Marc21Decoder*: Encodes a stream in MARC21 format
- *MarcXmlHandler*: A marc xml reader
- *ComarcXmlHandler*: A ComarcXML handler
### encoder
- *Marc21Encoder*: Decodes MARC 21 records
- *MarcXmlEncoder*: Encodes a stream into MARCXML

## Pica
### decoder/reader/handler
- *PicaXmlHandler*: A pica xml handler
- *PicaDecoder*:  Parses non-normalized and normalized pica+ records
### encoder
- *PicaEncoder*: Encodes an event stream in pica+ format
