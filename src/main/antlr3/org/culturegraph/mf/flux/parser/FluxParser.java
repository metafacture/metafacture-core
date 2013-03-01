// $ANTLR 3.4 U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g 2013-03-01 11:51:24

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
    public String getGrammarFileName() { return "U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g"; }


    public static class flux_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "flux"
    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:43:1: flux : ( varDef )* mainflow ;
    public final FluxParser.flux_return flux() throws RecognitionException {
        FluxParser.flux_return retval = new FluxParser.flux_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        FluxParser.varDef_return varDef1 =null;

        FluxParser.mainflow_return mainflow2 =null;



        try {
            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:44:3: ( ( varDef )* mainflow )
            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:45:3: ( varDef )* mainflow
            {
            root_0 = (CommonTree)adaptor.nil();


            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:45:3: ( varDef )*
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
            	    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:45:3: varDef
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


            pushFollow(FOLLOW_mainflow_in_flux116);
            mainflow2=mainflow();

            state._fsp--;

            adaptor.addChild(root_0, mainflow2.getTree());

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
    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:51:1: varDef : ( 'default ' i= Identifier '=' exp ';' -> ^( DEFAULT Identifier exp ) |i= Identifier '=' exp ';' -> ^( ASSIGN Identifier exp ) );
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
            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:52:3: ( 'default ' i= Identifier '=' exp ';' -> ^( DEFAULT Identifier exp ) |i= Identifier '=' exp ';' -> ^( ASSIGN Identifier exp ) )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==30) ) {
                alt2=1;
            }
            else if ( (LA2_0==Identifier) ) {
                alt2=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;

            }
            switch (alt2) {
                case 1 :
                    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:53:3: 'default ' i= Identifier '=' exp ';'
                    {
                    string_literal3=(Token)match(input,30,FOLLOW_30_in_varDef140);  
                    stream_30.add(string_literal3);


                    i=(Token)match(input,Identifier,FOLLOW_Identifier_in_varDef144);  
                    stream_Identifier.add(i);


                    char_literal4=(Token)match(input,29,FOLLOW_29_in_varDef146);  
                    stream_29.add(char_literal4);


                    pushFollow(FOLLOW_exp_in_varDef148);
                    exp5=exp();

                    state._fsp--;

                    stream_exp.add(exp5.getTree());

                    char_literal6=(Token)match(input,28,FOLLOW_28_in_varDef150);  
                    stream_28.add(char_literal6);


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
                    // 54:5: -> ^( DEFAULT Identifier exp )
                    {
                        // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:55:7: ^( DEFAULT Identifier exp )
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
                    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:56:5: i= Identifier '=' exp ';'
                    {
                    i=(Token)match(input,Identifier,FOLLOW_Identifier_in_varDef178);  
                    stream_Identifier.add(i);


                    char_literal7=(Token)match(input,29,FOLLOW_29_in_varDef180);  
                    stream_29.add(char_literal7);


                    pushFollow(FOLLOW_exp_in_varDef182);
                    exp8=exp();

                    state._fsp--;

                    stream_exp.add(exp8.getTree());

                    char_literal9=(Token)match(input,28,FOLLOW_28_in_varDef184);  
                    stream_28.add(char_literal9);


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
                    // 57:5: -> ^( ASSIGN Identifier exp )
                    {
                        // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:58:7: ^( ASSIGN Identifier exp )
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


    public static class mainflow_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "mainflow"
    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:61:1: mainflow : ( StdIn | exp ) '|' ! flow ';' !;
    public final FluxParser.mainflow_return mainflow() throws RecognitionException {
        FluxParser.mainflow_return retval = new FluxParser.mainflow_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token StdIn10=null;
        Token char_literal12=null;
        Token char_literal14=null;
        FluxParser.exp_return exp11 =null;

        FluxParser.flow_return flow13 =null;


        CommonTree StdIn10_tree=null;
        CommonTree char_literal12_tree=null;
        CommonTree char_literal14_tree=null;

        try {
            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:62:3: ( ( StdIn | exp ) '|' ! flow ';' !)
            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:63:3: ( StdIn | exp ) '|' ! flow ';' !
            {
            root_0 = (CommonTree)adaptor.nil();


            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:63:3: ( StdIn | exp )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==StdIn) ) {
                alt3=1;
            }
            else if ( (LA3_0==Identifier||LA3_0==StringLiteral) ) {
                alt3=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;

            }
            switch (alt3) {
                case 1 :
                    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:64:5: StdIn
                    {
                    StdIn10=(Token)match(input,StdIn,FOLLOW_StdIn_in_mainflow225); 
                    StdIn10_tree = 
                    (CommonTree)adaptor.create(StdIn10)
                    ;
                    adaptor.addChild(root_0, StdIn10_tree);


                    }
                    break;
                case 2 :
                    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:65:7: exp
                    {
                    pushFollow(FOLLOW_exp_in_mainflow233);
                    exp11=exp();

                    state._fsp--;

                    adaptor.addChild(root_0, exp11.getTree());

                    }
                    break;

            }


            char_literal12=(Token)match(input,32,FOLLOW_32_in_mainflow241); 

            pushFollow(FOLLOW_flow_in_mainflow244);
            flow13=flow();

            state._fsp--;

            adaptor.addChild(root_0, flow13.getTree());

            char_literal14=(Token)match(input,28,FOLLOW_28_in_mainflow246); 

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
    // $ANTLR end "mainflow"


    public static class tee_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "tee"
    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:70:1: tee : ( '{' flow '}' )+ -> ^( TEE ( ^( SUBFLOW flow ) )+ ) ;
    public final FluxParser.tee_return tee() throws RecognitionException {
        FluxParser.tee_return retval = new FluxParser.tee_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token char_literal15=null;
        Token char_literal17=null;
        FluxParser.flow_return flow16 =null;


        CommonTree char_literal15_tree=null;
        CommonTree char_literal17_tree=null;
        RewriteRuleTokenStream stream_31=new RewriteRuleTokenStream(adaptor,"token 31");
        RewriteRuleTokenStream stream_33=new RewriteRuleTokenStream(adaptor,"token 33");
        RewriteRuleSubtreeStream stream_flow=new RewriteRuleSubtreeStream(adaptor,"rule flow");
        try {
            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:71:3: ( ( '{' flow '}' )+ -> ^( TEE ( ^( SUBFLOW flow ) )+ ) )
            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:72:3: ( '{' flow '}' )+
            {
            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:72:3: ( '{' flow '}' )+
            int cnt4=0;
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==31) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:72:4: '{' flow '}'
            	    {
            	    char_literal15=(Token)match(input,31,FOLLOW_31_in_tee263);  
            	    stream_31.add(char_literal15);


            	    pushFollow(FOLLOW_flow_in_tee265);
            	    flow16=flow();

            	    state._fsp--;

            	    stream_flow.add(flow16.getTree());

            	    char_literal17=(Token)match(input,33,FOLLOW_33_in_tee267);  
            	    stream_33.add(char_literal17);


            	    }
            	    break;

            	default :
            	    if ( cnt4 >= 1 ) break loop4;
                        EarlyExitException eee =
                            new EarlyExitException(4, input);
                        throw eee;
                }
                cnt4++;
            } while (true);


            // AST REWRITE
            // elements: flow
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 73:5: -> ^( TEE ( ^( SUBFLOW flow ) )+ )
            {
                // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:74:7: ^( TEE ( ^( SUBFLOW flow ) )+ )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(
                (CommonTree)adaptor.create(TEE, "TEE")
                , root_1);

                if ( !(stream_flow.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_flow.hasNext() ) {
                    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:76:9: ^( SUBFLOW flow )
                    {
                    CommonTree root_2 = (CommonTree)adaptor.nil();
                    root_2 = (CommonTree)adaptor.becomeRoot(
                    (CommonTree)adaptor.create(SUBFLOW, "SUBFLOW")
                    , root_2);

                    adaptor.addChild(root_2, stream_flow.nextTree());

                    adaptor.addChild(root_1, root_2);
                    }

                }
                stream_flow.reset();

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


    public static class flow_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "flow"
    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:80:1: flow : ( pipe | tee ) ( '|' ! ( pipe | tee ) )* ;
    public final FluxParser.flow_return flow() throws RecognitionException {
        FluxParser.flow_return retval = new FluxParser.flow_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token char_literal20=null;
        FluxParser.pipe_return pipe18 =null;

        FluxParser.tee_return tee19 =null;

        FluxParser.pipe_return pipe21 =null;

        FluxParser.tee_return tee22 =null;


        CommonTree char_literal20_tree=null;

        try {
            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:81:3: ( ( pipe | tee ) ( '|' ! ( pipe | tee ) )* )
            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:82:3: ( pipe | tee ) ( '|' ! ( pipe | tee ) )*
            {
            root_0 = (CommonTree)adaptor.nil();


            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:82:3: ( pipe | tee )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==Identifier) ) {
                alt5=1;
            }
            else if ( (LA5_0==31) ) {
                alt5=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;

            }
            switch (alt5) {
                case 1 :
                    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:83:5: pipe
                    {
                    pushFollow(FOLLOW_pipe_in_flow338);
                    pipe18=pipe();

                    state._fsp--;

                    adaptor.addChild(root_0, pipe18.getTree());

                    }
                    break;
                case 2 :
                    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:84:7: tee
                    {
                    pushFollow(FOLLOW_tee_in_flow346);
                    tee19=tee();

                    state._fsp--;

                    adaptor.addChild(root_0, tee19.getTree());

                    }
                    break;

            }


            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:86:3: ( '|' ! ( pipe | tee ) )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==32) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:87:5: '|' ! ( pipe | tee )
            	    {
            	    char_literal20=(Token)match(input,32,FOLLOW_32_in_flow360); 

            	    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:88:5: ( pipe | tee )
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
            	            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:89:7: pipe
            	            {
            	            pushFollow(FOLLOW_pipe_in_flow375);
            	            pipe21=pipe();

            	            state._fsp--;

            	            adaptor.addChild(root_0, pipe21.getTree());

            	            }
            	            break;
            	        case 2 :
            	            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:90:9: tee
            	            {
            	            pushFollow(FOLLOW_tee_in_flow385);
            	            tee22=tee();

            	            state._fsp--;

            	            adaptor.addChild(root_0, tee22.getTree());

            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    break loop7;
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
    // $ANTLR end "flow"


    public static class pipe_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "pipe"
    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:100:1: pipe : qualifiedName ( '(' pipeArgs ')' )? -> ^( QualifiedName[$qualifiedName.text] ( pipeArgs )* ) ;
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
            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:101:3: ( qualifiedName ( '(' pipeArgs ')' )? -> ^( QualifiedName[$qualifiedName.text] ( pipeArgs )* ) )
            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:102:3: qualifiedName ( '(' pipeArgs ')' )?
            {
            pushFollow(FOLLOW_qualifiedName_in_pipe426);
            qualifiedName23=qualifiedName();

            state._fsp--;

            stream_qualifiedName.add(qualifiedName23.getTree());

            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:102:17: ( '(' pipeArgs ')' )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==23) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:102:18: '(' pipeArgs ')'
                    {
                    char_literal24=(Token)match(input,23,FOLLOW_23_in_pipe429);  
                    stream_23.add(char_literal24);


                    pushFollow(FOLLOW_pipeArgs_in_pipe431);
                    pipeArgs25=pipeArgs();

                    state._fsp--;

                    stream_pipeArgs.add(pipeArgs25.getTree());

                    char_literal26=(Token)match(input,24,FOLLOW_24_in_pipe433);  
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
            // 103:5: -> ^( QualifiedName[$qualifiedName.text] ( pipeArgs )* )
            {
                // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:104:7: ^( QualifiedName[$qualifiedName.text] ( pipeArgs )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(
                (CommonTree)adaptor.create(QualifiedName, (qualifiedName23!=null?input.toString(qualifiedName23.start,qualifiedName23.stop):null))
                , root_1);

                // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:104:44: ( pipeArgs )*
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
    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:107:1: exp : atom ( '+' ^ atom )* ;
    public final FluxParser.exp_return exp() throws RecognitionException {
        FluxParser.exp_return retval = new FluxParser.exp_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token char_literal28=null;
        FluxParser.atom_return atom27 =null;

        FluxParser.atom_return atom29 =null;


        CommonTree char_literal28_tree=null;

        try {
            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:108:3: ( atom ( '+' ^ atom )* )
            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:109:3: atom ( '+' ^ atom )*
            {
            root_0 = (CommonTree)adaptor.nil();


            pushFollow(FOLLOW_atom_in_exp470);
            atom27=atom();

            state._fsp--;

            adaptor.addChild(root_0, atom27.getTree());

            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:109:8: ( '+' ^ atom )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==25) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:109:9: '+' ^ atom
            	    {
            	    char_literal28=(Token)match(input,25,FOLLOW_25_in_exp473); 
            	    char_literal28_tree = 
            	    (CommonTree)adaptor.create(char_literal28)
            	    ;
            	    root_0 = (CommonTree)adaptor.becomeRoot(char_literal28_tree, root_0);


            	    pushFollow(FOLLOW_atom_in_exp476);
            	    atom29=atom();

            	    state._fsp--;

            	    adaptor.addChild(root_0, atom29.getTree());

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
    // $ANTLR end "exp"


    public static class atom_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "atom"
    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:112:1: atom : ( StringLiteral | Identifier );
    public final FluxParser.atom_return atom() throws RecognitionException {
        FluxParser.atom_return retval = new FluxParser.atom_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        Token set30=null;

        CommonTree set30_tree=null;

        try {
            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:113:3: ( StringLiteral | Identifier )
            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:
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
    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:118:1: pipeArgs : ( exp | exp ',' ! VarRef | VarRef | namedArg ) ( ',' ! namedArg )* ;
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
            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:119:3: ( ( exp | exp ',' ! VarRef | VarRef | namedArg ) ( ',' ! namedArg )* )
            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:120:3: ( exp | exp ',' ! VarRef | VarRef | namedArg ) ( ',' ! namedArg )*
            {
            root_0 = (CommonTree)adaptor.nil();


            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:120:3: ( exp | exp ',' ! VarRef | VarRef | namedArg )
            int alt10=4;
            alt10 = dfa10.predict(input);
            switch (alt10) {
                case 1 :
                    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:121:5: exp
                    {
                    pushFollow(FOLLOW_exp_in_pipeArgs520);
                    exp31=exp();

                    state._fsp--;

                    adaptor.addChild(root_0, exp31.getTree());

                    }
                    break;
                case 2 :
                    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:122:7: exp ',' ! VarRef
                    {
                    pushFollow(FOLLOW_exp_in_pipeArgs528);
                    exp32=exp();

                    state._fsp--;

                    adaptor.addChild(root_0, exp32.getTree());

                    char_literal33=(Token)match(input,26,FOLLOW_26_in_pipeArgs530); 

                    VarRef34=(Token)match(input,VarRef,FOLLOW_VarRef_in_pipeArgs533); 
                    VarRef34_tree = 
                    (CommonTree)adaptor.create(VarRef34)
                    ;
                    adaptor.addChild(root_0, VarRef34_tree);


                    }
                    break;
                case 3 :
                    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:123:7: VarRef
                    {
                    VarRef35=(Token)match(input,VarRef,FOLLOW_VarRef_in_pipeArgs541); 
                    VarRef35_tree = 
                    (CommonTree)adaptor.create(VarRef35)
                    ;
                    adaptor.addChild(root_0, VarRef35_tree);


                    }
                    break;
                case 4 :
                    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:124:7: namedArg
                    {
                    pushFollow(FOLLOW_namedArg_in_pipeArgs549);
                    namedArg36=namedArg();

                    state._fsp--;

                    adaptor.addChild(root_0, namedArg36.getTree());

                    }
                    break;

            }


            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:126:3: ( ',' ! namedArg )*
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( (LA11_0==26) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:126:4: ',' ! namedArg
            	    {
            	    char_literal37=(Token)match(input,26,FOLLOW_26_in_pipeArgs558); 

            	    pushFollow(FOLLOW_namedArg_in_pipeArgs561);
            	    namedArg38=namedArg();

            	    state._fsp--;

            	    adaptor.addChild(root_0, namedArg38.getTree());

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
    // $ANTLR end "pipeArgs"


    public static class namedArg_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "namedArg"
    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:129:1: namedArg : Identifier '=' exp -> ^( ARG Identifier exp ) ;
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
            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:130:3: ( Identifier '=' exp -> ^( ARG Identifier exp ) )
            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:131:3: Identifier '=' exp
            {
            Identifier39=(Token)match(input,Identifier,FOLLOW_Identifier_in_namedArg578);  
            stream_Identifier.add(Identifier39);


            char_literal40=(Token)match(input,29,FOLLOW_29_in_namedArg580);  
            stream_29.add(char_literal40);


            pushFollow(FOLLOW_exp_in_namedArg582);
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
            // 132:5: -> ^( ARG Identifier exp )
            {
                // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:133:7: ^( ARG Identifier exp )
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
    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:136:1: qualifiedName : Identifier ( '.' Identifier )* ;
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
            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:137:3: ( Identifier ( '.' Identifier )* )
            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:138:3: Identifier ( '.' Identifier )*
            {
            root_0 = (CommonTree)adaptor.nil();


            Identifier42=(Token)match(input,Identifier,FOLLOW_Identifier_in_qualifiedName617); 
            Identifier42_tree = 
            (CommonTree)adaptor.create(Identifier42)
            ;
            adaptor.addChild(root_0, Identifier42_tree);


            // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:138:14: ( '.' Identifier )*
            loop12:
            do {
                int alt12=2;
                int LA12_0 = input.LA(1);

                if ( (LA12_0==27) ) {
                    alt12=1;
                }


                switch (alt12) {
            	case 1 :
            	    // U:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\Flux.g:138:15: '.' Identifier
            	    {
            	    char_literal43=(Token)match(input,27,FOLLOW_27_in_qualifiedName620); 
            	    char_literal43_tree = 
            	    (CommonTree)adaptor.create(char_literal43)
            	    ;
            	    adaptor.addChild(root_0, char_literal43_tree);


            	    Identifier44=(Token)match(input,Identifier,FOLLOW_Identifier_in_qualifiedName622); 
            	    Identifier44_tree = 
            	    (CommonTree)adaptor.create(Identifier44)
            	    ;
            	    adaptor.addChild(root_0, Identifier44_tree);


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
    // $ANTLR end "qualifiedName"

    // Delegated rules


    protected DFA10 dfa10 = new DFA10(this);
    static final String DFA10_eotS =
        "\12\uffff";
    static final String DFA10_eofS =
        "\12\uffff";
    static final String DFA10_minS =
        "\1\12\1\30\1\uffff\1\30\1\uffff\2\12\1\uffff\1\30\1\uffff";
    static final String DFA10_maxS =
        "\1\25\1\35\1\uffff\1\32\1\uffff\1\22\1\25\1\uffff\1\32\1\uffff";
    static final String DFA10_acceptS =
        "\2\uffff\1\3\1\uffff\1\4\2\uffff\1\1\1\uffff\1\2";
    static final String DFA10_specialS =
        "\12\uffff}>";
    static final String[] DFA10_transitionS = {
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

    static final short[] DFA10_eot = DFA.unpackEncodedString(DFA10_eotS);
    static final short[] DFA10_eof = DFA.unpackEncodedString(DFA10_eofS);
    static final char[] DFA10_min = DFA.unpackEncodedStringToUnsignedChars(DFA10_minS);
    static final char[] DFA10_max = DFA.unpackEncodedStringToUnsignedChars(DFA10_maxS);
    static final short[] DFA10_accept = DFA.unpackEncodedString(DFA10_acceptS);
    static final short[] DFA10_special = DFA.unpackEncodedString(DFA10_specialS);
    static final short[][] DFA10_transition;

    static {
        int numStates = DFA10_transitionS.length;
        DFA10_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA10_transition[i] = DFA.unpackEncodedString(DFA10_transitionS[i]);
        }
    }

    class DFA10 extends DFA {

        public DFA10(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 10;
            this.eot = DFA10_eot;
            this.eof = DFA10_eof;
            this.min = DFA10_min;
            this.max = DFA10_max;
            this.accept = DFA10_accept;
            this.special = DFA10_special;
            this.transition = DFA10_transition;
        }
        public String getDescription() {
            return "120:3: ( exp | exp ',' ! VarRef | VarRef | namedArg )";
        }
    }
 

    public static final BitSet FOLLOW_varDef_in_flux113 = new BitSet(new long[]{0x0000000040060400L});
    public static final BitSet FOLLOW_mainflow_in_flux116 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_30_in_varDef140 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_Identifier_in_varDef144 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_varDef146 = new BitSet(new long[]{0x0000000000040400L});
    public static final BitSet FOLLOW_exp_in_varDef148 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_varDef150 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_varDef178 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_varDef180 = new BitSet(new long[]{0x0000000000040400L});
    public static final BitSet FOLLOW_exp_in_varDef182 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_varDef184 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_StdIn_in_mainflow225 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_exp_in_mainflow233 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_32_in_mainflow241 = new BitSet(new long[]{0x0000000080000400L});
    public static final BitSet FOLLOW_flow_in_mainflow244 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_28_in_mainflow246 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_31_in_tee263 = new BitSet(new long[]{0x0000000080000400L});
    public static final BitSet FOLLOW_flow_in_tee265 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_33_in_tee267 = new BitSet(new long[]{0x0000000080000002L});
    public static final BitSet FOLLOW_pipe_in_flow338 = new BitSet(new long[]{0x0000000100000002L});
    public static final BitSet FOLLOW_tee_in_flow346 = new BitSet(new long[]{0x0000000100000002L});
    public static final BitSet FOLLOW_32_in_flow360 = new BitSet(new long[]{0x0000000080000400L});
    public static final BitSet FOLLOW_pipe_in_flow375 = new BitSet(new long[]{0x0000000100000002L});
    public static final BitSet FOLLOW_tee_in_flow385 = new BitSet(new long[]{0x0000000100000002L});
    public static final BitSet FOLLOW_qualifiedName_in_pipe426 = new BitSet(new long[]{0x0000000000800002L});
    public static final BitSet FOLLOW_23_in_pipe429 = new BitSet(new long[]{0x0000000000240400L});
    public static final BitSet FOLLOW_pipeArgs_in_pipe431 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_24_in_pipe433 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_atom_in_exp470 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_25_in_exp473 = new BitSet(new long[]{0x0000000000040400L});
    public static final BitSet FOLLOW_atom_in_exp476 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_exp_in_pipeArgs520 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_exp_in_pipeArgs528 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_26_in_pipeArgs530 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_VarRef_in_pipeArgs533 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_VarRef_in_pipeArgs541 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_namedArg_in_pipeArgs549 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_26_in_pipeArgs558 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_namedArg_in_pipeArgs561 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_Identifier_in_namedArg578 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_namedArg580 = new BitSet(new long[]{0x0000000000040400L});
    public static final BitSet FOLLOW_exp_in_namedArg582 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_qualifiedName617 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_27_in_qualifiedName620 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_Identifier_in_qualifiedName622 = new BitSet(new long[]{0x0000000008000002L});

}