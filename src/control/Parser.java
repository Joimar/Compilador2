package control;

import java.util.ArrayList;

import model.Token;

public class Parser {
	
	String[] forStructure = {"for", "(", ";", ";", ")", "{"};
	String[] printStructure = {"print", "(", ")", ";"};
	String[] methodReturnStructure = {"<", ":", ":", ">", "}"};
	ArrayList<Token> tokensList; // lista de tokens recebida do lexico
	int index; // indice atual da lista de tokens
	int bracesToClose; // numero de "}" que precisam ser encontrados
	int openMethods; // numero de metodos que nao foram fechados; se for maior que um, utilizar modo panico
	
	public Parser(ArrayList<Token> tokensList) {
		this.tokensList = tokensList;
		bracesToClose = 0;
		openMethods = 0;
	}
	
	public boolean readTokens() {
		System.out.println("---------- INICIO DA ANALISE SINTATICA ----------");
		for (index = 0; index < tokensList.size(); index++) {
			Token token = tokensList.get(index);
			if (openMethods <= 1) {		
				if (token.lexeme.equals("for")) { // verifica se eh uma producao for
					if (openMethods == 1) { // verifica se esta dentro de um metodo
						if (!recognizeFor()) {
							System.out.println("ERRO: Estrutura 'for' mal formada na linha " + token.line);
						}
					} else {
						System.out.println("ERRO: Posicione a estrutura 'for' da linha " + token.line + " dentro de um metodo");
						return false;
					}
				}
				else if (token.lexeme.equals("print")) { // verifica se eh uma producao print
					if (openMethods == 1) {
						if (!recognizePrint()) {
							System.out.println("ERRO: Estrutura 'print' mal formada na linha " + token.line);
						}
					} else {
						System.out.println("ERRO: Posicione a estrutura 'print' da linha " + token.line + " dentro de um metodo");
						return false;
					}
				}
				else if (isAtributeType() && tokensList.get(index+1).type.equals("ID")) { // pode ser inicio de declaracao de variavel ou declaracao de metodo
					if (tokensList.get(index+2).lexeme.equals("(")) { // eh uma declaracao de metodo
						if (openMethods == 0) { // nao ha metodos a serem fechados
							if (!recognizeMethod()) {
								System.out.println("ERRO: Declaracao de metodo mal formada na linha " + token.line);
							}
						} else {
							openMethods++;
						}
					}
				}
				else if (token.lexeme.equals("<") && tokensList.get(index+1).lexeme.equals(":") && openMethods == 1) { // verifica se eh o retorno do metodo
					if (!recognizeMethodReturn()) { // somente aqui os metodos sao fechados corretamente
						System.out.println("ERRO: Fechamento de metodo ou retorno mal formado na linha " + token.line);
					}
				}
				else if (token.lexeme.equals("}")) { // verifica se eh um "}" e se existe alguma estrutura para fechar
					if (bracesToClose > 0) {
						bracesToClose--;						
					} else if (openMethods == 1) { // se a estrutura que esta sendo fechada eh um metodo (sem retorno antes), esta errado
						System.out.println("ERRO: Metodo fechado sem retorno na linha " + token.line);
						openMethods--;
					}
				}
			} else {
				System.out.println("ERRO: Nao e permitido declarar um metodo dentro de outro, verifique a linha " + token.line);
				return false;
			}
		}
		if (bracesToClose > 0) { // verifica se algum bloco esta aberto
			System.out.println("ERRO: " + bracesToClose + " simbolos '}' faltando!"); // alerta de que eh preciso inserir simbolo(s) de "}"
			return false;
		} else {
			return true;
		}
	}
	
	public boolean isAtributeType() {
		return (tokensList.get(index).lexeme.equals("int") || tokensList.get(index).lexeme.equals("float") ||
				tokensList.get(index).lexeme.equals("bool") || tokensList.get(index).lexeme.equals("string"));
	}
	
	public boolean tokensToRead() {
		return index < tokensList.size();
	}
	
	public boolean recognizeMethod() {
		boolean isFirstParameter = true;
		if (isAtributeType()) { // verifica se eh um retorno valido
			index++;
			if (tokensToRead() && tokensList.get(index).type.equals("ID")) { // verifica se eh um nome de metodo
				index++;
				if (tokensToRead() && tokensList.get(index).lexeme.equals("(")) { // verifica se eh "("
					index++;
					while (tokensToRead() && !tokensList.get(index).lexeme.equals(")")) { // verifica se eh ")"
						if (isFirstParameter && isAtributeType() && tokensList.get(index+1).type.equals("ID")) { // verifica se eh o primeiro parametro (nao tem virgula)
							isFirstParameter = false;
							index = index + 2;
						} else if (!isFirstParameter && tokensList.get(index).lexeme.equals(",")) { // verifica se ha "," antes do parametro, caso nao seja o primeiro
							index++;
							if (isAtributeType() && tokensList.get(index+1).type.equals("ID")) {
								index = index + 2;
							} else {
								return false;
							}
						} else {
							return false;
						}
					}
					index++;
					if (tokensToRead() && tokensList.get(index).lexeme.equals("{")) { // verifica se eh "{"
						openMethods++;
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean recognizeMethodReturn() {
		if (tokensList.get(index).lexeme.equals(methodReturnStructure[0])) { // verifica se eh "<"
			index++;
			if (tokensToRead() && tokensList.get(index).lexeme.equals(methodReturnStructure[1])) { // verifica se eh ":"
				index++;
				if (tokensToRead() && tokensList.get(index).type.equals("ID") || tokensList.get(index).type.equals("NUM") || tokensList.get(index).type.equals("STR") || 
						tokensList.get(index).lexeme.equals("true") || tokensList.get(index).lexeme.equals("false")) { // verifica se eh retorno valido
					index++;
					if (tokensToRead() && tokensList.get(index).lexeme.equals(methodReturnStructure[2])) { // verifica se eh ":"
						index++;
						if (tokensToRead() && tokensList.get(index).lexeme.equals(methodReturnStructure[3])) { // verifica se eh ">"
							index++;
							if (tokensToRead() && tokensList.get(index).lexeme.equals(methodReturnStructure[4])) { // verifica se eh "}"
								System.out.println("Estrutura do metodo sintaticamente correta na linha " + tokensList.get(index).line);
								openMethods--;
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	public boolean recognizePrint() {
		if (tokensList.get(index).lexeme.equals(printStructure[0])) { // verifica se eh "print"
			index++;
			if (tokensToRead() && tokensList.get(index).lexeme.equals(printStructure[1])) { // verifica se eh "("
				index++;
				if(tokensToRead() && tokensList.get(index).type.equals("STR")) { // verifica se eh uma string
					index++;
					if (tokensToRead() && tokensList.get(index).lexeme.equals(printStructure[2])) { // verifica se eh ")"
						index++;
						if (tokensToRead() && tokensList.get(index).lexeme.equals(printStructure[3])) { // verifica se eh ";"
							System.out.println("Estrutura do 'print' sintaticamente correta na linha " + tokensList.get(index).line);
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public boolean recognizeFor() {
		if (tokensList.get(index).lexeme.equals(forStructure[0])) { // verifica se eh "for"
			index++;
			if (tokensToRead() && tokensList.get(index).lexeme.equals(forStructure[1])) { // verifica se eh "("
				index++;
				if (tokensToRead() && recognizeInitialization()) { // verifica se eh uma inicializacao
					index++;
					if (tokensToRead() && tokensList.get(index).lexeme.equals(forStructure[2])) { // verifica se eh um ";"
						index++;
						if (tokensToRead() && recognizeRelationalOperation()) { // verifica se eh uma condicao
							index++;
							if (tokensToRead() && tokensList.get(index).lexeme.equals(forStructure[3])) { // verifica se eh um ";"
								index++;
								if (tokensToRead() && recognizeIncrement()) { // verifica se eh um incremento
									index++;
									if (tokensToRead() && tokensList.get(index).lexeme.equals(forStructure[4])) { // verifica se eh um ")"
										index++;
										if (tokensToRead() && tokensList.get(index).lexeme.equals(forStructure[5])) { // verifica se eh um "{"
											bracesToClose++;
											System.out.println("Estrutura do 'for' sintaticamente correta na linha " + tokensList.get(index).line);
											return true;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
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
	
	public boolean recognizeIf() {
		if (tokensList.get(index).lexeme.equals("if")) {
			index++;
			if (tokensToRead() && tokensList.get(index).lexeme.equals("(")) {
				index++;
				if (tokensToRead() && recognizeCondition()) {
					index++;
					if (tokensToRead() && tokensList.get(index).lexeme.equals(")")) {
						index++;
						if (tokensToRead() && tokensList.get(index).lexeme.equals("{")) {
							index++;
							if (tokensToRead() && recognizeCommand()) {
								index++;
								if (tokensToRead() && tokensList.get(index).lexeme.equals("}")) {
									index++;
									if (tokensToRead() && tokensList.get(index).lexeme.equals("else")) {
										if (recognizeElse()) {
											return true;
										}
									} else {
										return true;
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	public boolean recognizeElse() {
		if (tokensList.get(index).lexeme.equals("else")) {
			index++;
			if (tokensToRead() && tokensList.get(index).lexeme.equals("{")) {
				index++;
				if (tokensToRead() && recognizeCommand()) {
					index++;
					if (tokensToRead() && tokensList.get(index).lexeme.equals("}")) {
						index++;
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean recognizeCondition() {
		return true;
	}
	
	public boolean recognizeCommand() {
		return true;
	}
}
