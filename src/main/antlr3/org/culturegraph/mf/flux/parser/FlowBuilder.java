// $ANTLR 3.4 D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g 2013-06-17 09:35:26

package org.culturegraph.mf.flux.parser;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import org.culturegraph.mf.flux.parser.FluxProgramm;
import org.culturegraph.mf.exceptions.FluxParseException;


import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class FlowBuilder extends TreeParser {
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


    private FluxProgramm flux = new FluxProgramm();
    private Map<String, String> vars = new HashMap<String, String>();

    public final void addVaribleAssignements(final Map<String, String> vars) {
    	this.vars.putAll(vars);
    }



    // $ANTLR start "flux"
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:45:1: flux returns [FluxProgramm retValue = flux] : varDefs ( flow )* ;
    public final FluxProgramm flux() throws RecognitionException {
        FluxProgramm retValue =  flux;


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

                if ( (LA1_0==Identifier||(LA1_0 >= StdIn && LA1_0 <= StringLiteral)||LA1_0==Wormhole||LA1_0==26) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:49:5: flow
            	    {
            	    pushFollow(FOLLOW_flow_in_flux82);
            	    flow();

            	    state._fsp--;



            	             flux.nextFlow();
            	            

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);



               flux.compile();
              

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retValue;
    }
    // $ANTLR end "flux"



    // $ANTLR start "flow"
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:60:1: flow : ( StdIn |e= exp |ws= Wormhole ) flowtail (we= Wormhole )? ;
    public final void flow() throws RecognitionException {
        CommonTree ws=null;
        CommonTree we=null;
        String e =null;


        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:61:3: ( ( StdIn |e= exp |ws= Wormhole ) flowtail (we= Wormhole )? )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:62:3: ( StdIn |e= exp |ws= Wormhole ) flowtail (we= Wormhole )?
            {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:62:3: ( StdIn |e= exp |ws= Wormhole )
            int alt2=3;
            switch ( input.LA(1) ) {
            case StdIn:
                {
                alt2=1;
                }
                break;
            case Identifier:
            case StringLiteral:
            case 26:
                {
                alt2=2;
                }
                break;
            case Wormhole:
                {
                alt2=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;

            }

            switch (alt2) {
                case 1 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:63:5: StdIn
                    {
                    match(input,StdIn,FOLLOW_StdIn_in_flow126); 


                              flux.setStdInStart();
                             

                    }
                    break;
                case 2 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:67:7: e= exp
                    {
                    pushFollow(FOLLOW_exp_in_flow148);
                    e=exp();

                    state._fsp--;



                                flux.setStringStart(e);
                               

                    }
                    break;
                case 3 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:71:7: ws= Wormhole
                    {
                    ws=(CommonTree)match(input,Wormhole,FOLLOW_Wormhole_in_flow172); 


                                      flux.setWormholeStart((ws!=null?ws.getText():null));
                                     

                    }
                    break;

            }


            pushFollow(FOLLOW_flowtail_in_flow200);
            flowtail();

            state._fsp--;


            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:77:3: (we= Wormhole )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==Wormhole) ) {
                int LA3_1 = input.LA(2);

                if ( (LA3_1==EOF||LA3_1==Identifier||(LA3_1 >= StdIn && LA3_1 <= StringLiteral)||LA3_1==Wormhole||LA3_1==26) ) {
                    alt3=1;
                }
            }
            switch (alt3) {
                case 1 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:78:5: we= Wormhole
                    {
                    we=(CommonTree)match(input,Wormhole,FOLLOW_Wormhole_in_flow212); 


                                    flux.setWormholeEnd((we!=null?we.getText():null));
                                   

                    }
                    break;

            }


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
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:85:1: varDefs : ( varDef )* ;
    public final void varDefs() throws RecognitionException {
        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:86:3: ( ( varDef )* )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:87:3: ( varDef )*
            {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:87:3: ( varDef )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0 >= ASSIGN && LA4_0 <= DEFAULT)) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:87:3: varDef
            	    {
            	    pushFollow(FOLLOW_varDef_in_varDefs250);
            	    varDef();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop4;
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
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:90:1: varDef : ( ^( ASSIGN name= Identifier (e= exp )? ) | ^( DEFAULT name= Identifier (e= exp )? ) );
    public final void varDef() throws RecognitionException {
        CommonTree name=null;
        String e =null;


        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:91:3: ( ^( ASSIGN name= Identifier (e= exp )? ) | ^( DEFAULT name= Identifier (e= exp )? ) )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==ASSIGN) ) {
                alt7=1;
            }
            else if ( (LA7_0==DEFAULT) ) {
                alt7=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;

            }
            switch (alt7) {
                case 1 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:92:3: ^( ASSIGN name= Identifier (e= exp )? )
                    {
                    match(input,ASSIGN,FOLLOW_ASSIGN_in_varDef267); 

                    match(input, Token.DOWN, null); 
                    name=(CommonTree)match(input,Identifier,FOLLOW_Identifier_in_varDef271); 

                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:92:29: (e= exp )?
                    int alt5=2;
                    int LA5_0 = input.LA(1);

                    if ( (LA5_0==Identifier||LA5_0==StringLiteral||LA5_0==26) ) {
                        alt5=1;
                    }
                    switch (alt5) {
                        case 1 :
                            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:92:29: e= exp
                            {
                            pushFollow(FOLLOW_exp_in_varDef275);
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
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:98:3: ^( DEFAULT name= Identifier (e= exp )? )
                    {
                    match(input,DEFAULT,FOLLOW_DEFAULT_in_varDef293); 

                    match(input, Token.DOWN, null); 
                    name=(CommonTree)match(input,Identifier,FOLLOW_Identifier_in_varDef297); 

                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:98:30: (e= exp )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0==Identifier||LA6_0==StringLiteral||LA6_0==26) ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:98:30: e= exp
                            {
                            pushFollow(FOLLOW_exp_in_varDef301);
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
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:107:1: tee : ^( TEE ( ^( SUBFLOW flowtail ) )+ ) ;
    public final void tee() throws RecognitionException {
        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:108:3: ( ^( TEE ( ^( SUBFLOW flowtail ) )+ ) )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:109:3: ^( TEE ( ^( SUBFLOW flowtail ) )+ )
            {
            match(input,TEE,FOLLOW_TEE_in_tee331); 


                    flux.startTee();
                    //System.out.println("start tee");
                   

            match(input, Token.DOWN, null); 
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:115:5: ( ^( SUBFLOW flowtail ) )+
            int cnt8=0;
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0==SUBFLOW) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:116:7: ^( SUBFLOW flowtail )
            	    {
            	    match(input,SUBFLOW,FOLLOW_SUBFLOW_in_tee356); 

            	    match(input, Token.DOWN, null); 
            	    pushFollow(FOLLOW_flowtail_in_tee358);
            	    flowtail();

            	    state._fsp--;


            	    match(input, Token.UP, null); 



            	           flux.endSubFlow();
            	           // System.out.println("end subflow");
            	          

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


            match(input, Token.UP, null); 



               flux.endTee();
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
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:131:1: flowtail : ( pipe | tee )+ ;
    public final void flowtail() throws RecognitionException {
        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:132:3: ( ( pipe | tee )+ )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:133:3: ( pipe | tee )+
            {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:133:3: ( pipe | tee )+
            int cnt9=0;
            loop9:
            do {
                int alt9=3;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==QualifiedName) ) {
                    alt9=1;
                }
                else if ( (LA9_0==TEE) ) {
                    alt9=2;
                }


                switch (alt9) {
            	case 1 :
            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:134:5: pipe
            	    {
            	    pushFollow(FOLLOW_pipe_in_flowtail414);
            	    pipe();

            	    state._fsp--;


            	    }
            	    break;
            	case 2 :
            	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:135:7: tee
            	    {
            	    pushFollow(FOLLOW_tee_in_flowtail422);
            	    tee();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt9 >= 1 ) break loop9;
                        EarlyExitException eee =
                            new EarlyExitException(9, input);
                        throw eee;
                }
                cnt9++;
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
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:139:1: exp returns [String value] : (s= StringLiteral |id= Identifier | ^( '+' e1= exp e2= exp ) );
    public final String exp() throws RecognitionException {
        String value = null;


        CommonTree s=null;
        CommonTree id=null;
        String e1 =null;

        String e2 =null;


        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:140:3: (s= StringLiteral |id= Identifier | ^( '+' e1= exp e2= exp ) )
            int alt10=3;
            switch ( input.LA(1) ) {
            case StringLiteral:
                {
                alt10=1;
                }
                break;
            case Identifier:
                {
                alt10=2;
                }
                break;
            case 26:
                {
                alt10=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;

            }

            switch (alt10) {
                case 1 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:141:3: s= StringLiteral
                    {
                    s=(CommonTree)match(input,StringLiteral,FOLLOW_StringLiteral_in_exp448); 


                                      value = (s!=null?s.getText():null);
                                     

                    }
                    break;
                case 2 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:145:5: id= Identifier
                    {
                    id=(CommonTree)match(input,Identifier,FOLLOW_Identifier_in_exp476); 


                                      value = vars.get((id!=null?id.getText():null));
                                      if (value == null) {
                                      	throw new FluxParseException("Variable " + (id!=null?id.getText():null) + " not assigned.");
                                      }
                                     

                    }
                    break;
                case 3 :
                    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:153:3: ^( '+' e1= exp e2= exp )
                    {
                    match(input,26,FOLLOW_26_in_exp505); 

                    match(input, Token.DOWN, null); 
                    pushFollow(FOLLOW_exp_in_exp509);
                    e1=exp();

                    state._fsp--;


                    pushFollow(FOLLOW_exp_in_exp513);
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
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:160:1: pipe : ^(name= QualifiedName (e= exp )? ( VarRef )? (a= arg )* ) ;
    public final void pipe() throws RecognitionException {
        CommonTree name=null;
        String e =null;

        FlowBuilder.arg_return a =null;



        final Map<String, String> namedArgs = new HashMap<String, String>();
        final List<Object> cArgs = new ArrayList<Object>();

        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:165:3: ( ^(name= QualifiedName (e= exp )? ( VarRef )? (a= arg )* ) )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:166:3: ^(name= QualifiedName (e= exp )? ( VarRef )? (a= arg )* )
            {
            name=(CommonTree)match(input,QualifiedName,FOLLOW_QualifiedName_in_pipe549); 

            if ( input.LA(1)==Token.DOWN ) {
                match(input, Token.DOWN, null); 
                // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:168:5: (e= exp )?
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( (LA11_0==Identifier||LA11_0==StringLiteral||LA11_0==26) ) {
                    alt11=1;
                }
                switch (alt11) {
                    case 1 :
                        // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:169:7: e= exp
                        {
                        pushFollow(FOLLOW_exp_in_pipe565);
                        e=exp();

                        state._fsp--;



                                    cArgs.add(e);
                                   

                        }
                        break;

                }


                // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:174:5: ( VarRef )?
                int alt12=2;
                int LA12_0 = input.LA(1);

                if ( (LA12_0==VarRef) ) {
                    alt12=1;
                }
                switch (alt12) {
                    case 1 :
                        // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:175:7: VarRef
                        {
                        match(input,VarRef,FOLLOW_VarRef_in_pipe600); 


                                     cArgs.add(Collections.unmodifiableMap(vars));
                                    

                        }
                        break;

                }


                // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:180:5: (a= arg )*
                loop13:
                do {
                    int alt13=2;
                    int LA13_0 = input.LA(1);

                    if ( (LA13_0==ARG) ) {
                        alt13=1;
                    }


                    switch (alt13) {
                	case 1 :
                	    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:181:7: a= arg
                	    {
                	    pushFollow(FOLLOW_arg_in_pipe638);
                	    a=arg();

                	    state._fsp--;



                	                namedArgs.put((a!=null?a.key:null), (a!=null?a.value:null));
                	               

                	    }
                	    break;

                	default :
                	    break loop13;
                    }
                } while (true);


                match(input, Token.UP, null); 
            }



               flux.addElement((name!=null?name.getText():null), namedArgs, cArgs);
              

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
    // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:193:1: arg returns [String key, String value] : ^( ARG k= Identifier e= exp ) ;
    public final FlowBuilder.arg_return arg() throws RecognitionException {
        FlowBuilder.arg_return retval = new FlowBuilder.arg_return();
        retval.start = input.LT(1);


        CommonTree k=null;
        String e =null;


        try {
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:194:3: ( ^( ARG k= Identifier e= exp ) )
            // D:\\git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:195:3: ^( ARG k= Identifier e= exp )
            {
            match(input,ARG,FOLLOW_ARG_in_arg691); 

            match(input, Token.DOWN, null); 
            k=(CommonTree)match(input,Identifier,FOLLOW_Identifier_in_arg695); 

            pushFollow(FOLLOW_exp_in_arg699);
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


 

    public static final BitSet FOLLOW_varDefs_in_flux72 = new BitSet(new long[]{0x0000000004860402L});
    public static final BitSet FOLLOW_flow_in_flux82 = new BitSet(new long[]{0x0000000004860402L});
    public static final BitSet FOLLOW_StdIn_in_flow126 = new BitSet(new long[]{0x0000000000084000L});
    public static final BitSet FOLLOW_exp_in_flow148 = new BitSet(new long[]{0x0000000000084000L});
    public static final BitSet FOLLOW_Wormhole_in_flow172 = new BitSet(new long[]{0x0000000000084000L});
    public static final BitSet FOLLOW_flowtail_in_flow200 = new BitSet(new long[]{0x0000000000800002L});
    public static final BitSet FOLLOW_Wormhole_in_flow212 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_varDef_in_varDefs250 = new BitSet(new long[]{0x0000000000000062L});
    public static final BitSet FOLLOW_ASSIGN_in_varDef267 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_Identifier_in_varDef271 = new BitSet(new long[]{0x0000000004040408L});
    public static final BitSet FOLLOW_exp_in_varDef275 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_DEFAULT_in_varDef293 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_Identifier_in_varDef297 = new BitSet(new long[]{0x0000000004040408L});
    public static final BitSet FOLLOW_exp_in_varDef301 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_TEE_in_tee331 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_SUBFLOW_in_tee356 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_flowtail_in_tee358 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_pipe_in_flowtail414 = new BitSet(new long[]{0x0000000000084002L});
    public static final BitSet FOLLOW_tee_in_flowtail422 = new BitSet(new long[]{0x0000000000084002L});
    public static final BitSet FOLLOW_StringLiteral_in_exp448 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_exp476 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_exp505 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_exp_in_exp509 = new BitSet(new long[]{0x0000000004040400L});
    public static final BitSet FOLLOW_exp_in_exp513 = new BitSet(new long[]{0x0000000000000008L});
    public static final BitSet FOLLOW_QualifiedName_in_pipe549 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_exp_in_pipe565 = new BitSet(new long[]{0x0000000000200018L});
    public static final BitSet FOLLOW_VarRef_in_pipe600 = new BitSet(new long[]{0x0000000000000018L});
    public static final BitSet FOLLOW_arg_in_pipe638 = new BitSet(new long[]{0x0000000000000018L});
    public static final BitSet FOLLOW_ARG_in_arg691 = new BitSet(new long[]{0x0000000000000004L});
    public static final BitSet FOLLOW_Identifier_in_arg695 = new BitSet(new long[]{0x0000000004040400L});
    public static final BitSet FOLLOW_exp_in_arg699 = new BitSet(new long[]{0x0000000000000008L});

}