package org.metafacture.biblio.marc21;

import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.helpers.DefaultStreamPipe;

public abstract class MarcXmlEncoderAbstract extends DefaultStreamPipe<ObjectReceiver<String>> implements MarcXmlEncoderInterface {
    public void onResetStream(){
    }
}
