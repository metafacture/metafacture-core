// $ANTLR 3.4 D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g 2013-04-09 11:19:02

package org.culturegraph.mf.flux.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.runtime.tree.*;


@SuppressWarnings({"all", "warnings", "unchecked"})
public class FluxParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ARG", "ASSIGN", "DEFAULT", "Digit", "EscapeSequence", "HexDigit", "Identifier", "LINE_COMMENT", "Letter", "OctalEscape", "QualifiedName", "SUBFLOW", "StartString", "StdIn", "StringLiteral", "TEE", "UnicodeEscape", "VarRef", "WS", "'('", "')'", "'+'", "','", "'.'", "';'", "'='", "'default '", "'{'", "'|'", "'}'"
    };

    public static final int EOF=-1;
    public static final int T__23=23;
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

                    if ( (LA1_2==29) ) {
                        alt1=1;
                    }


                }
                else if ( (LA1_0==30) ) {
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

                if ( (LA2_0==Identifier||(LA2_0 >= StdIn && LA2_0 <= StringLiteral)) ) {
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
        RewriteRuleTokenStream stream_Identifier=new RewriteRuleTokenStream(adaptor,"token Identifier");
        RewriteRuleTokenStream stream_28=new RewriteRuleTokenStream(adaptor,"token 28");
        RewriteRuleTokenStream stream_29=new RewriteRuleTokenStream(adaptor,"token 29");
        RewriteRuleSubtreeStream stream_exp=new RewriteRuleSubtreeStream(adaptor,"rule exp");
        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:49:3: ( 'default ' i= Identifier '=' exp ';' -> ^( DEFAULT Identifier exp ) |i= Identifier '=' exp ';' -> ^( ASSIGN Identifier exp ) )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==30) ) {
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
                    string_literal3=(Token)match(input,30,FOLLOW_30_in_varDef132);  
                    stream_30.add(string_literal3);


                    i=(Token)match(input,Identifier,FOLLOW_Identifier_in_varDef136);  
                    stream_Identifier.add(i);


                    char_literal4=(Token)match(input,29,FOLLOW_29_in_varDef138);  
                    stream_29.add(char_literal4);


                    pushFollow(FOLLOW_exp_in_varDef140);
                    exp5=exp();

                    state._fsp--;

                    stream_exp.add(exp5.getTree());

                    char_literal6=(Token)match(input,28,FOLLOW_28_in_varDef142);  
                    stream_28.add(char_literal6);


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


                    char_literal7=(Token)match(input,29,FOLLOW_29_in_varDef172);  
                    stream_29.add(char_literal7);


                    pushFollow(FOLLOW_exp_in_varDef174);
                    exp8=exp();

                    state._fsp--;

                    stream_exp.add(exp8.getTree());

                    char_literal9=(Token)match(input,28,FOLLOW_28_in_varDef176);  
                    stream_28.add(char_literal9);


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
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:58:1: flow : ( StdIn | exp ) '|' ! flowtail ';' !;
    public final FluxParser.flow_return flow() throws RecognitionException {
        FluxParser.flow_return retval = new FluxParser.flow_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token StdIn10=null;
        Token char_literal12=null;
        Token char_literal14=null;
        FluxParser.exp_return exp11 =null;

        FluxParser.flowtail_return flowtail13 =null;


        CommonTree StdIn10_tree=null;
        CommonTree char_literal12_tree=null;
        CommonTree char_literal14_tree=null;

        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:59:3: ( ( StdIn | exp ) '|' ! flowtail ';' !)
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:60:3: ( StdIn | exp ) '|' ! flowtail ';' !
            {
            root_0 = (CommonTree)adaptor.nil();


            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:60:3: ( StdIn | exp )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==StdIn) ) {
                alt4=1;
            }
            else if ( (LA4_0==Identifier||LA4_0==StringLiteral) ) {
                alt4=2;
            }
            else {
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

            }


            char_literal12=(Token)match(input,32,FOLLOW_32_in_flow233); 

            pushFollow(FOLLOW_flowtail_in_flow236);
            flowtail13=flowtail();

            state._fsp--;

            adaptor.addChild(root_0, flowtail13.getTree());

            char_literal14=(Token)match(input,28,FOLLOW_28_in_flow238); 

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
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:67:1: tee : ( '{' flowtail '}' )+ -> ^( TEE ( ^( SUBFLOW flowtail ) )+ ) ;
    public final FluxParser.tee_return tee() throws RecognitionException {
        FluxParser.tee_return retval = new FluxParser.tee_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token char_literal15=null;
        Token char_literal17=null;
        FluxParser.flowtail_return flowtail16 =null;


        CommonTree char_literal15_tree=null;
        CommonTree char_literal17_tree=null;
        RewriteRuleTokenStream stream_31=new RewriteRuleTokenStream(adaptor,"token 31");
        RewriteRuleTokenStream stream_33=new RewriteRuleTokenStream(adaptor,"token 33");
        RewriteRuleSubtreeStream stream_flowtail=new RewriteRuleSubtreeStream(adaptor,"rule flowtail");
        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:68:3: ( ( '{' flowtail '}' )+ -> ^( TEE ( ^( SUBFLOW flowtail ) )+ ) )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:69:3: ( '{' flowtail '}' )+
            {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:69:3: ( '{' flowtail '}' )+
            int cnt5=0;
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==31) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:69:4: '{' flowtail '}'
            	    {
            	    char_literal15=(Token)match(input,31,FOLLOW_31_in_tee255);  
            	    stream_31.add(char_literal15);


            	    pushFollow(FOLLOW_flowtail_in_tee257);
            	    flowtail16=flowtail();

            	    state._fsp--;

            	    stream_flowtail.add(flowtail16.getTree());

            	    char_literal17=(Token)match(input,33,FOLLOW_33_in_tee259);  
            	    stream_33.add(char_literal17);


            	    }
            	    break;

            	default :
            	    if ( cnt5 >= 1 ) break loop5;
                        EarlyExitException eee =
                            new EarlyExitException(5, input);
                        throw eee;
                }
                cnt5++;
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
            // 70:5: -> ^( TEE ( ^( SUBFLOW flowtail ) )+ )
            {
                // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:71:7: ^( TEE ( ^( SUBFLOW flowtail ) )+ )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(
                (CommonTree)adaptor.create(TEE, "TEE")
                , root_1);

                if ( !(stream_flowtail.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_flowtail.hasNext() ) {
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:73:9: ^( SUBFLOW flowtail )
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
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:77:1: flowtail : ( pipe | tee ) ( '|' ! ( pipe | tee ) )* ;
    public final FluxParser.flowtail_return flowtail() throws RecognitionException {
        FluxParser.flowtail_return retval = new FluxParser.flowtail_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token char_literal20=null;
        FluxParser.pipe_return pipe18 =null;

        FluxParser.tee_return tee19 =null;

        FluxParser.pipe_return pipe21 =null;

        FluxParser.tee_return tee22 =null;


        CommonTree char_literal20_tree=null;

        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:78:3: ( ( pipe | tee ) ( '|' ! ( pipe | tee ) )* )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:79:3: ( pipe | tee ) ( '|' ! ( pipe | tee ) )*
            {
            root_0 = (CommonTree)adaptor.nil();


            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:79:3: ( pipe | tee )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==Identifier) ) {
                alt6=1;
            }
            else if ( (LA6_0==31) ) {
                alt6=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;

            }
            switch (alt6) {
                case 1 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:80:5: pipe
                    {
                    pushFollow(FOLLOW_pipe_in_flowtail330);
                    pipe18=pipe();

                    state._fsp--;

                    adaptor.addChild(root_0, pipe18.getTree());

                    }
                    break;
                case 2 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:81:7: tee
                    {
                    pushFollow(FOLLOW_tee_in_flowtail338);
                    tee19=tee();

                    state._fsp--;

                    adaptor.addChild(root_0, tee19.getTree());

                    }
                    break;

            }


            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:83:3: ( '|' ! ( pipe | tee ) )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0==32) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:84:5: '|' ! ( pipe | tee )
            	    {
            	    char_literal20=(Token)match(input,32,FOLLOW_32_in_flowtail352); 

            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:85:5: ( pipe | tee )
            	    int alt7=2;
            	    int LA7_0 = input.LA(1);

            	    if ( (LA7_0==Identifier) ) {
            	        alt7=1;
            	    }
            	    else if ( (LA7_0==31) ) {
            	        alt7=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 7, 0, input);

            	        throw nvae;

            	    }
            	    switch (alt7) {
            	        case 1 :
            	            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:86:7: pipe
            	            {
            	            pushFollow(FOLLOW_pipe_in_flowtail367);
            	            pipe21=pipe();

            	            state._fsp--;

            	            adaptor.addChild(root_0, pipe21.getTree());

            	            }
            	            break;
            	        case 2 :
            	            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:87:9: tee
            	            {
            	            pushFollow(FOLLOW_tee_in_flowtail377);
            	            tee22=tee();

            	            state._fsp--;

            	            adaptor.addChild(root_0, tee22.getTree());

            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    break loop8;
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
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:97:1: pipe : qualifiedName ( '(' pipeArgs ')' )? -> ^( QualifiedName[$qualifiedName.text] ( pipeArgs )* ) ;
    public final FluxParser.pipe_return pipe() throws RecognitionException {
        FluxParser.pipe_return retval = new FluxParser.pipe_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token char_literal24=null;
        Token char_literal26=null;
        FluxParser.qualifiedName_return qualifiedName23 =null;

        FluxParser.pipeArgs_return pipeArgs25 =null;


        CommonTree char_literal24_tree=null;
        CommonTree char_literal26_tree=null;
        RewriteRuleTokenStream stream_23=new RewriteRuleTokenStream(adaptor,"token 23");
        RewriteRuleTokenStream stream_24=new RewriteRuleTokenStream(adaptor,"token 24");
        RewriteRuleSubtreeStream stream_qualifiedName=new RewriteRuleSubtreeStream(adaptor,"rule qualifiedName");
        RewriteRuleSubtreeStream stream_pipeArgs=new RewriteRuleSubtreeStream(adaptor,"rule pipeArgs");
        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:98:3: ( qualifiedName ( '(' pipeArgs ')' )? -> ^( QualifiedName[$qualifiedName.text] ( pipeArgs )* ) )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:99:3: qualifiedName ( '(' pipeArgs ')' )?
            {
            pushFollow(FOLLOW_qualifiedName_in_pipe418);
            qualifiedName23=qualifiedName();

            state._fsp--;

            stream_qualifiedName.add(qualifiedName23.getTree());

            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:99:17: ( '(' pipeArgs ')' )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==23) ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:99:18: '(' pipeArgs ')'
                    {
                    char_literal24=(Token)match(input,23,FOLLOW_23_in_pipe421);  
                    stream_23.add(char_literal24);


                    pushFollow(FOLLOW_pipeArgs_in_pipe423);
                    pipeArgs25=pipeArgs();

                    state._fsp--;

                    stream_pipeArgs.add(pipeArgs25.getTree());

                    char_literal26=(Token)match(input,24,FOLLOW_24_in_pipe425);  
                    stream_24.add(char_literal26);


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
            // 100:5: -> ^( QualifiedName[$qualifiedName.text] ( pipeArgs )* )
            {
                // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:101:7: ^( QualifiedName[$qualifiedName.text] ( pipeArgs )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(
                (CommonTree)adaptor.create(QualifiedName, (qualifiedName23!=null?input.toString(qualifiedName23.start,qualifiedName23.stop):null))
                , root_1);

                // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:101:44: ( pipeArgs )*
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
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:104:1: exp : atom ( '+' ^ atom )* ;
    public final FluxParser.exp_return exp() throws RecognitionException {
        FluxParser.exp_return retval = new FluxParser.exp_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token char_literal28=null;
        FluxParser.atom_return atom27 =null;

        FluxParser.atom_return atom29 =null;


        CommonTree char_literal28_tree=null;

        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:105:3: ( atom ( '+' ^ atom )* )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:106:3: atom ( '+' ^ atom )*
            {
            root_0 = (CommonTree)adaptor.nil();


            pushFollow(FOLLOW_atom_in_exp462);
            atom27=atom();

            state._fsp--;

            adaptor.addChild(root_0, atom27.getTree());

            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:106:8: ( '+' ^ atom )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==25) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:106:9: '+' ^ atom
            	    {
            	    char_literal28=(Token)match(input,25,FOLLOW_25_in_exp465); 
            	    char_literal28_tree = 
            	    (CommonTree)adaptor.create(char_literal28)
            	    ;
            	    root_0 = (CommonTree)adaptor.becomeRoot(char_literal28_tree, root_0);


            	    pushFollow(FOLLOW_atom_in_exp468);
            	    atom29=atom();

            	    state._fsp--;

            	    adaptor.addChild(root_0, atom29.getTree());

            	    }
            	    break;

            	default :
            	    break loop10;
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
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:109:1: atom : ( StringLiteral | Identifier );
    public final FluxParser.atom_return atom() throws RecognitionException {
        FluxParser.atom_return retval = new FluxParser.atom_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token set30=null;

        CommonTree set30_tree=null;

        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:110:3: ( StringLiteral | Identifier )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:
            {
            root_0 = (CommonTree)adaptor.nil();


            set30=(Token)input.LT(1);

            if ( input.LA(1)==Identifier||input.LA(1)==StringLiteral ) {
                input.consume();
                adaptor.addChild(root_0, 
                (CommonTree)adaptor.create(set30)
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
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:115:1: pipeArgs : ( exp | exp ',' ! VarRef | VarRef | namedArg ) ( ',' ! namedArg )* ;
    public final FluxParser.pipeArgs_return pipeArgs() throws RecognitionException {
        FluxParser.pipeArgs_return retval = new FluxParser.pipeArgs_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token char_literal33=null;
        Token VarRef34=null;
        Token VarRef35=null;
        Token char_literal37=null;
        FluxParser.exp_return exp31 =null;

        FluxParser.exp_return exp32 =null;

        FluxParser.namedArg_return namedArg36 =null;

        FluxParser.namedArg_return namedArg38 =null;


        CommonTree char_literal33_tree=null;
        CommonTree VarRef34_tree=null;
        CommonTree VarRef35_tree=null;
        CommonTree char_literal37_tree=null;

        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:116:3: ( ( exp | exp ',' ! VarRef | VarRef | namedArg ) ( ',' ! namedArg )* )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:117:3: ( exp | exp ',' ! VarRef | VarRef | namedArg ) ( ',' ! namedArg )*
            {
            root_0 = (CommonTree)adaptor.nil();


            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:117:3: ( exp | exp ',' ! VarRef | VarRef | namedArg )
            int alt11=4;
            alt11 = dfa11.predict(input);
            switch (alt11) {
                case 1 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:118:5: exp
                    {
                    pushFollow(FOLLOW_exp_in_pipeArgs512);
                    exp31=exp();

                    state._fsp--;

                    adaptor.addChild(root_0, exp31.getTree());

                    }
                    break;
                case 2 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:119:7: exp ',' ! VarRef
                    {
                    pushFollow(FOLLOW_exp_in_pipeArgs520);
                    exp32=exp();

                    state._fsp--;

                    adaptor.addChild(root_0, exp32.getTree());

                    char_literal33=(Token)match(input,26,FOLLOW_26_in_pipeArgs522); 

                    VarRef34=(Token)match(input,VarRef,FOLLOW_VarRef_in_pipeArgs525); 
                    VarRef34_tree = 
                    (CommonTree)adaptor.create(VarRef34)
                    ;
                    adaptor.addChild(root_0, VarRef34_tree);


                    }
                    break;
                case 3 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:120:7: VarRef
                    {
                    VarRef35=(Token)match(input,VarRef,FOLLOW_VarRef_in_pipeArgs533); 
                    VarRef35_tree = 
                    (CommonTree)adaptor.create(VarRef35)
                    ;
                    adaptor.addChild(root_0, VarRef35_tree);


                    }
                    break;
                case 4 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:121:7: namedArg
                    {
                    pushFollow(FOLLOW_namedArg_in_pipeArgs541);
                    namedArg36=namedArg();

                    state._fsp--;

                    adaptor.addChild(root_0, namedArg36.getTree());

                    }
                    break;

            }


            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:123:3: ( ',' ! namedArg )*
            loop12:
            do {
                int alt12=2;
                int LA12_0 = input.LA(1);

                if ( (LA12_0==26) ) {
                    alt12=1;
                }


                switch (alt12) {
            	case 1 :
            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:123:4: ',' ! namedArg
            	    {
            	    char_literal37=(Token)match(input,26,FOLLOW_26_in_pipeArgs550); 

            	    pushFollow(FOLLOW_namedArg_in_pipeArgs553);
            	    namedArg38=namedArg();

            	    state._fsp--;

            	    adaptor.addChild(root_0, namedArg38.getTree());

            	    }
            	    break;

            	default :
            	    break loop12;
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
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:126:1: namedArg : Identifier '=' exp -> ^( ARG Identifier exp ) ;
    public final FluxParser.namedArg_return namedArg() throws RecognitionException {
        FluxParser.namedArg_return retval = new FluxParser.namedArg_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token Identifier39=null;
        Token char_literal40=null;
        FluxParser.exp_return exp41 =null;


        CommonTree Identifier39_tree=null;
        CommonTree char_literal40_tree=null;
        RewriteRuleTokenStream stream_Identifier=new RewriteRuleTokenStream(adaptor,"token Identifier");
        RewriteRuleTokenStream stream_29=new RewriteRuleTokenStream(adaptor,"token 29");
        RewriteRuleSubtreeStream stream_exp=new RewriteRuleSubtreeStream(adaptor,"rule exp");
        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:127:3: ( Identifier '=' exp -> ^( ARG Identifier exp ) )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:128:3: Identifier '=' exp
            {
            Identifier39=(Token)match(input,Identifier,FOLLOW_Identifier_in_namedArg570);  
            stream_Identifier.add(Identifier39);


            char_literal40=(Token)match(input,29,FOLLOW_29_in_namedArg572);  
            stream_29.add(char_literal40);


            pushFollow(FOLLOW_exp_in_namedArg574);
            exp41=exp();

            state._fsp--;

            stream_exp.add(exp41.getTree());

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
            // 129:5: -> ^( ARG Identifier exp )
            {
                // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:130:7: ^( ARG Identifier exp )
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
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:133:1: qualifiedName : Identifier ( '.' Identifier )* ;
    public final FluxParser.qualifiedName_return qualifiedName() throws RecognitionException {
        FluxParser.qualifiedName_return retval = new FluxParser.qualifiedName_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token Identifier42=null;
        Token char_literal43=null;
        Token Identifier44=null;

        CommonTree Identifier42_tree=null;
        CommonTree char_literal43_tree=null;
        CommonTree Identifier44_tree=null;

        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:134:3: ( Identifier ( '.' Identifier )* )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:135:3: Identifier ( '.' Identifier )*
            {
            root_0 = (CommonTree)adaptor.nil();


            Identifier42=(Token)match(input,Identifier,FOLLOW_Identifier_in_qualifiedName609); 
            Identifier42_tree = 
            (CommonTree)adaptor.create(Identifier42)
            ;
            adaptor.addChild(root_0, Identifier42_tree);


            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:135:14: ( '.' Identifier )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( (LA13_0==27) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:135:15: '.' Identifier
            	    {
            	    char_literal43=(Token)match(input,27,FOLLOW_27_in_qualifiedName612); 
            	    char_literal43_tree = 
            	    (CommonTree)adaptor.create(char_literal43)
            	    ;
            	    adaptor.addChild(root_0, char_literal43_tree);


            	    Identifier44=(Token)match(input,Identifier,FOLLOW_Identifier_in_qualifiedName614); 
            	    Identifier44_tree = 
            	    (CommonTree)adaptor.create(Identifier44)
            	    ;
            	    adaptor.addChild(root_0, Identifier44_tree);


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
    // $ANTLR end "qualifiedName"

    // Delegated rules


    protected DFA11 dfa11 = new DFA11(this);
    static final String DFA11_eotS =
        "\12\uffff";
    static final String DFA11_eofS =
        "\12\uffff";
    static final String DFA11_minS =
        "\1\12\1\30\1\uffff\1\30\1\uffff\2\12\1\uffff\1\30\1\uffff";
    static final String DFA11_maxS =
        "\1\25\1\35\1\uffff\1\32\1\uffff\1\22\1\25\1\uffff\1\32\1\uffff";
    static final String DFA11_acceptS =
        "\2\uffff\1\3\1\uffff\1\4\2\uffff\1\1\1\uffff\1\2";
    static final String DFA11_specialS =
        "\12\uffff}>";
    static final String[] DFA11_transitionS = {
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

    static final short[] DFA11_eot = DFA.unpackEncodedString(DFA11_eotS);
    static final short[] DFA11_eof = DFA.unpackEncodedString(DFA11_eofS);
    static final char[] DFA11_min = DFA.unpackEncodedStringToUnsignedChars(DFA11_minS);
    static final char[] DFA11_max = DFA.unpackEncodedStringToUnsignedChars(DFA11_maxS);
    static final short[] DFA11_accept = DFA.unpackEncodedString(DFA11_acceptS);
    static final short[] DFA11_special = DFA.unpackEncodedString(DFA11_specialS);
    static final short[][] DFA11_transition;

    static {
        int numStates = DFA11_transitionS.length;
        DFA11_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA11_transition[i] = DFA.unpackEncodedString(DFA11_transitionS[i]);
        }
    }

    class DFA11 extends DFA {

        public DFA11(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 11;
            this.eot = DFA11_eot;
            this.eof = DFA11_eof;
            this.min = DFA11_min;
            this.max = DFA11_max;
            this.accept = DFA11_accept;
            this.special = DFA11_special;
            this.transition = DFA11_transition;
        }
        public String getDescription() {
            return "117:3: ( exp | exp ',' ! VarRef | VarRef | namedArg )";
        }
    }
 

    public static final BitSet FOLLOW_varDef_in_flux113 = new BitSet(new long[]{0x0000000040060402L});
    public static final BitSet FOLLOW_flow_in_flux116 = new BitSet(new long[]{0x0000000000060402L});
    public static final BitSet FOLLOW_30_in_varDef132 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_Identifier_in_varDef136 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_varDef138 = new BitSet(new long[]{0x0000000000040400L});
    public static final BitSet FOLLOW_exp_in_varDef140 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_varDef142 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_varDef170 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_varDef172 = new BitSet(new long[]{0x0000000000040400L});
    public static final BitSet FOLLOW_exp_in_varDef174 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_varDef176 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_StdIn_in_flow217 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_exp_in_flow225 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_32_in_flow233 = new BitSet(new long[]{0x0000000080000400L});
    public static final BitSet FOLLOW_flowtail_in_flow236 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_flow238 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_31_in_tee255 = new BitSet(new long[]{0x0000000080000400L});
    public static final BitSet FOLLOW_flowtail_in_tee257 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_33_in_tee259 = new BitSet(new long[]{0x0000000080000002L});
    public static final BitSet FOLLOW_pipe_in_flowtail330 = new BitSet(new long[]{0x0000000100000002L});
    public static final BitSet FOLLOW_tee_in_flowtail338 = new BitSet(new long[]{0x0000000100000002L});
    public static final BitSet FOLLOW_32_in_flowtail352 = new BitSet(new long[]{0x0000000080000400L});
    public static final BitSet FOLLOW_pipe_in_flowtail367 = new BitSet(new long[]{0x0000000100000002L});
    public static final BitSet FOLLOW_tee_in_flowtail377 = new BitSet(new long[]{0x0000000100000002L});
    public static final BitSet FOLLOW_qualifiedName_in_pipe418 = new BitSet(new long[]{0x0000000000800002L});
    public static final BitSet FOLLOW_23_in_pipe421 = new BitSet(new long[]{0x0000000000240400L});
    public static final BitSet FOLLOW_pipeArgs_in_pipe423 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_24_in_pipe425 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_atom_in_exp462 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_25_in_exp465 = new BitSet(new long[]{0x0000000000040400L});
    public static final BitSet FOLLOW_atom_in_exp468 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_exp_in_pipeArgs512 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_exp_in_pipeArgs520 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_pipeArgs522 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_VarRef_in_pipeArgs525 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_VarRef_in_pipeArgs533 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_namedArg_in_pipeArgs541 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_26_in_pipeArgs550 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_namedArg_in_pipeArgs553 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_Identifier_in_namedArg570 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_namedArg572 = new BitSet(new long[]{0x0000000000040400L});
    public static final BitSet FOLLOW_exp_in_namedArg574 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_qualifiedName609 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_27_in_qualifiedName612 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_Identifier_in_qualifiedName614 = new BitSet(new long[]{0x0000000008000002L});

}