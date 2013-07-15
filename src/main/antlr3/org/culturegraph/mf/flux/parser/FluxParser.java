// $ANTLR 3.4 D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g 2013-07-03 10:40:33

package org.culturegraph.mf.flux.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.runtime.tree.*;


@SuppressWarnings({"all", "warnings", "unchecked"})
public class FluxParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ARG", "ASSIGN", "DEFAULT", "Digit", "EscapeSequence", "HexDigit", "Identifier", "LINE_COMMENT", "Letter", "OctalEscape", "QualifiedName", "SUBFLOW", "StartString", "StdIn", "StringLiteral", "TEE", "UnicodeEscape", "VarRef", "WS", "Wormhole", "'('", "')'", "'+'", "','", "'.'", "';'", "'='", "'default '", "'{'", "'|'", "'}'"
    };

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
    public Parser[] getDelegates() {
        return new Parser[] {};
    }

    // delegators


    public FluxParser(TokenStream input) {
        this(input, new RecognizerSharedState());
    }
    public FluxParser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }

protected TreeAdaptor adaptor = new CommonTreeAdaptor();

public void setTreeAdaptor(TreeAdaptor adaptor) {
    this.adaptor = adaptor;
}
public TreeAdaptor getTreeAdaptor() {
    return adaptor;
}
    public String[] getTokenNames() { return FluxParser.tokenNames; }
    public String getGrammarFileName() { return "D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g"; }


    public static class flux_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "flux"
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:43:1: flux : ( varDef )* ( flow )* ;
    public final FluxParser.flux_return flux() throws RecognitionException {
        FluxParser.flux_return retval = new FluxParser.flux_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        FluxParser.varDef_return varDef1 =null;

        FluxParser.flow_return flow2 =null;



        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:44:3: ( ( varDef )* ( flow )* )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:45:3: ( varDef )* ( flow )*
            {
            root_0 = (CommonTree)adaptor.nil();


            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:45:3: ( varDef )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==Identifier) ) {
                    int LA1_2 = input.LA(2);

                    if ( (LA1_2==30) ) {
                        alt1=1;
                    }


                }
                else if ( (LA1_0==31) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:45:3: varDef
            	    {
            	    pushFollow(FOLLOW_varDef_in_flux113);
            	    varDef1=varDef();

            	    state._fsp--;

            	    adaptor.addChild(root_0, varDef1.getTree());

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:45:11: ( flow )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==Identifier||(LA2_0 >= StdIn && LA2_0 <= StringLiteral)||LA2_0==Wormhole) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:45:11: flow
            	    {
            	    pushFollow(FOLLOW_flow_in_flux116);
            	    flow2=flow();

            	    state._fsp--;

            	    adaptor.addChild(root_0, flow2.getTree());

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "flux"


    public static class varDef_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "varDef"
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:48:1: varDef : ( 'default ' i= Identifier '=' exp ';' -> ^( DEFAULT Identifier exp ) |i= Identifier '=' exp ';' -> ^( ASSIGN Identifier exp ) );
    public final FluxParser.varDef_return varDef() throws RecognitionException {
        FluxParser.varDef_return retval = new FluxParser.varDef_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token i=null;
        Token string_literal3=null;
        Token char_literal4=null;
        Token char_literal6=null;
        Token char_literal7=null;
        Token char_literal9=null;
        FluxParser.exp_return exp5 =null;

        FluxParser.exp_return exp8 =null;


        CommonTree i_tree=null;
        CommonTree string_literal3_tree=null;
        CommonTree char_literal4_tree=null;
        CommonTree char_literal6_tree=null;
        CommonTree char_literal7_tree=null;
        CommonTree char_literal9_tree=null;
        RewriteRuleTokenStream stream_30=new RewriteRuleTokenStream(adaptor,"token 30");
        RewriteRuleTokenStream stream_31=new RewriteRuleTokenStream(adaptor,"token 31");
        RewriteRuleTokenStream stream_Identifier=new RewriteRuleTokenStream(adaptor,"token Identifier");
        RewriteRuleTokenStream stream_29=new RewriteRuleTokenStream(adaptor,"token 29");
        RewriteRuleSubtreeStream stream_exp=new RewriteRuleSubtreeStream(adaptor,"rule exp");
        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:49:3: ( 'default ' i= Identifier '=' exp ';' -> ^( DEFAULT Identifier exp ) |i= Identifier '=' exp ';' -> ^( ASSIGN Identifier exp ) )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==31) ) {
                alt3=1;
            }
            else if ( (LA3_0==Identifier) ) {
                alt3=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;

            }
            switch (alt3) {
                case 1 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:50:3: 'default ' i= Identifier '=' exp ';'
                    {
                    string_literal3=(Token)match(input,31,FOLLOW_31_in_varDef132);  
                    stream_31.add(string_literal3);


                    i=(Token)match(input,Identifier,FOLLOW_Identifier_in_varDef136);  
                    stream_Identifier.add(i);


                    char_literal4=(Token)match(input,30,FOLLOW_30_in_varDef138);  
                    stream_30.add(char_literal4);


                    pushFollow(FOLLOW_exp_in_varDef140);
                    exp5=exp();

                    state._fsp--;

                    stream_exp.add(exp5.getTree());

                    char_literal6=(Token)match(input,29,FOLLOW_29_in_varDef142);  
                    stream_29.add(char_literal6);


                    // AST REWRITE
                    // elements: Identifier, exp
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 51:5: -> ^( DEFAULT Identifier exp )
                    {
                        // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:52:7: ^( DEFAULT Identifier exp )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(
                        (CommonTree)adaptor.create(DEFAULT, "DEFAULT")
                        , root_1);

                        adaptor.addChild(root_1, 
                        stream_Identifier.nextNode()
                        );

                        adaptor.addChild(root_1, stream_exp.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;

                    }
                    break;
                case 2 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:53:5: i= Identifier '=' exp ';'
                    {
                    i=(Token)match(input,Identifier,FOLLOW_Identifier_in_varDef170);  
                    stream_Identifier.add(i);


                    char_literal7=(Token)match(input,30,FOLLOW_30_in_varDef172);  
                    stream_30.add(char_literal7);


                    pushFollow(FOLLOW_exp_in_varDef174);
                    exp8=exp();

                    state._fsp--;

                    stream_exp.add(exp8.getTree());

                    char_literal9=(Token)match(input,29,FOLLOW_29_in_varDef176);  
                    stream_29.add(char_literal9);


                    // AST REWRITE
                    // elements: exp, Identifier
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 54:5: -> ^( ASSIGN Identifier exp )
                    {
                        // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:55:7: ^( ASSIGN Identifier exp )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(
                        (CommonTree)adaptor.create(ASSIGN, "ASSIGN")
                        , root_1);

                        adaptor.addChild(root_1, 
                        stream_Identifier.nextNode()
                        );

                        adaptor.addChild(root_1, stream_exp.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }


                    retval.tree = root_0;

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "varDef"


    public static class flow_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "flow"
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:58:1: flow : ( StdIn | exp | Wormhole ) '|' ! flowtail ( '|' ! Wormhole )? ';' !;
    public final FluxParser.flow_return flow() throws RecognitionException {
        FluxParser.flow_return retval = new FluxParser.flow_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token StdIn10=null;
        Token Wormhole12=null;
        Token char_literal13=null;
        Token char_literal15=null;
        Token Wormhole16=null;
        Token char_literal17=null;
        FluxParser.exp_return exp11 =null;

        FluxParser.flowtail_return flowtail14 =null;


        CommonTree StdIn10_tree=null;
        CommonTree Wormhole12_tree=null;
        CommonTree char_literal13_tree=null;
        CommonTree char_literal15_tree=null;
        CommonTree Wormhole16_tree=null;
        CommonTree char_literal17_tree=null;

        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:59:3: ( ( StdIn | exp | Wormhole ) '|' ! flowtail ( '|' ! Wormhole )? ';' !)
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:60:3: ( StdIn | exp | Wormhole ) '|' ! flowtail ( '|' ! Wormhole )? ';' !
            {
            root_0 = (CommonTree)adaptor.nil();


            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:60:3: ( StdIn | exp | Wormhole )
            int alt4=3;
            switch ( input.LA(1) ) {
            case StdIn:
                {
                alt4=1;
                }
                break;
            case Identifier:
            case StringLiteral:
                {
                alt4=2;
                }
                break;
            case Wormhole:
                {
                alt4=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;

            }

            switch (alt4) {
                case 1 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:61:5: StdIn
                    {
                    StdIn10=(Token)match(input,StdIn,FOLLOW_StdIn_in_flow217); 
                    StdIn10_tree = 
                    (CommonTree)adaptor.create(StdIn10)
                    ;
                    adaptor.addChild(root_0, StdIn10_tree);


                    }
                    break;
                case 2 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:62:7: exp
                    {
                    pushFollow(FOLLOW_exp_in_flow225);
                    exp11=exp();

                    state._fsp--;

                    adaptor.addChild(root_0, exp11.getTree());

                    }
                    break;
                case 3 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:63:7: Wormhole
                    {
                    Wormhole12=(Token)match(input,Wormhole,FOLLOW_Wormhole_in_flow233); 
                    Wormhole12_tree = 
                    (CommonTree)adaptor.create(Wormhole12)
                    ;
                    adaptor.addChild(root_0, Wormhole12_tree);


                    }
                    break;

            }


            char_literal13=(Token)match(input,33,FOLLOW_33_in_flow241); 

            pushFollow(FOLLOW_flowtail_in_flow244);
            flowtail14=flowtail();

            state._fsp--;

            adaptor.addChild(root_0, flowtail14.getTree());

            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:65:17: ( '|' ! Wormhole )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==33) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:65:18: '|' ! Wormhole
                    {
                    char_literal15=(Token)match(input,33,FOLLOW_33_in_flow247); 

                    Wormhole16=(Token)match(input,Wormhole,FOLLOW_Wormhole_in_flow250); 
                    Wormhole16_tree = 
                    (CommonTree)adaptor.create(Wormhole16)
                    ;
                    adaptor.addChild(root_0, Wormhole16_tree);


                    }
                    break;

            }


            char_literal17=(Token)match(input,29,FOLLOW_29_in_flow254); 

            }

            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "flow"


    public static class tee_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "tee"
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:68:1: tee : ( '{' flowtail '}' )+ -> ^( TEE ( ^( SUBFLOW flowtail ) )+ ) ;
    public final FluxParser.tee_return tee() throws RecognitionException {
        FluxParser.tee_return retval = new FluxParser.tee_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token char_literal18=null;
        Token char_literal20=null;
        FluxParser.flowtail_return flowtail19 =null;


        CommonTree char_literal18_tree=null;
        CommonTree char_literal20_tree=null;
        RewriteRuleTokenStream stream_32=new RewriteRuleTokenStream(adaptor,"token 32");
        RewriteRuleTokenStream stream_34=new RewriteRuleTokenStream(adaptor,"token 34");
        RewriteRuleSubtreeStream stream_flowtail=new RewriteRuleSubtreeStream(adaptor,"rule flowtail");
        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:69:3: ( ( '{' flowtail '}' )+ -> ^( TEE ( ^( SUBFLOW flowtail ) )+ ) )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:70:3: ( '{' flowtail '}' )+
            {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:70:3: ( '{' flowtail '}' )+
            int cnt6=0;
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0==32) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:70:4: '{' flowtail '}'
            	    {
            	    char_literal18=(Token)match(input,32,FOLLOW_32_in_tee271);  
            	    stream_32.add(char_literal18);


            	    pushFollow(FOLLOW_flowtail_in_tee273);
            	    flowtail19=flowtail();

            	    state._fsp--;

            	    stream_flowtail.add(flowtail19.getTree());

            	    char_literal20=(Token)match(input,34,FOLLOW_34_in_tee275);  
            	    stream_34.add(char_literal20);


            	    }
            	    break;

            	default :
            	    if ( cnt6 >= 1 ) break loop6;
                        EarlyExitException eee =
                            new EarlyExitException(6, input);
                        throw eee;
                }
                cnt6++;
            } while (true);


            // AST REWRITE
            // elements: flowtail
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 71:5: -> ^( TEE ( ^( SUBFLOW flowtail ) )+ )
            {
                // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:72:7: ^( TEE ( ^( SUBFLOW flowtail ) )+ )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(
                (CommonTree)adaptor.create(TEE, "TEE")
                , root_1);

                if ( !(stream_flowtail.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_flowtail.hasNext() ) {
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:74:9: ^( SUBFLOW flowtail )
                    {
                    CommonTree root_2 = (CommonTree)adaptor.nil();
                    root_2 = (CommonTree)adaptor.becomeRoot(
                    (CommonTree)adaptor.create(SUBFLOW, "SUBFLOW")
                    , root_2);

                    adaptor.addChild(root_2, stream_flowtail.nextTree());

                    adaptor.addChild(root_1, root_2);
                    }

                }
                stream_flowtail.reset();

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "tee"


    public static class flowtail_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "flowtail"
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:78:1: flowtail : ( pipe | tee ) ( '|' ! ( pipe | tee ) )* ;
    public final FluxParser.flowtail_return flowtail() throws RecognitionException {
        FluxParser.flowtail_return retval = new FluxParser.flowtail_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token char_literal23=null;
        FluxParser.pipe_return pipe21 =null;

        FluxParser.tee_return tee22 =null;

        FluxParser.pipe_return pipe24 =null;

        FluxParser.tee_return tee25 =null;


        CommonTree char_literal23_tree=null;

        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:79:3: ( ( pipe | tee ) ( '|' ! ( pipe | tee ) )* )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:80:3: ( pipe | tee ) ( '|' ! ( pipe | tee ) )*
            {
            root_0 = (CommonTree)adaptor.nil();


            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:80:3: ( pipe | tee )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==Identifier) ) {
                alt7=1;
            }
            else if ( (LA7_0==32) ) {
                alt7=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;

            }
            switch (alt7) {
                case 1 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:81:5: pipe
                    {
                    pushFollow(FOLLOW_pipe_in_flowtail346);
                    pipe21=pipe();

                    state._fsp--;

                    adaptor.addChild(root_0, pipe21.getTree());

                    }
                    break;
                case 2 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:82:7: tee
                    {
                    pushFollow(FOLLOW_tee_in_flowtail354);
                    tee22=tee();

                    state._fsp--;

                    adaptor.addChild(root_0, tee22.getTree());

                    }
                    break;

            }


            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:84:3: ( '|' ! ( pipe | tee ) )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==33) ) {
                    int LA9_1 = input.LA(2);

                    if ( (LA9_1==Identifier||LA9_1==32) ) {
                        alt9=1;
                    }


                }


                switch (alt9) {
            	case 1 :
            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:85:5: '|' ! ( pipe | tee )
            	    {
            	    char_literal23=(Token)match(input,33,FOLLOW_33_in_flowtail368); 

            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:86:5: ( pipe | tee )
            	    int alt8=2;
            	    int LA8_0 = input.LA(1);

            	    if ( (LA8_0==Identifier) ) {
            	        alt8=1;
            	    }
            	    else if ( (LA8_0==32) ) {
            	        alt8=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 8, 0, input);

            	        throw nvae;

            	    }
            	    switch (alt8) {
            	        case 1 :
            	            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:87:7: pipe
            	            {
            	            pushFollow(FOLLOW_pipe_in_flowtail383);
            	            pipe24=pipe();

            	            state._fsp--;

            	            adaptor.addChild(root_0, pipe24.getTree());

            	            }
            	            break;
            	        case 2 :
            	            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:88:9: tee
            	            {
            	            pushFollow(FOLLOW_tee_in_flowtail393);
            	            tee25=tee();

            	            state._fsp--;

            	            adaptor.addChild(root_0, tee25.getTree());

            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "flowtail"


    public static class pipe_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "pipe"
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:98:1: pipe : qualifiedName ( '(' pipeArgs ')' )? -> ^( QualifiedName[$qualifiedName.text] ( pipeArgs )* ) ;
    public final FluxParser.pipe_return pipe() throws RecognitionException {
        FluxParser.pipe_return retval = new FluxParser.pipe_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token char_literal27=null;
        Token char_literal29=null;
        FluxParser.qualifiedName_return qualifiedName26 =null;

        FluxParser.pipeArgs_return pipeArgs28 =null;


        CommonTree char_literal27_tree=null;
        CommonTree char_literal29_tree=null;
        RewriteRuleTokenStream stream_24=new RewriteRuleTokenStream(adaptor,"token 24");
        RewriteRuleTokenStream stream_25=new RewriteRuleTokenStream(adaptor,"token 25");
        RewriteRuleSubtreeStream stream_qualifiedName=new RewriteRuleSubtreeStream(adaptor,"rule qualifiedName");
        RewriteRuleSubtreeStream stream_pipeArgs=new RewriteRuleSubtreeStream(adaptor,"rule pipeArgs");
        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:99:3: ( qualifiedName ( '(' pipeArgs ')' )? -> ^( QualifiedName[$qualifiedName.text] ( pipeArgs )* ) )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:100:3: qualifiedName ( '(' pipeArgs ')' )?
            {
            pushFollow(FOLLOW_qualifiedName_in_pipe434);
            qualifiedName26=qualifiedName();

            state._fsp--;

            stream_qualifiedName.add(qualifiedName26.getTree());

            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:100:17: ( '(' pipeArgs ')' )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==24) ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:100:18: '(' pipeArgs ')'
                    {
                    char_literal27=(Token)match(input,24,FOLLOW_24_in_pipe437);  
                    stream_24.add(char_literal27);


                    pushFollow(FOLLOW_pipeArgs_in_pipe439);
                    pipeArgs28=pipeArgs();

                    state._fsp--;

                    stream_pipeArgs.add(pipeArgs28.getTree());

                    char_literal29=(Token)match(input,25,FOLLOW_25_in_pipe441);  
                    stream_25.add(char_literal29);


                    }
                    break;

            }


            // AST REWRITE
            // elements: pipeArgs
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 101:5: -> ^( QualifiedName[$qualifiedName.text] ( pipeArgs )* )
            {
                // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:102:7: ^( QualifiedName[$qualifiedName.text] ( pipeArgs )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(
                (CommonTree)adaptor.create(QualifiedName, (qualifiedName26!=null?input.toString(qualifiedName26.start,qualifiedName26.stop):null))
                , root_1);

                // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:102:44: ( pipeArgs )*
                while ( stream_pipeArgs.hasNext() ) {
                    adaptor.addChild(root_1, stream_pipeArgs.nextTree());

                }
                stream_pipeArgs.reset();

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "pipe"


    public static class exp_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "exp"
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:105:1: exp : atom ( '+' ^ atom )* ;
    public final FluxParser.exp_return exp() throws RecognitionException {
        FluxParser.exp_return retval = new FluxParser.exp_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token char_literal31=null;
        FluxParser.atom_return atom30 =null;

        FluxParser.atom_return atom32 =null;


        CommonTree char_literal31_tree=null;

        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:106:3: ( atom ( '+' ^ atom )* )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:107:3: atom ( '+' ^ atom )*
            {
            root_0 = (CommonTree)adaptor.nil();


            pushFollow(FOLLOW_atom_in_exp478);
            atom30=atom();

            state._fsp--;

            adaptor.addChild(root_0, atom30.getTree());

            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:107:8: ( '+' ^ atom )*
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( (LA11_0==26) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:107:9: '+' ^ atom
            	    {
            	    char_literal31=(Token)match(input,26,FOLLOW_26_in_exp481); 
            	    char_literal31_tree = 
            	    (CommonTree)adaptor.create(char_literal31)
            	    ;
            	    root_0 = (CommonTree)adaptor.becomeRoot(char_literal31_tree, root_0);


            	    pushFollow(FOLLOW_atom_in_exp484);
            	    atom32=atom();

            	    state._fsp--;

            	    adaptor.addChild(root_0, atom32.getTree());

            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "exp"


    public static class atom_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "atom"
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:110:1: atom : ( StringLiteral | Identifier );
    public final FluxParser.atom_return atom() throws RecognitionException {
        FluxParser.atom_return retval = new FluxParser.atom_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token set33=null;

        CommonTree set33_tree=null;

        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:111:3: ( StringLiteral | Identifier )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:
            {
            root_0 = (CommonTree)adaptor.nil();


            set33=(Token)input.LT(1);

            if ( input.LA(1)==Identifier||input.LA(1)==StringLiteral ) {
                input.consume();
                adaptor.addChild(root_0, 
                (CommonTree)adaptor.create(set33)
                );
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "atom"


    public static class pipeArgs_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "pipeArgs"
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:116:1: pipeArgs : ( exp | exp ',' ! VarRef | VarRef | namedArg ) ( ',' ! namedArg )* ;
    public final FluxParser.pipeArgs_return pipeArgs() throws RecognitionException {
        FluxParser.pipeArgs_return retval = new FluxParser.pipeArgs_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token char_literal36=null;
        Token VarRef37=null;
        Token VarRef38=null;
        Token char_literal40=null;
        FluxParser.exp_return exp34 =null;

        FluxParser.exp_return exp35 =null;

        FluxParser.namedArg_return namedArg39 =null;

        FluxParser.namedArg_return namedArg41 =null;


        CommonTree char_literal36_tree=null;
        CommonTree VarRef37_tree=null;
        CommonTree VarRef38_tree=null;
        CommonTree char_literal40_tree=null;

        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:117:3: ( ( exp | exp ',' ! VarRef | VarRef | namedArg ) ( ',' ! namedArg )* )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:118:3: ( exp | exp ',' ! VarRef | VarRef | namedArg ) ( ',' ! namedArg )*
            {
            root_0 = (CommonTree)adaptor.nil();


            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:118:3: ( exp | exp ',' ! VarRef | VarRef | namedArg )
            int alt12=4;
            alt12 = dfa12.predict(input);
            switch (alt12) {
                case 1 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:119:5: exp
                    {
                    pushFollow(FOLLOW_exp_in_pipeArgs528);
                    exp34=exp();

                    state._fsp--;

                    adaptor.addChild(root_0, exp34.getTree());

                    }
                    break;
                case 2 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:120:7: exp ',' ! VarRef
                    {
                    pushFollow(FOLLOW_exp_in_pipeArgs536);
                    exp35=exp();

                    state._fsp--;

                    adaptor.addChild(root_0, exp35.getTree());

                    char_literal36=(Token)match(input,27,FOLLOW_27_in_pipeArgs538); 

                    VarRef37=(Token)match(input,VarRef,FOLLOW_VarRef_in_pipeArgs541); 
                    VarRef37_tree = 
                    (CommonTree)adaptor.create(VarRef37)
                    ;
                    adaptor.addChild(root_0, VarRef37_tree);


                    }
                    break;
                case 3 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:121:7: VarRef
                    {
                    VarRef38=(Token)match(input,VarRef,FOLLOW_VarRef_in_pipeArgs549); 
                    VarRef38_tree = 
                    (CommonTree)adaptor.create(VarRef38)
                    ;
                    adaptor.addChild(root_0, VarRef38_tree);


                    }
                    break;
                case 4 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:122:7: namedArg
                    {
                    pushFollow(FOLLOW_namedArg_in_pipeArgs557);
                    namedArg39=namedArg();

                    state._fsp--;

                    adaptor.addChild(root_0, namedArg39.getTree());

                    }
                    break;

            }


            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:124:3: ( ',' ! namedArg )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( (LA13_0==27) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:124:4: ',' ! namedArg
            	    {
            	    char_literal40=(Token)match(input,27,FOLLOW_27_in_pipeArgs566); 

            	    pushFollow(FOLLOW_namedArg_in_pipeArgs569);
            	    namedArg41=namedArg();

            	    state._fsp--;

            	    adaptor.addChild(root_0, namedArg41.getTree());

            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "pipeArgs"


    public static class namedArg_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "namedArg"
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:127:1: namedArg : Identifier '=' exp -> ^( ARG Identifier exp ) ;
    public final FluxParser.namedArg_return namedArg() throws RecognitionException {
        FluxParser.namedArg_return retval = new FluxParser.namedArg_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token Identifier42=null;
        Token char_literal43=null;
        FluxParser.exp_return exp44 =null;


        CommonTree Identifier42_tree=null;
        CommonTree char_literal43_tree=null;
        RewriteRuleTokenStream stream_30=new RewriteRuleTokenStream(adaptor,"token 30");
        RewriteRuleTokenStream stream_Identifier=new RewriteRuleTokenStream(adaptor,"token Identifier");
        RewriteRuleSubtreeStream stream_exp=new RewriteRuleSubtreeStream(adaptor,"rule exp");
        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:128:3: ( Identifier '=' exp -> ^( ARG Identifier exp ) )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:129:3: Identifier '=' exp
            {
            Identifier42=(Token)match(input,Identifier,FOLLOW_Identifier_in_namedArg586);  
            stream_Identifier.add(Identifier42);


            char_literal43=(Token)match(input,30,FOLLOW_30_in_namedArg588);  
            stream_30.add(char_literal43);


            pushFollow(FOLLOW_exp_in_namedArg590);
            exp44=exp();

            state._fsp--;

            stream_exp.add(exp44.getTree());

            // AST REWRITE
            // elements: exp, Identifier
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 130:5: -> ^( ARG Identifier exp )
            {
                // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:131:7: ^( ARG Identifier exp )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(
                (CommonTree)adaptor.create(ARG, "ARG")
                , root_1);

                adaptor.addChild(root_1, 
                stream_Identifier.nextNode()
                );

                adaptor.addChild(root_1, stream_exp.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }


            retval.tree = root_0;

            }

            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "namedArg"


    public static class qualifiedName_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "qualifiedName"
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:134:1: qualifiedName : Identifier ( '.' Identifier )* ;
    public final FluxParser.qualifiedName_return qualifiedName() throws RecognitionException {
        FluxParser.qualifiedName_return retval = new FluxParser.qualifiedName_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token Identifier45=null;
        Token char_literal46=null;
        Token Identifier47=null;

        CommonTree Identifier45_tree=null;
        CommonTree char_literal46_tree=null;
        CommonTree Identifier47_tree=null;

        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:135:3: ( Identifier ( '.' Identifier )* )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:136:3: Identifier ( '.' Identifier )*
            {
            root_0 = (CommonTree)adaptor.nil();


            Identifier45=(Token)match(input,Identifier,FOLLOW_Identifier_in_qualifiedName625); 
            Identifier45_tree = 
            (CommonTree)adaptor.create(Identifier45)
            ;
            adaptor.addChild(root_0, Identifier45_tree);


            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:136:14: ( '.' Identifier )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0==28) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:136:15: '.' Identifier
            	    {
            	    char_literal46=(Token)match(input,28,FOLLOW_28_in_qualifiedName628); 
            	    char_literal46_tree = 
            	    (CommonTree)adaptor.create(char_literal46)
            	    ;
            	    adaptor.addChild(root_0, char_literal46_tree);


            	    Identifier47=(Token)match(input,Identifier,FOLLOW_Identifier_in_qualifiedName630); 
            	    Identifier47_tree = 
            	    (CommonTree)adaptor.create(Identifier47)
            	    ;
            	    adaptor.addChild(root_0, Identifier47_tree);


            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "qualifiedName"

    // Delegated rules


    protected DFA12 dfa12 = new DFA12(this);
    static final String DFA12_eotS =
        "\12\uffff";
    static final String DFA12_eofS =
        "\12\uffff";
    static final String DFA12_minS =
        "\1\12\1\31\1\uffff\1\31\1\uffff\2\12\1\uffff\1\31\1\uffff";
    static final String DFA12_maxS =
        "\1\25\1\36\1\uffff\1\33\1\uffff\1\22\1\25\1\uffff\1\33\1\uffff";
    static final String DFA12_acceptS =
        "\2\uffff\1\3\1\uffff\1\4\2\uffff\1\1\1\uffff\1\2";
    static final String DFA12_specialS =
        "\12\uffff}>";
    static final String[] DFA12_transitionS = {
            "\1\1\7\uffff\1\3\2\uffff\1\2",
            "\1\7\1\5\1\6\2\uffff\1\4",
            "",
            "\1\7\1\5\1\6",
            "",
            "\1\10\7\uffff\1\10",
            "\1\7\12\uffff\1\11",
            "",
            "\1\7\1\5\1\6",
            ""
    };

    static final short[] DFA12_eot = DFA.unpackEncodedString(DFA12_eotS);
    static final short[] DFA12_eof = DFA.unpackEncodedString(DFA12_eofS);
    static final char[] DFA12_min = DFA.unpackEncodedStringToUnsignedChars(DFA12_minS);
    static final char[] DFA12_max = DFA.unpackEncodedStringToUnsignedChars(DFA12_maxS);
    static final short[] DFA12_accept = DFA.unpackEncodedString(DFA12_acceptS);
    static final short[] DFA12_special = DFA.unpackEncodedString(DFA12_specialS);
    static final short[][] DFA12_transition;

    static {
        int numStates = DFA12_transitionS.length;
        DFA12_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA12_transition[i] = DFA.unpackEncodedString(DFA12_transitionS[i]);
        }
    }

    class DFA12 extends DFA {

        public DFA12(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 12;
            this.eot = DFA12_eot;
            this.eof = DFA12_eof;
            this.min = DFA12_min;
            this.max = DFA12_max;
            this.accept = DFA12_accept;
            this.special = DFA12_special;
            this.transition = DFA12_transition;
        }
        public String getDescription() {
            return "118:3: ( exp | exp ',' ! VarRef | VarRef | namedArg )";
        }
    }
 

    public static final BitSet FOLLOW_varDef_in_flux113 = new BitSet(new long[]{0x0000000080860402L});
    public static final BitSet FOLLOW_flow_in_flux116 = new BitSet(new long[]{0x0000000000860402L});
    public static final BitSet FOLLOW_31_in_varDef132 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_Identifier_in_varDef136 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_30_in_varDef138 = new BitSet(new long[]{0x0000000000040400L});
    public static final BitSet FOLLOW_exp_in_varDef140 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_varDef142 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_varDef170 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_30_in_varDef172 = new BitSet(new long[]{0x0000000000040400L});
    public static final BitSet FOLLOW_exp_in_varDef174 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_varDef176 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_StdIn_in_flow217 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_exp_in_flow225 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_Wormhole_in_flow233 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_33_in_flow241 = new BitSet(new long[]{0x0000000100000400L});
    public static final BitSet FOLLOW_flowtail_in_flow244 = new BitSet(new long[]{0x0000000220000000L});
    public static final BitSet FOLLOW_33_in_flow247 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_Wormhole_in_flow250 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_flow254 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_tee271 = new BitSet(new long[]{0x0000000100000400L});
    public static final BitSet FOLLOW_flowtail_in_tee273 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_34_in_tee275 = new BitSet(new long[]{0x0000000100000002L});
    public static final BitSet FOLLOW_pipe_in_flowtail346 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_tee_in_flowtail354 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_33_in_flowtail368 = new BitSet(new long[]{0x0000000100000400L});
    public static final BitSet FOLLOW_pipe_in_flowtail383 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_tee_in_flowtail393 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_qualifiedName_in_pipe434 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_24_in_pipe437 = new BitSet(new long[]{0x0000000000240400L});
    public static final BitSet FOLLOW_pipeArgs_in_pipe439 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_25_in_pipe441 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_atom_in_exp478 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_26_in_exp481 = new BitSet(new long[]{0x0000000000040400L});
    public static final BitSet FOLLOW_atom_in_exp484 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_exp_in_pipeArgs528 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_exp_in_pipeArgs536 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_27_in_pipeArgs538 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_VarRef_in_pipeArgs541 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_VarRef_in_pipeArgs549 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_namedArg_in_pipeArgs557 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_27_in_pipeArgs566 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_namedArg_in_pipeArgs569 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_Identifier_in_namedArg586 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_30_in_namedArg588 = new BitSet(new long[]{0x0000000000040400L});
    public static final BitSet FOLLOW_exp_in_namedArg590 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_qualifiedName625 = new BitSet(new long[]{0x0000000010000002L});
    public static final BitSet FOLLOW_28_in_qualifiedName628 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_Identifier_in_qualifiedName630 = new BitSet(new long[]{0x0000000010000002L});

}