package control;

import java.util.ArrayList;

import model.Token;

public class Parser {
	
	String[] classStructure = {"class", "<name>", "{", "<content>", "}"};
	String[] mainStructure = {"bool", "main", "(", ")", "{", "<commands>", "<return>", "}"};
	String[] methodStructure = {"<return_type>", "<name>", "(", "<parameters>", ")", "{", "<commands>", "<return>", "}"};
	String[] methodReturnStructure = {"<", ":", "<return>", ":", ">"};
	String[] forStructure = {"for", "(", "<initialization>", ";", "<condition>", ";", "<increment>", ")", "{", "<commands>", "}"};
	String[] ifStructure = {"if", "(", "<condition>", ")", "{", "<commands>", "}"};
	String[] elseStructure = {"else", "{", "<commands>", "}"};
	String[] printStructure = {"print", "(", "<content>", ")", ";"};
	String[] scanStructure = {"scan", "(", "<content>", ")", ";"};
	ArrayList<String> ifSyncTokens = new ArrayList<>();
	ArrayList<Token> tokensList; // lista de tokens recebida do lexico
	int index; // indice atual da lista de tokens
	ArrayList<String> errorsList; // lista de erros sintaticos
	
	public Parser(ArrayList<Token> tokensList) {
		this.tokensList = tokensList;
		errorsList = new ArrayList<String>();
		addIfSyncTokens();
	}
	
	public void addIfSyncTokens() {
		ifSyncTokens.add("int");
		ifSyncTokens.add("float");
		ifSyncTokens.add("bool");
		ifSyncTokens.add("string");
		ifSyncTokens.add("Identifier");
		ifSyncTokens.add("for");
		ifSyncTokens.add("if");
		ifSyncTokens.add("print");
		ifSyncTokens.add("scan");
		ifSyncTokens.add("+");
		ifSyncTokens.add("-");
		ifSyncTokens.add("*");
		ifSyncTokens.add("/");
		ifSyncTokens.add("%");
		ifSyncTokens.add(";");
		ifSyncTokens.add("else");
	}
	
	public boolean readTokens() {
		System.out.println("---------- INICIO DA ANALISE SINTATICA ----------");
		for (index = 0; index < tokensList.size(); index++) {
			if (tokensList.get(index).lexeme.equals("class")) { // declaracao de classe
				if (!recognizeClass()) {
					panicModeClass();
				} else {
					System.out.println("Classe correta na linha " + tokensList.get(index).line);
				}
			} else { // declaracao de variavel global
				if (tokensList.get(index).lexeme.equals("final")) { // pode ser final
					index++;
				}
				if (tokensToRead() &&  (isAtributeType() || tokensList.get(index).type.equals("ID"))) {
					readGlobalVariable();
				}
			}
		}
		for (int i = 0; i < errorsList.size(); i++) {
			System.out.println(errorsList.get(i));
		}
		return true;		
	}
	
	public boolean readGlobalVariable() {
		if (isAtributeType() || tokensList.get(index).type.equals("ID")) {
			index++;
			if (tokensToRead() && recognizeVector()) { // verifica se eh vetor ou matriz
				index++;
			}
			if (tokensToRead() && tokensList.get(index).type.equals("ID")) {
				index++;
				if (tokensToRead() && (tokensList.get(index).lexeme.equals(";") || tokensList.get(index).lexeme.equals(","))) {
					index--; // para comecar a varredura da estrutura de declaracao de variavel a partir do nome
					if (!recognizeVariableDeclaration()) {
						panicModeVariable();
					} else {
						System.out.println("Declaracao de variavel global correta na linha " + tokensList.get(index).line);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	
	public boolean recognizeClass() {
		boolean isCorrect = true;
		int classIndex = 0;
		while (classIndex < classStructure.length) {
			if (tokensToRead()) {
				if (classIndex == 1) { // verifica se eh um nome de classe valido
					if (!tokensList.get(index).type.equals("ID")) {
						isCorrect = false;
					}
					classIndex++;
					index++;
				} else if (classIndex == 2) {
					if (tokensList.get(index).lexeme.equals(":")) {
						index++;
						if (tokensToRead() && !tokensList.get(index).type.equals("ID")) {
							isCorrect = false;
						}
					} else if (!tokensList.get(index).lexeme.equals("{")) {
						isCorrect = false;
					} 
					if (tokensList.get(index).lexeme.equals("{")) {
						classIndex++;
					}
					index++;
				} else if (classIndex == 3) { // verifica se os comandos estao corretos
					if (tokensList.get(index).lexeme.equals("}")) { // se for um um "}" eh porque nao ha nenhum comando dentro da classe
						classIndex++;
					} else {
						while (tokensToRead() && recognizeClassContent()) { // enquanto houver comandos validos dentro da classe 
							index++;							
						}
						if (!tokensList.get(index).lexeme.equals("}")) { // nao ha mais comandos dentro da classe, achou o "}"
							index++;
						}
						classIndex++;
					}
				} else { // verifica se os demais tokens estao corretos
					if (!tokensList.get(index).lexeme.equals(classStructure[classIndex])) {
						isCorrect = false;
					}
					if (classIndex < 4) { // se nao for o ultimo token, avanca o indice
						index++;
					} 
					classIndex++;
				}
			}
			//if (!isCorrect) {
				//forIndex++;
			//}		
		}
		return isCorrect;
	}
	
	public void panicModeClass() {
		errorsList.add("ERRO: Classe mal formada na linha " + tokensList.get(index-1).line);
	}
	
	public boolean recognizeClassContent() {
		boolean isVector = false;
		if (isAtributeType() || tokensList.get(index).type.equals("ID")) {
			index++;
			if (tokensToRead() && recognizeVector()) { // verifica se eh vetor ou matriz
				isVector = true;
				index++;
			}
			
			if (tokensToRead() && tokensList.get(index).lexeme.equals("main") && isVector == false) {
				index--;
				if (!recognizeMain()) {
					panicModeMethod();
				} else {
					System.out.println("Main correta na linha " + tokensList.get(index).line);
					return true;
				}
			} else if (tokensToRead() && tokensList.get(index).type.equals("ID")) {
				index++;
				if (tokensToRead() && tokensList.get(index).lexeme.equals("(") && isVector == false) { // declaracao de metodo
					index = index - 2; // para comecar a varredura da estrutura do metodo a partir do tipo de retorno
					if (!recognizeMethod()) {
						panicModeMethod();
					} else {
						System.out.println("Metodo correto na linha " + tokensList.get(index).line);
						return true;
					}
				} else if (tokensList.get(index).lexeme.equals(";") || tokensList.get(index).lexeme.equals(",")) { // declaracao de variavel
					index--; // para comecar a varredura da estrutura de declaracao de variavel a partir do nome
					if (!recognizeVariableDeclaration()) {
						panicModeVariable();
					} else {
						System.out.println("Declaracao de variavel correta na linha " + tokensList.get(index).line);
						return true;
					}
				} 
			}
		}
		return false;
	}
	
	public boolean recognizeMain() {
		boolean isCorrect = true;
		int mainIndex = 0;
		while (mainIndex < mainStructure.length) {
			if (tokensToRead()) {
				if (mainIndex == 0) { // verifica se o retorno da main eh bool
					if (!tokensList.get(index).lexeme.equals("bool")) {
						isCorrect = false;
					}
					mainIndex++;
					index++;
				} else if (mainIndex == 1) { // verifica se o nome da main esta correta
					if (!tokensList.get(index).lexeme.equals("main")) {
						isCorrect = false;
					}
					mainIndex++;
					index++;
				} else if (mainIndex == 5) { // verifica se os comandos estao corretos
					if (tokensList.get(index).lexeme.equals("<")) { // se for um um "<" eh porque nao ha nenhum comando dentro da main, apenas o retorno
						mainIndex++;
					} else {
						while (tokensToRead() && recognizeCommand()) { // enquanto houver comandos validos dentro da main
							index++;							
						}
						if (!tokensList.get(index).lexeme.equals("<")) { // nao ha mais comandos dentro da main, achou o "<" do inicio do retorno
							index++;
						}
						mainIndex++;
					}
				} else if (mainIndex == 6) {
					if (!recognizeMethodReturn()) {
						isCorrect = false;
					}
					mainIndex++;
					index++;
				} else { // verifica se os demais tokens estao corretos
					if (!tokensList.get(index).lexeme.equals(mainStructure[mainIndex])) {
						isCorrect = false;
					}
					if (mainIndex < 7) {
						index++;
					}
					mainIndex++;
				}
			}
			//if (!isCorrect) {
				//forIndex++;
			//}		
		}
		return isCorrect;
	}
	
	public boolean recognizeCommand() {
		if (tokensList.get(index).lexeme.equals("for")) {
			if (!recognizeTokenFor()) {
				panicModeFor();
			} else {
				System.out.println("For correto na linha " + tokensList.get(index).line);
				return true;
			}
		} else if (tokensList.get(index).lexeme.equals("if")) {
			if (!recognizeTokenIf()) {
				panicModeIf();
			} else {
				System.out.println("If correto na linha " + tokensList.get(index).line);
				return true;
			}
		} else if (tokensList.get(index).lexeme.equals("print")) {
			if (!recognizeTokenPrint()) {
				panicModePrint();
			} else {
				System.out.println("Print correto na linha " + tokensList.get(index).line);
				return true;
			}
		} else if (tokensList.get(index).lexeme.equals("scan")) {
			if (!recognizeTokenScan()) {
				panicModeScan();
			} else {
				System.out.println("Scan correto na linha " + tokensList.get(index).line);
				return true;
			}
		}
		return false;
	}
	
	public boolean recognizeVariableDeclaration() {
		boolean isFirstVariable = true;
		while (tokensToRead() && !tokensList.get(index).lexeme.equals(";")) {
			if (isFirstVariable) { // se for a primeira variavel, nao tem virgula antes
				if (tokensList.get(index).type.equals("ID")) { // verifica se o nome da variavel eh valido
					index++;
					isFirstVariable = false;
				} else {
					return false;
				}
			} else {
				if (tokensList.get(index).lexeme.equals(",")) {
					index++;
				} else {
					return false;
				}
				if (tokensToRead() && tokensList.get(index).type.equals("ID")) { // verifica se o nome da variavel eh valido
					index++;
				} else {
					return false;
				}
			}
		}
		return true;
	}
	
	public void panicModeVariable() {
		errorsList.add("ERRO: Declaracao de variavel mal formada na linha " + tokensList.get(index-1).line);
	}
	
	public boolean recognizeMethod() {
		boolean isCorrect = true;
		int methodIndex = 0;
		while (methodIndex < methodStructure.length) {
			if (tokensToRead()) {
				if (methodIndex == 0) { // verifica se o retorno do metodo esta correto
					if (!(isAtributeType() || tokensList.get(index).type.equals("ID"))) {
						isCorrect = false;
					}
					methodIndex++;
					index++;
				} else if (methodIndex == 1) { // verifica se o nome do metodo eh valido
					if (!tokensList.get(index).type.equals("ID")) {
						isCorrect = false;
					}
					methodIndex++;
					index++;
				} else if (methodIndex == 3) { // verifica se os parametros do metodo estao corretos
					if (!recognizeMethodParameters()) {
						isCorrect = false;
					}
					methodIndex++;
					index++;
				} else if (methodIndex == 6) { // verifica se os comandos estao corretos
					if (tokensList.get(index).lexeme.equals("<")) { // se for um um "<" eh porque nao ha nenhum comando dentro do metodo, apenas o retorno
						methodIndex++;
					} else {
						while (tokensToRead() && recognizeCommand()) { // enquanto houver comandos validos dentro do metodo
							index++;							
						}
						if (!tokensList.get(index).lexeme.equals("<")) { // nao ha mais comandos dentro do metodo, achou o "<" do inicio do retorno
							index++;
						}
						methodIndex++;
					}
				} else if (methodIndex == 7) {
					if (!recognizeMethodReturn()) {
						isCorrect = false;
					}
					methodIndex++;
					index++;
				} else { // verifica se os demais tokens estao corretos
					if (!tokensList.get(index).lexeme.equals(methodStructure[methodIndex])) {
						isCorrect = false;
					}
					if (methodIndex < 8) {
						index++;
					}
					methodIndex++;
				}
			}
			//if (!isCorrect) {
				//forIndex++;
			//}		
		}
		return isCorrect;
	}
	
	public void panicModeMethod() {
		errorsList.add("ERRO: Metodo mal formado na linha " + tokensList.get(index-1).line);
	}
	
	public boolean recognizeMethodParameters() {
		boolean isFirstParameter = true;
		while (tokensToRead() && !tokensList.get(index).lexeme.equals(")")) {
			if (isFirstParameter) { // se for o primeiro parametro, nao tem virgula antes
				if (isAtributeType() || tokensList.get(index).type.equals("ID")) { // verifica se o tipo do parametro esta correto
					index++;
				} else {
					return false;
				}
				if (tokensToRead() && tokensList.get(index).type.equals("ID")) { // verifica se o nome do parametro eh valido
					index++;
					isFirstParameter = false; // se tiver proximo parametro, nao sera mais o primeiro
				} else {
					return false;
				}
			} else {
				if (tokensList.get(index).lexeme.equals(",")) {
					index++;
				} else {
					return false;
				}
				if (tokensToRead() && (isAtributeType() || tokensList.get(index).type.equals("ID"))) { // verifica se o tipo do parametro esta correto
					index++;
				} else {
					return false;
				}
				if (tokensToRead() && (tokensList.get(index).type.equals("ID"))) { // verifica se o nome do parametro eh valido
					index++;
				} else {
					return false;
				}
			}
		}
		index--; // achou o ")", entao a leitura continua a partir dele
		return true;
	}
	
	public boolean recognizeMethodReturn() {
		boolean isCorrect = true;
		int methodReturnIndex = 0;
		while (methodReturnIndex < methodReturnStructure.length) {
			if (tokensToRead()) {
				if (methodReturnIndex == 2) {
					if (!tokensList.get(index).type.equals("ID")) {
						isCorrect = false;
					}
					methodReturnIndex++;
					index++;
				} else {
					if (!tokensList.get(index).lexeme.equals(methodReturnStructure[methodReturnIndex])) {
						isCorrect = false;
					}
					if (methodReturnIndex < 4) {
						index++;
					}
					methodReturnIndex++;
				}
			}
		}
		return isCorrect;
	}
	
	public boolean recognizeTokenFor() {
		boolean isCorrect = true;
		int forIndex = 0;
		while (forIndex < forStructure.length) {
			if (tokensToRead()) {
				if (forIndex == 2) { // verifica se a inicializacao do for esta correta
					if (!recognizeInitialization()) {
						isCorrect = false;
					}
					forIndex++;
					index++;
				} else if (forIndex == 4) { // verifica se a condicao do for esta correta
					if (!recognizeRelationalOperation()) {
						isCorrect = false;
					}
					forIndex++;
					index++;
				} else if (forIndex == 6) { // verifica se o incremento do for esta correto
					if (!recognizeIncrement()) {
						isCorrect = false;
					}
					forIndex++;
					index++;
				} else if (forIndex == 9) { // verifica se os comandos estao corretos
					if (tokensList.get(index).lexeme.equals("}")) { // se for um um "}" eh porque nao ha nenhum comando dentro do for
						forIndex++;
					} else {
						while (tokensToRead() && recognizeCommand()) { // enquanto houver comandos validos dentro do for
							index++;							
						}
						if (tokensToRead() && (!tokensList.get(index).lexeme.equals("}"))) { // nao ha mais comandos dentro do for, achou o "}"
							index++;
						}
						forIndex++;
					}
				} else { // verifica se os demais tokens estao corretos
					if (!tokensList.get(index).lexeme.equals(forStructure[forIndex])) {
						isCorrect = false;
					}
					if (forIndex < 10) { // se nao for o ultimo token, avanca o indice
						index++;
					}
					forIndex++;
				}
			}
			//if (!isCorrect) {
				//forIndex++;
			//}		
		}
		return isCorrect;	
	}
	
	public void panicModeFor() {
		errorsList.add("ERRO: Estrutura 'for' mal formada na linha " + tokensList.get(index-1).line);
/*		while (tokensToRead() && !forSyncTokensList.contains(tokensList.get(index).lexeme)) { // ps.: verificar tambem se nao eh atribuicao ou operacao
			index++;
		}*/
		//index--;
	}
	
	public boolean recognizeTokenIf() {
		boolean isCorrect = true;
		int ifIndex = 0;
		while (ifIndex < ifStructure.length) {
			if (tokensToRead()) {
				if (ifIndex == 2) { // verifica se a condicao do if esta correta
					if (!recognizeRelationalOperation()) {
						isCorrect = false;
					}
					ifIndex++;
					index++;
				} else if (ifIndex == 5) { // verifica se os comandos estao corretos
					if (tokensList.get(index).lexeme.equals("}")) { // se for um um "}" eh porque nao ha nenhum comando dentro do if
						ifIndex++;
					} else {
						while (tokensToRead() && recognizeCommand()) { // enquanto houver comandos validos dentro do if
							index++;							
						}
						if (tokensToRead() && (!tokensList.get(index).lexeme.equals("}"))) { // nao ha mais comandos dentro do if, achou o "}"
							index++;
						}
						ifIndex++;
					}
				} else { // verifica se os demais tokens estao corretos
					if (!tokensList.get(index).lexeme.equals(ifStructure[ifIndex])) {
						isCorrect = false;
					}
					if (ifIndex < 6) { // se nao for o ultimo token, avanca o indice
						index++;
					} else {
						if (tokensList.get(index+1).lexeme.equals("else")) { // caso seja um if-else
							index++;
							if (!recognizeTokenElse()) {
								isCorrect = false;
							}
						}
					}
					ifIndex++;
				}
			}
			//if (!isCorrect) {
				//forIndex++;
			//}		
		}
		return isCorrect;
	}
	
	public boolean recognizeTokenElse() {
		boolean isCorrect = true;
		int elseIndex = 0;
		while (elseIndex < elseStructure.length) {
			if (tokensToRead()) {
				if (elseIndex == 2) { // verifica se os comandos estao corretos
					if (tokensList.get(index).lexeme.equals("}")) { // se for um um "}" eh porque nao ha nenhum comando dentro do else
						elseIndex++;
					} else {
						while (tokensToRead() && recognizeCommand()) { // enquanto houver comandos validos dentro do else
							index++;							
						}
						if (tokensToRead() && (!tokensList.get(index).lexeme.equals("}"))) { // nao ha mais comandos dentro do else, achou o "}"
							index++;
						}
						elseIndex++;
					}
				} else { // verifica se os demais tokens estao corretos
					if (!tokensList.get(index).lexeme.equals(elseStructure[elseIndex])) {
						isCorrect = false;
					}
					if (elseIndex < 3) { // se nao for o ultimo token, avanca o indice
						index++;
					} 
					elseIndex++;
				}
			}
			//if (!isCorrect) {
				//forIndex++;
			//}		
		}
		return isCorrect;
	}
	
	public void panicModeIf() {
		errorsList.add("ERRO: Estrutura 'if' mal formada na linha " + tokensList.get(index-1).line);
/*		while (tokensToRead() && !ifSyncTokens.contains(tokensList.get(index).lexeme)) {
			index++;
		}*/
	}
	
	public boolean recognizeTokenPrint() {
		boolean isCorrect = true;
		int printIndex = 0;
		while (printIndex < printStructure.length) {
			if (tokensToRead()) {
				if (printIndex == 2) { // verifica se o conteudo dentro do print esta correto
					if (!recognizePrintContent()) {
						isCorrect = false;
					}
					printIndex++;
					index++;
				} else { // verifica se os demais tokens estao corretos
					if (!tokensList.get(index).lexeme.equals(printStructure[printIndex])) {
						isCorrect = false;
					}
					if (printIndex < 4) { // se nao for o ultimo token, avanca o indice
						index++;
					}
					printIndex++;
				}
			}
			//if (!isCorrect) {
				//forIndex++;
			//}		
		}
		return isCorrect;
	}
	
	public boolean recognizePrintContent() {
		boolean isFirstContent = true;
		while (tokensToRead() && !tokensList.get(index).lexeme.equals(")")) {
			if (isFirstContent) { // se for a primeira escrita, nao tem virgula antes
				if (tokensList.get(index).type.equals("STR") || tokensList.get(index).type.equals("ID")) {
					isFirstContent = false; // se tiver proxima escrita, nao sera mais a primeira
					index++;
				} else {
					return false;
				}
			} else {
				if (tokensList.get(index).lexeme.equals(",")) { // verificar se as impressoes estao separadas por virgula
					index++;
				} else {
					return false;
				}
				if (tokensList.get(index).type.equals("STR") || tokensList.get(index).type.equals("ID")) {
					index++;
				} else {
					return false;
				}
			}
		}
		index--; // achou o ")", entao a leitura continua a partir dele
		return true;
	}
	
	public void panicModePrint() {
		errorsList.add("ERRO: Estrutura 'print' mal formada na linha " + tokensList.get(index-1).line);
	}
	
	public boolean recognizeTokenScan() {
		boolean isCorrect = true;
		int scanIndex = 0;
		while (scanIndex < scanStructure.length) {
			if (tokensToRead()) {
				if (scanIndex == 2) { // verifica se o conteudo dentro do scan esta correto
					if (!recognizeScanContent()) {
						isCorrect = false;
					}
					scanIndex++;
					index++;
				} else { // verifica se os demais tokens estao corretos
					if (!tokensList.get(index).lexeme.equals(scanStructure[scanIndex])) {
						isCorrect = false;
					}
					if (scanIndex < 4) { // se nao for o ultimo token, avanca o indice
						index++;
					}
					scanIndex++;
				}
			}
			//if (!isCorrect) {
				//forIndex++;
			//}		
		}
		return isCorrect;
	}
	
	public boolean recognizeScanContent() {
		boolean isFirstVariable = true;
		while (tokensToRead() && !tokensList.get(index).lexeme.equals(")")) {
			if (isFirstVariable) { // se for a primeira leitura, nao tem virgula antes
				if (tokensList.get(index).type.equals("ID")) {
					isFirstVariable = false; // se tiver proxima leitura, nao sera mais a primeira
					index++;
				} else {
					return false;
				}
			} else {
				if (tokensList.get(index).lexeme.equals(",")) { // verificar se as leituras estao separadas por virgula
					index++;
				} else {
					return false;
				}
				if (tokensToRead() && (tokensList.get(index).type.equals("ID"))) {
					index++;
				} else {
					return false;
				}
			}
		}
		index--; // achou o ")", entao a leitura continua a partir dele
		return true;
	}
	
	public void panicModeScan() {
		errorsList.add("ERRO: Estrutura 'scan' mal formada na linha " + tokensList.get(index-1).line);
	}
	
	public boolean isAtributeType() {
		return (tokensList.get(index).lexeme.equals("int") || tokensList.get(index).lexeme.equals("float") ||
				tokensList.get(index).lexeme.equals("bool") || tokensList.get(index).lexeme.equals("string"));
	}
	
	public boolean tokensToRead() {
		return index < tokensList.size();
	}
	
	public boolean recognizeInitialization() { 
		if (tokensList.get(index).type.equals("ID")) {
			index++;
			if (tokensToRead() && tokensList.get(index).lexeme.equals("=")) {
				index++;
				if (tokensToRead() && tokensList.get(index).type.equals("ID") || tokensList.get(index).type.equals("NUM")) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean recognizeIncrement() {
		if (tokensList.get(index).type.equals("ID")) {
			index++;
			if (tokensToRead() && tokensList.get(index).lexeme.equals("=")) {
				index++;
				if (tokensToRead() && recognizeArithmeticOperation()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean recognizeRelationalOperation() {
		if (tokensList.get(index).type.equals("ID")) {
			index++;
			if (tokensToRead() && tokensList.get(index).type.equals("RELOP")) {
				index++;
				if (tokensToRead() && tokensList.get(index).type.equals("ID") || tokensList.get(index).type.equals("NUM") || tokensList.get(index).type.equals("STR") || 
						tokensList.get(index).lexeme.equals("true") || tokensList.get(index).lexeme.equals("false")) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean recognizeArithmeticOperation() {
		if (tokensList.get(index).type.equals("ID") || tokensList.get(index).type.equals("NUM")) {
			index++;
			if (tokensToRead() && tokensList.get(index).type.equals("ARIOP")) {
				index++;
				if (tokensToRead() && tokensList.get(index).type.equals("ID") || tokensList.get(index).type.equals("NUM")) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean recognizeVector() {
		if (tokensList.get(index).lexeme.equals("[")) {
			index++;
			if (tokensToRead() && tokensList.get(index).type.equals("NUM")) {
				index++;
				if (tokensToRead() && tokensList.get(index).lexeme.equals("]")) {
					if (tokensToRead() && tokensList.get(index+1).lexeme.equals("[")) {
						index = index + 2;
						if (tokensToRead() && tokensList.get(index).type.equals("NUM")) {
							index++;
							if (tokensToRead() && tokensList.get(index).lexeme.equals("]")) {
								return true; // eh uma matriz (2 dimensoes)
							}
						}
					} else {
						return true; // eh um vetor (1 dimensao)
					}
				}
			}
		}
		return false;
	}

}