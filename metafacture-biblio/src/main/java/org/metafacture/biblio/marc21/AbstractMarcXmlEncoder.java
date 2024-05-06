package org.metafacture.biblio.marc21;

import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.helpers.DefaultStreamPipe;

public abstract class AbstractMarcXmlEncoder extends DefaultStreamPipe<ObjectReceiver<String>> implements MarcXmlEncoderInterface {

    protected void onResetStream() {
    }
}
