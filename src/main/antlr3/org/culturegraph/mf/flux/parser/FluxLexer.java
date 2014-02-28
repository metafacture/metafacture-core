// $ANTLR 3.4 /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g 2014-02-28 19:33:56

package org.culturegraph.mf.flux.parser;
import org.apache.commons.lang.StringEscapeUtils;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class FluxLexer extends Lexer {
    public static final int EOF=-1;
    public static final int T__24=24;
    public static final int T__25=25;
    public static final int T__26=26;
    public static final int T__27=27;
    public static final int T__28=28;
    public static final int T__29=29;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int T__33=33;
    public static final int T__34=34;
    public static final int ARG=4;
    public static final int ASSIGN=5;
    public static final int DEFAULT=6;
    public static final int Digit=7;
    public static final int EscapeSequence=8;
    public static final int HexDigit=9;
    public static final int Identifier=10;
    public static final int LINE_COMMENT=11;
    public static final int Letter=12;
    public static final int OctalEscape=13;
    public static final int QualifiedName=14;
    public static final int SUBFLOW=15;
    public static final int StartString=16;
    public static final int StdIn=17;
    public static final int StringLiteral=18;
    public static final int TEE=19;
    public static final int UnicodeEscape=20;
    public static final int VarRef=21;
    public static final int WS=22;
    public static final int Wormhole=23;

    // delegates
    // delegators
    public Lexer[] getDelegates() {
        return new Lexer[] {};
    }

    public FluxLexer() {} 
    public FluxLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public FluxLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
    }
    public String getGrammarFileName() { return "/home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g"; }

    // $ANTLR start "T__24"
    public final void mT__24() throws RecognitionException {
        try {
            int _type = T__24;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:12:7: ( '(' )
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:12:9: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__24"

    // $ANTLR start "T__25"
    public final void mT__25() throws RecognitionException {
        try {
            int _type = T__25;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:13:7: ( ')' )
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:13:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__25"

    // $ANTLR start "T__26"
    public final void mT__26() throws RecognitionException {
        try {
            int _type = T__26;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:14:7: ( '+' )
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:14:9: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__26"

    // $ANTLR start "T__27"
    public final void mT__27() throws RecognitionException {
        try {
            int _type = T__27;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:15:7: ( ',' )
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:15:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__27"

    // $ANTLR start "T__28"
    public final void mT__28() throws RecognitionException {
        try {
            int _type = T__28;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:16:7: ( '.' )
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:16:9: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__28"

    // $ANTLR start "T__29"
    public final void mT__29() throws RecognitionException {
        try {
            int _type = T__29;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:17:7: ( ';' )
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:17:9: ';'
            {
            match(';'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__29"

    // $ANTLR start "T__30"
    public final void mT__30() throws RecognitionException {
        try {
            int _type = T__30;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:18:7: ( '=' )
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:18:9: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__30"

    // $ANTLR start "T__31"
    public final void mT__31() throws RecognitionException {
        try {
            int _type = T__31;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:19:7: ( 'default ' )
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:19:9: 'default '
            {
            match("default "); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__31"

    // $ANTLR start "T__32"
    public final void mT__32() throws RecognitionException {
        try {
            int _type = T__32;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:20:7: ( '{' )
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:20:9: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__32"

    // $ANTLR start "T__33"
    public final void mT__33() throws RecognitionException {
        try {
            int _type = T__33;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:21:7: ( '|' )
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:21:9: '|'
            {
            match('|'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__33"

    // $ANTLR start "T__34"
    public final void mT__34() throws RecognitionException {
        try {
            int _type = T__34;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:22:7: ( '}' )
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:22:9: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T__34"

    // $ANTLR start "StdIn"
    public final void mStdIn() throws RecognitionException {
        try {
            int _type = StdIn;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:94:3: ( '>' )
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:95:3: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "StdIn"

    // $ANTLR start "VarRef"
    public final void mVarRef() throws RecognitionException {
        try {
            int _type = VarRef;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:140:3: ( '*' )
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:141:3: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "VarRef"

    // $ANTLR start "Identifier"
    public final void mIdentifier() throws RecognitionException {
        try {
            int _type = Identifier;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:145:3: ( Letter ( Letter | Digit )* )
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:146:3: Letter ( Letter | Digit )*
            {
            mLetter(); 


            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:147:3: ( Letter | Digit )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0=='$'||LA1_0=='-'||(LA1_0 >= '0' && LA1_0 <= '9')||(LA1_0 >= 'A' && LA1_0 <= 'Z')||LA1_0=='_'||(LA1_0 >= 'a' && LA1_0 <= 'z')||(LA1_0 >= '\u00C0' && LA1_0 <= '\u00D6')||(LA1_0 >= '\u00D8' && LA1_0 <= '\u00F6')||(LA1_0 >= '\u00F8' && LA1_0 <= '\u1FFF')||(LA1_0 >= '\u3040' && LA1_0 <= '\u318F')||(LA1_0 >= '\u3300' && LA1_0 <= '\u337F')||(LA1_0 >= '\u3400' && LA1_0 <= '\u3D2D')||(LA1_0 >= '\u4E00' && LA1_0 <= '\u9FFF')||(LA1_0 >= '\uF900' && LA1_0 <= '\uFAFF')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:
            	    {
            	    if ( input.LA(1)=='$'||input.LA(1)=='-'||(input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u3040' && input.LA(1) <= '\u318F')||(input.LA(1) >= '\u3300' && input.LA(1) <= '\u337F')||(input.LA(1) >= '\u3400' && input.LA(1) <= '\u3D2D')||(input.LA(1) >= '\u4E00' && input.LA(1) <= '\u9FFF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFAFF') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "Identifier"

    // $ANTLR start "Wormhole"
    public final void mWormhole() throws RecognitionException {
        try {
            int _type = Wormhole;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            CommonToken id=null;

            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:154:3: ( '@' id= Identifier )
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:155:3: '@' id= Identifier
            {
            match('@'); 

            int idStart193 = getCharIndex();
            int idStartLine193 = getLine();
            int idStartCharPos193 = getCharPositionInLine();
            mIdentifier(); 
            id = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, idStart193, getCharIndex()-1);
            id.setLine(idStartLine193);
            id.setCharPositionInLine(idStartCharPos193);



                                setText(id.getText());
                               

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "Wormhole"

    // $ANTLR start "StringLiteral"
    public final void mStringLiteral() throws RecognitionException {
        try {
            int _type = StringLiteral;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:162:3: ( '\"' ( EscapeSequence |~ ( '\\\\' | '\"' ) )* '\"' )
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:163:3: '\"' ( EscapeSequence |~ ( '\\\\' | '\"' ) )* '\"'
            {
            match('\"'); 

            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:164:3: ( EscapeSequence |~ ( '\\\\' | '\"' ) )*
            loop2:
            do {
                int alt2=3;
                int LA2_0 = input.LA(1);

                if ( (LA2_0=='\\') ) {
                    alt2=1;
                }
                else if ( ((LA2_0 >= '\u0000' && LA2_0 <= '!')||(LA2_0 >= '#' && LA2_0 <= '[')||(LA2_0 >= ']' && LA2_0 <= '\uFFFF')) ) {
                    alt2=2;
                }


                switch (alt2) {
            	case 1 :
            	    // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:165:5: EscapeSequence
            	    {
            	    mEscapeSequence(); 


            	    }
            	    break;
            	case 2 :
            	    // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:167:5: ~ ( '\\\\' | '\"' )
            	    {
            	    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '!')||(input.LA(1) >= '#' && input.LA(1) <= '[')||(input.LA(1) >= ']' && input.LA(1) <= '\uFFFF') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            match('\"'); 


                  // strip the quotes from the resulting token and unescape
                  setText(StringEscapeUtils.unescapeJava(getText().substring(1,
                  		getText().length() - 1)));
                 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "StringLiteral"

    // $ANTLR start "EscapeSequence"
    public final void mEscapeSequence() throws RecognitionException {
        try {
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:183:3: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' ) | UnicodeEscape | OctalEscape )
            int alt3=3;
            int LA3_0 = input.LA(1);

            if ( (LA3_0=='\\') ) {
                switch ( input.LA(2) ) {
                case '\"':
                case '\'':
                case '\\':
                case 'b':
                case 'f':
                case 'n':
                case 'r':
                case 't':
                    {
                    alt3=1;
                    }
                    break;
                case 'u':
                    {
                    alt3=2;
                    }
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                    {
                    alt3=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 3, 1, input);

                    throw nvae;

                }

            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;

            }
            switch (alt3) {
                case 1 :
                    // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:184:3: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' )
                    {
                    match('\\'); 

                    if ( input.LA(1)=='\"'||input.LA(1)=='\''||input.LA(1)=='\\'||input.LA(1)=='b'||input.LA(1)=='f'||input.LA(1)=='n'||input.LA(1)=='r'||input.LA(1)=='t' ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    }
                    break;
                case 2 :
                    // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:195:5: UnicodeEscape
                    {
                    mUnicodeEscape(); 


                    }
                    break;
                case 3 :
                    // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:196:5: OctalEscape
                    {
                    mOctalEscape(); 


                    }
                    break;

            }

        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "EscapeSequence"

    // $ANTLR start "OctalEscape"
    public final void mOctalEscape() throws RecognitionException {
        try {
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:201:3: ( '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) ( '0' .. '7' ) | '\\\\' ( '0' .. '7' ) )
            int alt4=3;
            int LA4_0 = input.LA(1);

            if ( (LA4_0=='\\') ) {
                int LA4_1 = input.LA(2);

                if ( ((LA4_1 >= '0' && LA4_1 <= '3')) ) {
                    int LA4_2 = input.LA(3);

                    if ( ((LA4_2 >= '0' && LA4_2 <= '7')) ) {
                        int LA4_4 = input.LA(4);

                        if ( ((LA4_4 >= '0' && LA4_4 <= '7')) ) {
                            alt4=1;
                        }
                        else {
                            alt4=2;
                        }
                    }
                    else {
                        alt4=3;
                    }
                }
                else if ( ((LA4_1 >= '4' && LA4_1 <= '7')) ) {
                    int LA4_3 = input.LA(3);

                    if ( ((LA4_3 >= '0' && LA4_3 <= '7')) ) {
                        alt4=2;
                    }
                    else {
                        alt4=3;
                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 1, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;

            }
            switch (alt4) {
                case 1 :
                    // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:202:3: '\\\\' ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    match('\\'); 

                    if ( (input.LA(1) >= '0' && input.LA(1) <= '3') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    }
                    break;
                case 2 :
                    // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:203:5: '\\\\' ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    match('\\'); 

                    if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    }
                    break;
                case 3 :
                    // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:204:5: '\\\\' ( '0' .. '7' )
                    {
                    match('\\'); 

                    if ( (input.LA(1) >= '0' && input.LA(1) <= '7') ) {
                        input.consume();
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;
                    }


                    }
                    break;

            }

        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "OctalEscape"

    // $ANTLR start "UnicodeEscape"
    public final void mUnicodeEscape() throws RecognitionException {
        try {
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:209:3: ( '\\\\' 'u' HexDigit HexDigit HexDigit HexDigit )
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:210:3: '\\\\' 'u' HexDigit HexDigit HexDigit HexDigit
            {
            match('\\'); 

            match('u'); 

            mHexDigit(); 


            mHexDigit(); 


            mHexDigit(); 


            mHexDigit(); 


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "UnicodeEscape"

    // $ANTLR start "HexDigit"
    public final void mHexDigit() throws RecognitionException {
        try {
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:215:3: ( ( '0' .. '9' | 'a' .. 'f' | 'A' .. 'F' ) )
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:
            {
            if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'F')||(input.LA(1) >= 'a' && input.LA(1) <= 'f') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "HexDigit"

    // $ANTLR start "Letter"
    public final void mLetter() throws RecognitionException {
        try {
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:225:3: ( '\\u0024' | '\\u0041' .. '\\u005a' | '\\u005f' | '\\u0061' .. '\\u007a' | '\\u00c0' .. '\\u00d6' | '\\u00d8' .. '\\u00f6' | '\\u00f8' .. '\\u00ff' | '\\u0100' .. '\\u1fff' | '\\u3040' .. '\\u318f' | '\\u3300' .. '\\u337f' | '\\u3400' .. '\\u3d2d' | '\\u4e00' .. '\\u9fff' | '\\uf900' .. '\\ufaff' | '-' )
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:
            {
            if ( input.LA(1)=='$'||input.LA(1)=='-'||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||(input.LA(1) >= '\u00C0' && input.LA(1) <= '\u00D6')||(input.LA(1) >= '\u00D8' && input.LA(1) <= '\u00F6')||(input.LA(1) >= '\u00F8' && input.LA(1) <= '\u1FFF')||(input.LA(1) >= '\u3040' && input.LA(1) <= '\u318F')||(input.LA(1) >= '\u3300' && input.LA(1) <= '\u337F')||(input.LA(1) >= '\u3400' && input.LA(1) <= '\u3D2D')||(input.LA(1) >= '\u4E00' && input.LA(1) <= '\u9FFF')||(input.LA(1) >= '\uF900' && input.LA(1) <= '\uFAFF') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "Letter"

    // $ANTLR start "Digit"
    public final void mDigit() throws RecognitionException {
        try {
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:244:3: ( '0' .. '9' )
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:
            {
            if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "Digit"

    // $ANTLR start "LINE_COMMENT"
    public final void mLINE_COMMENT() throws RecognitionException {
        try {
            int _type = LINE_COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:248:3: ( '//' (~ ( '\\n' | '\\r' ) )* ( ( '\\r' )? '\\n' | EOF ) )
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:249:3: '//' (~ ( '\\n' | '\\r' ) )* ( ( '\\r' )? '\\n' | EOF )
            {
            match("//"); 



            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:250:3: (~ ( '\\n' | '\\r' ) )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( ((LA5_0 >= '\u0000' && LA5_0 <= '\t')||(LA5_0 >= '\u000B' && LA5_0 <= '\f')||(LA5_0 >= '\u000E' && LA5_0 <= '\uFFFF')) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:
            	    {
            	    if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '\uFFFF') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);


            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:254:3: ( ( '\\r' )? '\\n' | EOF )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0=='\n'||LA7_0=='\r') ) {
                alt7=1;
            }
            else {
                alt7=2;
            }
            switch (alt7) {
                case 1 :
                    // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:255:5: ( '\\r' )? '\\n'
                    {
                    // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:255:5: ( '\\r' )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0=='\r') ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:255:5: '\\r'
                            {
                            match('\r'); 

                            }
                            break;

                    }


                    match('\n'); 

                    }
                    break;
                case 2 :
                    // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:256:7: EOF
                    {
                    match(EOF); 


                    }
                    break;

            }



                _channel = HIDDEN;
              

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LINE_COMMENT"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:265:3: ( ( ' ' | '\\r' | '\\t' | '\\u000C' | '\\n' ) )
            // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:266:3: ( ' ' | '\\r' | '\\t' | '\\u000C' | '\\n' )
            {
            if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||(input.LA(1) >= '\f' && input.LA(1) <= '\r')||input.LA(1)==' ' ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }



               _channel = HIDDEN;
              

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "WS"

    public void mTokens() throws RecognitionException {
        // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:1:8: ( T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | StdIn | VarRef | Identifier | Wormhole | StringLiteral | LINE_COMMENT | WS )
        int alt8=18;
        int LA8_0 = input.LA(1);

        if ( (LA8_0=='(') ) {
            alt8=1;
        }
        else if ( (LA8_0==')') ) {
            alt8=2;
        }
        else if ( (LA8_0=='+') ) {
            alt8=3;
        }
        else if ( (LA8_0==',') ) {
            alt8=4;
        }
        else if ( (LA8_0=='.') ) {
            alt8=5;
        }
        else if ( (LA8_0==';') ) {
            alt8=6;
        }
        else if ( (LA8_0=='=') ) {
            alt8=7;
        }
        else if ( (LA8_0=='d') ) {
            int LA8_8 = input.LA(2);

            if ( (LA8_8=='e') ) {
                int LA8_19 = input.LA(3);

                if ( (LA8_19=='f') ) {
                    int LA8_20 = input.LA(4);

                    if ( (LA8_20=='a') ) {
                        int LA8_21 = input.LA(5);

                        if ( (LA8_21=='u') ) {
                            int LA8_22 = input.LA(6);

                            if ( (LA8_22=='l') ) {
                                int LA8_23 = input.LA(7);

                                if ( (LA8_23=='t') ) {
                                    int LA8_24 = input.LA(8);

                                    if ( (LA8_24==' ') ) {
                                        alt8=8;
                                    }
                                    else {
                                        alt8=14;
                                    }
                                }
                                else {
                                    alt8=14;
                                }
                            }
                            else {
                                alt8=14;
                            }
                        }
                        else {
                            alt8=14;
                        }
                    }
                    else {
                        alt8=14;
                    }
                }
                else {
                    alt8=14;
                }
            }
            else {
                alt8=14;
            }
        }
        else if ( (LA8_0=='{') ) {
            alt8=9;
        }
        else if ( (LA8_0=='|') ) {
            alt8=10;
        }
        else if ( (LA8_0=='}') ) {
            alt8=11;
        }
        else if ( (LA8_0=='>') ) {
            alt8=12;
        }
        else if ( (LA8_0=='*') ) {
            alt8=13;
        }
        else if ( (LA8_0=='$'||LA8_0=='-'||(LA8_0 >= 'A' && LA8_0 <= 'Z')||LA8_0=='_'||(LA8_0 >= 'a' && LA8_0 <= 'c')||(LA8_0 >= 'e' && LA8_0 <= 'z')||(LA8_0 >= '\u00C0' && LA8_0 <= '\u00D6')||(LA8_0 >= '\u00D8' && LA8_0 <= '\u00F6')||(LA8_0 >= '\u00F8' && LA8_0 <= '\u1FFF')||(LA8_0 >= '\u3040' && LA8_0 <= '\u318F')||(LA8_0 >= '\u3300' && LA8_0 <= '\u337F')||(LA8_0 >= '\u3400' && LA8_0 <= '\u3D2D')||(LA8_0 >= '\u4E00' && LA8_0 <= '\u9FFF')||(LA8_0 >= '\uF900' && LA8_0 <= '\uFAFF')) ) {
            alt8=14;
        }
        else if ( (LA8_0=='@') ) {
            alt8=15;
        }
        else if ( (LA8_0=='\"') ) {
            alt8=16;
        }
        else if ( (LA8_0=='/') ) {
            alt8=17;
        }
        else if ( ((LA8_0 >= '\t' && LA8_0 <= '\n')||(LA8_0 >= '\f' && LA8_0 <= '\r')||LA8_0==' ') ) {
            alt8=18;
        }
        else {
            NoViableAltException nvae =
                new NoViableAltException("", 8, 0, input);

            throw nvae;

        }
        switch (alt8) {
            case 1 :
                // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:1:10: T__24
                {
                mT__24(); 


                }
                break;
            case 2 :
                // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:1:16: T__25
                {
                mT__25(); 


                }
                break;
            case 3 :
                // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:1:22: T__26
                {
                mT__26(); 


                }
                break;
            case 4 :
                // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:1:28: T__27
                {
                mT__27(); 


                }
                break;
            case 5 :
                // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:1:34: T__28
                {
                mT__28(); 


                }
                break;
            case 6 :
                // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:1:40: T__29
                {
                mT__29(); 


                }
                break;
            case 7 :
                // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:1:46: T__30
                {
                mT__30(); 


                }
                break;
            case 8 :
                // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:1:52: T__31
                {
                mT__31(); 


                }
                break;
            case 9 :
                // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:1:58: T__32
                {
                mT__32(); 


                }
                break;
            case 10 :
                // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:1:64: T__33
                {
                mT__33(); 


                }
                break;
            case 11 :
                // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:1:70: T__34
                {
                mT__34(); 


                }
                break;
            case 12 :
                // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:1:76: StdIn
                {
                mStdIn(); 


                }
                break;
            case 13 :
                // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:1:82: VarRef
                {
                mVarRef(); 


                }
                break;
            case 14 :
                // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:1:89: Identifier
                {
                mIdentifier(); 


                }
                break;
            case 15 :
                // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:1:100: Wormhole
                {
                mWormhole(); 


                }
                break;
            case 16 :
                // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:1:109: StringLiteral
                {
                mStringLiteral(); 


                }
                break;
            case 17 :
                // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:1:123: LINE_COMMENT
                {
                mLINE_COMMENT(); 


                }
                break;
            case 18 :
                // /home/christoph/git/metafacture-core/src/main/antlr3/org/culturegraph/mf/flux/parser/Flux.g:1:136: WS
                {
                mWS(); 


                }
                break;

        }

    }


 

}