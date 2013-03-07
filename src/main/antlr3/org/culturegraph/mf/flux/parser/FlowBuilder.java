// $ANTLR 3.5 D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g 2013-03-07 15:28:41

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

@SuppressWarnings("all")
public class FlowBuilder extends TreeParser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "ARG", "ASSIGN", "DEFAULT", "Digit", 
		"EscapeSequence", "HexDigit", "Identifier", "LINE_COMMENT", "Letter", 
		"OctalEscape", "QualifiedName", "SUBFLOW", "StartString", "StdIn", "StringLiteral", 
		"TEE", "UnicodeEscape", "VarRef", "WS", "'('", "')'", "'+'", "','", "'.'", 
		"';'", "'='", "'default '", "'{'", "'|'", "'}'"
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

	@Override public String[] getTokenNames() { return FlowBuilder.tokenNames; }
	@Override public String getGrammarFileName() { return "D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g"; }


	Flow flow = new Flow();
	Map<String, String> vars = new HashMap<String, String>();

	public final void addVaribleAssignements(final Map<String, String> vars) {
		this.vars.putAll(vars);
	}



	// $ANTLR start "flux"
	// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:45:1: flux returns [Flow flow] : varDefs mainflow ;
	public final Flow flux() throws RecognitionException {
		Flow flow = null;


		try {
			// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:46:3: ( varDefs mainflow )
			// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:47:3: varDefs mainflow
			{
			pushFollow(FOLLOW_varDefs_in_flux72);
			varDefs();
			state._fsp--;

			pushFollow(FOLLOW_mainflow_in_flux74);
			mainflow();
			state._fsp--;


			                    flow = this.flow;
			                   
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return flow;
	}
	// $ANTLR end "flux"



	// $ANTLR start "varDefs"
	// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:53:1: varDefs : ( varDef )* ;
	public final void varDefs() throws RecognitionException {
		try {
			// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:54:3: ( ( varDef )* )
			// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:55:3: ( varDef )*
			{
			// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:55:3: ( varDef )*
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( ((LA1_0 >= ASSIGN && LA1_0 <= DEFAULT)) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:55:3: varDef
					{
					pushFollow(FOLLOW_varDef_in_varDefs111);
					varDef();
					state._fsp--;

					}
					break;

				default :
					break loop1;
				}
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
	}
	// $ANTLR end "varDefs"



	// $ANTLR start "varDef"
	// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:61:1: varDef : ( ^( ASSIGN name= Identifier (e= exp )? ) | ^( DEFAULT name= Identifier (e= exp )? ) );
	public final void varDef() throws RecognitionException {
		CommonTree name=null;
		String e =null;

		try {
			// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:62:3: ( ^( ASSIGN name= Identifier (e= exp )? ) | ^( DEFAULT name= Identifier (e= exp )? ) )
			int alt4=2;
			int LA4_0 = input.LA(1);
			if ( (LA4_0==ASSIGN) ) {
				alt4=1;
			}
			else if ( (LA4_0==DEFAULT) ) {
				alt4=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 4, 0, input);
				throw nvae;
			}

			switch (alt4) {
				case 1 :
					// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:63:3: ^( ASSIGN name= Identifier (e= exp )? )
					{
					match(input,ASSIGN,FOLLOW_ASSIGN_in_varDef141); 
					match(input, Token.DOWN, null); 
					name=(CommonTree)match(input,Identifier,FOLLOW_Identifier_in_varDef145); 
					// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:63:29: (e= exp )?
					int alt2=2;
					int LA2_0 = input.LA(1);
					if ( (LA2_0==Identifier||LA2_0==StringLiteral||LA2_0==25) ) {
						alt2=1;
					}
					switch (alt2) {
						case 1 :
							// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:63:29: e= exp
							{
							pushFollow(FOLLOW_exp_in_varDef149);
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
					// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:69:3: ^( DEFAULT name= Identifier (e= exp )? )
					{
					match(input,DEFAULT,FOLLOW_DEFAULT_in_varDef168); 
					match(input, Token.DOWN, null); 
					name=(CommonTree)match(input,Identifier,FOLLOW_Identifier_in_varDef172); 
					// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:69:30: (e= exp )?
					int alt3=2;
					int LA3_0 = input.LA(1);
					if ( (LA3_0==Identifier||LA3_0==StringLiteral||LA3_0==25) ) {
						alt3=1;
					}
					switch (alt3) {
						case 1 :
							// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:69:30: e= exp
							{
							pushFollow(FOLLOW_exp_in_varDef176);
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
	}
	// $ANTLR end "varDef"



	// $ANTLR start "mainflow"
	// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:78:1: mainflow : ( StdIn |e= exp ) flow ;
	public final void mainflow() throws RecognitionException {
		String e =null;

		try {
			// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:79:3: ( ( StdIn |e= exp ) flow )
			// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:80:3: ( StdIn |e= exp ) flow
			{
			// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:80:3: ( StdIn |e= exp )
			int alt5=2;
			int LA5_0 = input.LA(1);
			if ( (LA5_0==StdIn) ) {
				alt5=1;
			}
			else if ( (LA5_0==Identifier||LA5_0==StringLiteral||LA5_0==25) ) {
				alt5=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 5, 0, input);
				throw nvae;
			}

			switch (alt5) {
				case 1 :
					// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:81:5: StdIn
					{
					match(input,StdIn,FOLLOW_StdIn_in_mainflow207); 

					           flow.setStdInStart();
					          
					}
					break;
				case 2 :
					// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:85:7: e= exp
					{
					pushFollow(FOLLOW_exp_in_mainflow230);
					e=exp();
					state._fsp--;


					             flow.setStringStart(e);
					            
					}
					break;

			}

			pushFollow(FOLLOW_flow_in_mainflow253);
			flow();
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
	}
	// $ANTLR end "mainflow"



	// $ANTLR start "tee"
	// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:93:1: tee : ^( TEE ( ^( SUBFLOW flow ) )+ ) ;
	public final void tee() throws RecognitionException {
		try {
			// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:94:3: ( ^( TEE ( ^( SUBFLOW flow ) )+ ) )
			// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:95:3: ^( TEE ( ^( SUBFLOW flow ) )+ )
			{
			match(input,TEE,FOLLOW_TEE_in_tee274); 

			         flow.startTee();
			         //System.out.println("start tee");
			        
			match(input, Token.DOWN, null); 
			// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:101:5: ( ^( SUBFLOW flow ) )+
			int cnt6=0;
			loop6:
			while (true) {
				int alt6=2;
				int LA6_0 = input.LA(1);
				if ( (LA6_0==SUBFLOW) ) {
					alt6=1;
				}

				switch (alt6) {
				case 1 :
					// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:102:7: ^( SUBFLOW flow )
					{
					match(input,SUBFLOW,FOLLOW_SUBFLOW_in_tee300); 
					match(input, Token.DOWN, null); 
					pushFollow(FOLLOW_flow_in_tee302);
					flow();
					state._fsp--;

					match(input, Token.UP, null); 


					        flow.endSubFlow();
					        // System.out.println("end subflow");
					       
					}
					break;

				default :
					if ( cnt6 >= 1 ) break loop6;
					EarlyExitException eee = new EarlyExitException(6, input);
					throw eee;
				}
				cnt6++;
			}

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
	}
	// $ANTLR end "tee"



	// $ANTLR start "flow"
	// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:117:1: flow : ( pipe | tee )+ ;
	public final void flow() throws RecognitionException {
		try {
			// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:118:3: ( ( pipe | tee )+ )
			// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:119:3: ( pipe | tee )+
			{
			// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:119:3: ( pipe | tee )+
			int cnt7=0;
			loop7:
			while (true) {
				int alt7=3;
				int LA7_0 = input.LA(1);
				if ( (LA7_0==QualifiedName) ) {
					alt7=1;
				}
				else if ( (LA7_0==TEE) ) {
					alt7=2;
				}

				switch (alt7) {
				case 1 :
					// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:120:5: pipe
					{
					pushFollow(FOLLOW_pipe_in_flow360);
					pipe();
					state._fsp--;

					}
					break;
				case 2 :
					// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:121:7: tee
					{
					pushFollow(FOLLOW_tee_in_flow368);
					tee();
					state._fsp--;

					}
					break;

				default :
					if ( cnt7 >= 1 ) break loop7;
					EarlyExitException eee = new EarlyExitException(7, input);
					throw eee;
				}
				cnt7++;
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
	}
	// $ANTLR end "flow"



	// $ANTLR start "exp"
	// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:125:1: exp returns [String value] : (s= StringLiteral |id= Identifier | ^( '+' e1= exp e2= exp ) );
	public final String exp() throws RecognitionException {
		String value = null;


		CommonTree s=null;
		CommonTree id=null;
		String e1 =null;
		String e2 =null;

		try {
			// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:126:3: (s= StringLiteral |id= Identifier | ^( '+' e1= exp e2= exp ) )
			int alt8=3;
			switch ( input.LA(1) ) {
			case StringLiteral:
				{
				alt8=1;
				}
				break;
			case Identifier:
				{
				alt8=2;
				}
				break;
			case 25:
				{
				alt8=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 8, 0, input);
				throw nvae;
			}
			switch (alt8) {
				case 1 :
					// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:127:3: s= StringLiteral
					{
					s=(CommonTree)match(input,StringLiteral,FOLLOW_StringLiteral_in_exp394); 

					                   value = (s!=null?s.getText():null);
					                  
					}
					break;
				case 2 :
					// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:131:5: id= Identifier
					{
					id=(CommonTree)match(input,Identifier,FOLLOW_Identifier_in_exp423); 

					                   value = vars.get((id!=null?id.getText():null));
					                   if (value == null) {
					                   	throw new FluxParseException("Variable " + (id!=null?id.getText():null) + " not assigned.");
					                   }
					                  
					}
					break;
				case 3 :
					// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:139:3: ^( '+' e1= exp e2= exp )
					{
					match(input,25,FOLLOW_25_in_exp453); 
					match(input, Token.DOWN, null); 
					pushFollow(FOLLOW_exp_in_exp457);
					e1=exp();
					state._fsp--;

					pushFollow(FOLLOW_exp_in_exp461);
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
	// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:146:1: pipe : ^(name= QualifiedName (e= exp )? ( VarRef )? (a= arg )* ) ;
	public final void pipe() throws RecognitionException {
		CommonTree name=null;
		String e =null;
		TreeRuleReturnScope a =null;


		final Map<String, String> namedArgs = new HashMap<String, String>();
		final List<Object> cArgs = new ArrayList<Object>();

		try {
			// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:151:3: ( ^(name= QualifiedName (e= exp )? ( VarRef )? (a= arg )* ) )
			// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:152:3: ^(name= QualifiedName (e= exp )? ( VarRef )? (a= arg )* )
			{
			name=(CommonTree)match(input,QualifiedName,FOLLOW_QualifiedName_in_pipe498); 
			if ( input.LA(1)==Token.DOWN ) {
				match(input, Token.DOWN, null); 
				// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:154:5: (e= exp )?
				int alt9=2;
				int LA9_0 = input.LA(1);
				if ( (LA9_0==Identifier||LA9_0==StringLiteral||LA9_0==25) ) {
					alt9=1;
				}
				switch (alt9) {
					case 1 :
						// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:155:7: e= exp
						{
						pushFollow(FOLLOW_exp_in_pipe514);
						e=exp();
						state._fsp--;


						             cArgs.add(e);
						            
						}
						break;

				}

				// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:160:5: ( VarRef )?
				int alt10=2;
				int LA10_0 = input.LA(1);
				if ( (LA10_0==VarRef) ) {
					alt10=1;
				}
				switch (alt10) {
					case 1 :
						// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:161:7: VarRef
						{
						match(input,VarRef,FOLLOW_VarRef_in_pipe550); 

						              cArgs.add(Collections.unmodifiableMap(vars));
						             
						}
						break;

				}

				// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:166:5: (a= arg )*
				loop11:
				while (true) {
					int alt11=2;
					int LA11_0 = input.LA(1);
					if ( (LA11_0==ARG) ) {
						alt11=1;
					}

					switch (alt11) {
					case 1 :
						// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:167:7: a= arg
						{
						pushFollow(FOLLOW_arg_in_pipe589);
						a=arg();
						state._fsp--;


						             namedArgs.put((a!=null?((FlowBuilder.arg_return)a).key:null), (a!=null?((FlowBuilder.arg_return)a).value:null));
						            
						}
						break;

					default :
						break loop11;
					}
				}

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
	}
	// $ANTLR end "pipe"


	public static class arg_return extends TreeRuleReturnScope {
		public String key;
		public String value;
	};


	// $ANTLR start "arg"
	// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:179:1: arg returns [String key, String value] : ^( ARG k= Identifier e= exp ) ;
	public final FlowBuilder.arg_return arg() throws RecognitionException {
		FlowBuilder.arg_return retval = new FlowBuilder.arg_return();
		retval.start = input.LT(1);

		CommonTree k=null;
		String e =null;

		try {
			// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:180:3: ( ^( ARG k= Identifier e= exp ) )
			// D:\\Git\\metafacture-core\\src\\main\\antlr3\\org\\culturegraph\\mf\\flux\\parser\\FlowBuilder.g:181:3: ^( ARG k= Identifier e= exp )
			{
			match(input,ARG,FOLLOW_ARG_in_arg644); 
			match(input, Token.DOWN, null); 
			k=(CommonTree)match(input,Identifier,FOLLOW_Identifier_in_arg648); 
			pushFollow(FOLLOW_exp_in_arg652);
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



	public static final BitSet FOLLOW_varDefs_in_flux72 = new BitSet(new long[]{0x0000000002060400L});
	public static final BitSet FOLLOW_mainflow_in_flux74 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_varDef_in_varDefs111 = new BitSet(new long[]{0x0000000000000062L});
	public static final BitSet FOLLOW_ASSIGN_in_varDef141 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_Identifier_in_varDef145 = new BitSet(new long[]{0x0000000002040408L});
	public static final BitSet FOLLOW_exp_in_varDef149 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_DEFAULT_in_varDef168 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_Identifier_in_varDef172 = new BitSet(new long[]{0x0000000002040408L});
	public static final BitSet FOLLOW_exp_in_varDef176 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_StdIn_in_mainflow207 = new BitSet(new long[]{0x0000000000084000L});
	public static final BitSet FOLLOW_exp_in_mainflow230 = new BitSet(new long[]{0x0000000000084000L});
	public static final BitSet FOLLOW_flow_in_mainflow253 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TEE_in_tee274 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_SUBFLOW_in_tee300 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_flow_in_tee302 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_pipe_in_flow360 = new BitSet(new long[]{0x0000000000084002L});
	public static final BitSet FOLLOW_tee_in_flow368 = new BitSet(new long[]{0x0000000000084002L});
	public static final BitSet FOLLOW_StringLiteral_in_exp394 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_Identifier_in_exp423 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_25_in_exp453 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_exp_in_exp457 = new BitSet(new long[]{0x0000000002040400L});
	public static final BitSet FOLLOW_exp_in_exp461 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_QualifiedName_in_pipe498 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_exp_in_pipe514 = new BitSet(new long[]{0x0000000000200018L});
	public static final BitSet FOLLOW_VarRef_in_pipe550 = new BitSet(new long[]{0x0000000000000018L});
	public static final BitSet FOLLOW_arg_in_pipe589 = new BitSet(new long[]{0x0000000000000018L});
	public static final BitSet FOLLOW_ARG_in_arg644 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_Identifier_in_arg648 = new BitSet(new long[]{0x0000000002040400L});
	public static final BitSet FOLLOW_exp_in_arg652 = new BitSet(new long[]{0x0000000000000008L});
}
