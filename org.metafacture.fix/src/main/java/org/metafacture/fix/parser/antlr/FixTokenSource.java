package org.metafacture.fix.parser.antlr;

import org.metafacture.fix.parser.antlr.internal.InternalFixParser;

import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.eclipse.xtext.parser.antlr.AbstractIndentationTokenSource;

public class FixTokenSource extends AbstractIndentationTokenSource {

    public FixTokenSource(final TokenSource delegate) {
        super(delegate);
    }

    @Override
    protected boolean shouldSplitTokenImpl(final Token token) {
        return token.getType() == InternalFixParser.RULE_WS;
    }

    @Override
    protected int getBeginTokenType() {
        return InternalFixParser.RULE_BEGIN;
    }

    @Override
    protected int getEndTokenType() {
        return InternalFixParser.RULE_END;
    }

}
