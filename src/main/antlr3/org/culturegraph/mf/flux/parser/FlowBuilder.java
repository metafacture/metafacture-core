// $ANTLR 3.4 D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g 2013-04-09 11:19:02

package org.culturegraph.mf.flux.parser;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import org.culturegraph.mf.flux.Flow;
import org.culturegraph.mf.exceptions.FluxParseException;


import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class FlowBuilder extends TreeParser {
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
    public TreeParser[] getDelegates() {
        return new TreeParser[] {};
    }

    // delegators


    public FlowBuilder(TreeNodeStream input) {
        this(input, new RecognizerSharedState());
    }
    public FlowBuilder(TreeNodeStream input, RecognizerSharedState state) {
        super(input, state);
    }

    public String[] getTokenNames() { return FlowBuilder.tokenNames; }
    public String getGrammarFileName() { return "D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g"; }


    private Flow flow;
    private Map<String, String> vars = new HashMap<String, String>();

    public final void addVaribleAssignements(final Map<String, String> vars) {
    	this.vars.putAll(vars);
    }



    // $ANTLR start "flux"
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:45:1: flux returns [List<Flow> flows = new ArrayList<Flow>()] : varDefs ( flow )* ;
    public final List<Flow> flux() throws RecognitionException {
        List<Flow> flows =  new ArrayList<Flow>();


        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:46:3: ( varDefs ( flow )* )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:47:3: varDefs ( flow )*
            {
            pushFollow(FOLLOW_varDefs_in_flux72);
            varDefs();

            state._fsp--;


            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:48:3: ( flow )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==Identifier||(LA1_0 >= StdIn && LA1_0 <= StringLiteral)||LA1_0==25) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:49:5: flow
            	    {
            	    pushFollow(FOLLOW_flow_in_flux82);
            	    flow();

            	    state._fsp--;



            	             flows.add(this.flow);
            	            

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return flows;
    }
    // $ANTLR end "flux"



    // $ANTLR start "flow"
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:56:1: flow : ( StdIn |e= exp ) flowtail ;
    public final void flow() throws RecognitionException {
        String e =null;



        this.flow = new Flow();

        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:60:3: ( ( StdIn |e= exp ) flowtail )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:61:3: ( StdIn |e= exp ) flowtail
            {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:61:3: ( StdIn |e= exp )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==StdIn) ) {
                alt2=1;
            }
            else if ( (LA2_0==Identifier||LA2_0==StringLiteral||LA2_0==25) ) {
                alt2=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;

            }
            switch (alt2) {
                case 1 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:62:5: StdIn
                    {
                    match(input,StdIn,FOLLOW_StdIn_in_flow124); 


                              flow.setStdInStart();
                             

                    }
                    break;
                case 2 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:66:7: e= exp
                    {
                    pushFollow(FOLLOW_exp_in_flow146);
                    e=exp();

                    state._fsp--;



                                flow.setStringStart(e);
                               

                    }
                    break;

            }


            pushFollow(FOLLOW_flowtail_in_flow168);
            flowtail();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "flow"



    // $ANTLR start "varDefs"
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:74:1: varDefs : ( varDef )* ;
    public final void varDefs() throws RecognitionException {
        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:75:3: ( ( varDef )* )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:76:3: ( varDef )*
            {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:76:3: ( varDef )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0 >= ASSIGN && LA3_0 <= DEFAULT)) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:76:3: varDef
            	    {
            	    pushFollow(FOLLOW_varDef_in_varDefs183);
            	    varDef();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);



                      
                     

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "varDefs"



    // $ANTLR start "varDef"
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:82:1: varDef : ( ^( ASSIGN name= Identifier (e= exp )? ) | ^( DEFAULT name= Identifier (e= exp )? ) );
    public final void varDef() throws RecognitionException {
        CommonTree name=null;
        String e =null;


        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:83:3: ( ^( ASSIGN name= Identifier (e= exp )? ) | ^( DEFAULT name= Identifier (e= exp )? ) )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==ASSIGN) ) {
                alt6=1;
            }
            else if ( (LA6_0==DEFAULT) ) {
                alt6=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;

            }
            switch (alt6) {
                case 1 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:84:3: ^( ASSIGN name= Identifier (e= exp )? )
                    {
                    match(input,ASSIGN,FOLLOW_ASSIGN_in_varDef212); 

                    match(input, Token.DOWN, null); 
                    name=(CommonTree)match(input,Identifier,FOLLOW_Identifier_in_varDef216); 

                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:84:29: (e= exp )?
                    int alt4=2;
                    int LA4_0 = input.LA(1);

                    if ( (LA4_0==Identifier||LA4_0==StringLiteral||LA4_0==25) ) {
                        alt4=1;
                    }
                    switch (alt4) {
                        case 1 :
                            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:84:29: e= exp
                            {
                            pushFollow(FOLLOW_exp_in_varDef220);
                            e=exp();

                            state._fsp--;


                            }
                            break;

                    }


                    match(input, Token.UP, null); 



                       vars.put((name!=null?name.getText():null), e);
                      

                    }
                    break;
                case 2 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:90:3: ^( DEFAULT name= Identifier (e= exp )? )
                    {
                    match(input,DEFAULT,FOLLOW_DEFAULT_in_varDef238); 

                    match(input, Token.DOWN, null); 
                    name=(CommonTree)match(input,Identifier,FOLLOW_Identifier_in_varDef242); 

                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:90:30: (e= exp )?
                    int alt5=2;
                    int LA5_0 = input.LA(1);

                    if ( (LA5_0==Identifier||LA5_0==StringLiteral||LA5_0==25) ) {
                        alt5=1;
                    }
                    switch (alt5) {
                        case 1 :
                            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:90:30: e= exp
                            {
                            pushFollow(FOLLOW_exp_in_varDef246);
                            e=exp();

                            state._fsp--;


                            }
                            break;

                    }


                    match(input, Token.UP, null); 



                       if (!vars.containsKey((name!=null?name.getText():null))) {
                       	vars.put((name!=null?name.getText():null), e);
                       }
                      

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "varDef"



    // $ANTLR start "tee"
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:99:1: tee : ^( TEE ( ^( SUBFLOW flowtail ) )+ ) ;
    public final void tee() throws RecognitionException {
        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:100:3: ( ^( TEE ( ^( SUBFLOW flowtail ) )+ ) )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:101:3: ^( TEE ( ^( SUBFLOW flowtail ) )+ )
            {
            match(input,TEE,FOLLOW_TEE_in_tee276); 


                    flow.startTee();
                    //System.out.println("start tee");
                   

            match(input, Token.DOWN, null); 
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:107:5: ( ^( SUBFLOW flowtail ) )+
            int cnt7=0;
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==SUBFLOW) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:108:7: ^( SUBFLOW flowtail )
            	    {
            	    match(input,SUBFLOW,FOLLOW_SUBFLOW_in_tee301); 

            	    match(input, Token.DOWN, null); 
            	    pushFollow(FOLLOW_flowtail_in_tee303);
            	    flowtail();

            	    state._fsp--;


            	    match(input, Token.UP, null); 



            	           flow.endSubFlow();
            	           // System.out.println("end subflow");
            	          

            	    }
            	    break;

            	default :
            	    if ( cnt7 >= 1 ) break loop7;
                        EarlyExitException eee =
                            new EarlyExitException(7, input);
                        throw eee;
                }
                cnt7++;
            } while (true);


            match(input, Token.UP, null); 



               flow.endTee();
               //System.out.println("end tee");
              

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "tee"



    // $ANTLR start "flowtail"
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:123:1: flowtail : ( pipe | tee )+ ;
    public final void flowtail() throws RecognitionException {
        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:124:3: ( ( pipe | tee )+ )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:125:3: ( pipe | tee )+
            {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:125:3: ( pipe | tee )+
            int cnt8=0;
            loop8:
            do {
                int alt8=3;
                int LA8_0 = input.LA(1);

                if ( (LA8_0==QualifiedName) ) {
                    alt8=1;
                }
                else if ( (LA8_0==TEE) ) {
                    alt8=2;
                }


                switch (alt8) {
            	case 1 :
            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:126:5: pipe
            	    {
            	    pushFollow(FOLLOW_pipe_in_flowtail359);
            	    pipe();

            	    state._fsp--;


            	    }
            	    break;
            	case 2 :
            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:127:7: tee
            	    {
            	    pushFollow(FOLLOW_tee_in_flowtail367);
            	    tee();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt8 >= 1 ) break loop8;
                        EarlyExitException eee =
                            new EarlyExitException(8, input);
                        throw eee;
                }
                cnt8++;
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "flowtail"



    // $ANTLR start "exp"
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:131:1: exp returns [String value] : (s= StringLiteral |id= Identifier | ^( '+' e1= exp e2= exp ) );
    public final String exp() throws RecognitionException {
        String value = null;


        CommonTree s=null;
        CommonTree id=null;
        String e1 =null;

        String e2 =null;


        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:132:3: (s= StringLiteral |id= Identifier | ^( '+' e1= exp e2= exp ) )
            int alt9=3;
            switch ( input.LA(1) ) {
            case StringLiteral:
                {
                alt9=1;
                }
                break;
            case Identifier:
                {
                alt9=2;
                }
                break;
            case 25:
                {
                alt9=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;

            }

            switch (alt9) {
                case 1 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:133:3: s= StringLiteral
                    {
                    s=(CommonTree)match(input,StringLiteral,FOLLOW_StringLiteral_in_exp393); 


                                      value = (s!=null?s.getText():null);
                                     

                    }
                    break;
                case 2 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:137:5: id= Identifier
                    {
                    id=(CommonTree)match(input,Identifier,FOLLOW_Identifier_in_exp421); 


                                      value = vars.get((id!=null?id.getText():null));
                                      if (value == null) {
                                      	throw new FluxParseException("Variable " + (id!=null?id.getText():null) + " not assigned.");
                                      }
                                     

                    }
                    break;
                case 3 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:145:3: ^( '+' e1= exp e2= exp )
                    {
                    match(input,25,FOLLOW_25_in_exp450); 

                    match(input, Token.DOWN, null); 
                    pushFollow(FOLLOW_exp_in_exp454);
                    e1=exp();

                    state._fsp--;


                    pushFollow(FOLLOW_exp_in_exp458);
                    e2=exp();

                    state._fsp--;


                    match(input, Token.UP, null); 



                       value = e1 + e2;
                      

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "exp"



    // $ANTLR start "pipe"
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:152:1: pipe : ^(name= QualifiedName (e= exp )? ( VarRef )? (a= arg )* ) ;
    public final void pipe() throws RecognitionException {
        CommonTree name=null;
        String e =null;

        FlowBuilder.arg_return a =null;



        final Map<String, String> namedArgs = new HashMap<String, String>();
        final List<Object> cArgs = new ArrayList<Object>();

        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:157:3: ( ^(name= QualifiedName (e= exp )? ( VarRef )? (a= arg )* ) )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:158:3: ^(name= QualifiedName (e= exp )? ( VarRef )? (a= arg )* )
            {
            name=(CommonTree)match(input,QualifiedName,FOLLOW_QualifiedName_in_pipe494); 

            if ( input.LA(1)==Token.DOWN ) {
                match(input, Token.DOWN, null); 
                // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:160:5: (e= exp )?
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==Identifier||LA10_0==StringLiteral||LA10_0==25) ) {
                    alt10=1;
                }
                switch (alt10) {
                    case 1 :
                        // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:161:7: e= exp
                        {
                        pushFollow(FOLLOW_exp_in_pipe510);
                        e=exp();

                        state._fsp--;



                                    cArgs.add(e);
                                   

                        }
                        break;

                }


                // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:166:5: ( VarRef )?
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( (LA11_0==VarRef) ) {
                    alt11=1;
                }
                switch (alt11) {
                    case 1 :
                        // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:167:7: VarRef
                        {
                        match(input,VarRef,FOLLOW_VarRef_in_pipe545); 


                                     cArgs.add(Collections.unmodifiableMap(vars));
                                    

                        }
                        break;

                }


                // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:172:5: (a= arg )*
                loop12:
                do {
                    int alt12=2;
                    int LA12_0 = input.LA(1);

                    if ( (LA12_0==ARG) ) {
                        alt12=1;
                    }


                    switch (alt12) {
                	case 1 :
                	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:173:7: a= arg
                	    {
                	    pushFollow(FOLLOW_arg_in_pipe583);
                	    a=arg();

                	    state._fsp--;



                	                namedArgs.put((a!=null?a.key:null), (a!=null?a.value:null));
                	               

                	    }
                	    break;

                	default :
                	    break loop12;
                    }
                } while (true);


                match(input, Token.UP, null); 
            }



               flow.addElement(flow.createElement((name!=null?name.getText():null), namedArgs, cArgs));
              

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "pipe"


    public static class arg_return extends TreeRuleReturnScope {
        public String key;
        public String value;
    };


    // $ANTLR start "arg"
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:185:1: arg returns [String key, String value] : ^( ARG k= Identifier e= exp ) ;
    public final FlowBuilder.arg_return arg() throws RecognitionException {
        FlowBuilder.arg_return retval = new FlowBuilder.arg_return();
        retval.start = input.LT(1);


        CommonTree k=null;
        String e =null;


        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:186:3: ( ^( ARG k= Identifier e= exp ) )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:187:3: ^( ARG k= Identifier e= exp )
            {
            match(input,ARG,FOLLOW_ARG_in_arg636); 

            match(input, Token.DOWN, null); 
            k=(CommonTree)match(input,Identifier,FOLLOW_Identifier_in_arg640); 

            pushFollow(FOLLOW_exp_in_arg644);
            e=exp();

            state._fsp--;


            match(input, Token.UP, null); 



               retval.key = (k!=null?k.getText():null);
               retval.value = e;
              

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "arg"

    // Delegated rules


 

    public static final BitSet FOLLOW_varDefs_in_flux72 = new BitSet(new long[]{0x0000000002060402L});
    public static final BitSet FOLLOW_flow_in_flux82 = new BitSet(new long[]{0x0000000002060402L});
    public static final BitSet FOLLOW_StdIn_in_flow124 = new BitSet(new long[]{0x0000000000084000L});
    public static final BitSet FOLLOW_exp_in_flow146 = new BitSet(new long[]{0x0000000000084000L});
    public static final BitSet FOLLOW_flowtail_in_flow168 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varDef_in_varDefs183 = new BitSet(new long[]{0x0000000000000062L});
    public static final BitSet FOLLOW_ASSIGN_in_varDef212 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_Identifier_in_varDef216 = new BitSet(new long[]{0x0000000002040408L});
    public static final BitSet FOLLOW_exp_in_varDef220 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_DEFAULT_in_varDef238 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_Identifier_in_varDef242 = new BitSet(new long[]{0x0000000002040408L});
    public static final BitSet FOLLOW_exp_in_varDef246 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_TEE_in_tee276 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_SUBFLOW_in_tee301 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_flowtail_in_tee303 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_pipe_in_flowtail359 = new BitSet(new long[]{0x0000000000084002L});
    public static final BitSet FOLLOW_tee_in_flowtail367 = new BitSet(new long[]{0x0000000000084002L});
    public static final BitSet FOLLOW_StringLiteral_in_exp393 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_exp421 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_25_in_exp450 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_exp_in_exp454 = new BitSet(new long[]{0x0000000002040400L});
    public static final BitSet FOLLOW_exp_in_exp458 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_QualifiedName_in_pipe494 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_exp_in_pipe510 = new BitSet(new long[]{0x0000000000200018L});
    public static final BitSet FOLLOW_VarRef_in_pipe545 = new BitSet(new long[]{0x0000000000000018L});
    public static final BitSet FOLLOW_arg_in_pipe583 = new BitSet(new long[]{0x0000000000000018L});
    public static final BitSet FOLLOW_ARG_in_arg636 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_Identifier_in_arg640 = new BitSet(new long[]{0x0000000002040400L});
    public static final BitSet FOLLOW_exp_in_arg644 = new BitSet(new long[]{0x0000000000000008L});

}