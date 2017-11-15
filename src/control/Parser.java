package control;

import java.util.ArrayList;

import model.Token;

public class Parser {
	
	String[] forStructure = {"for", "(", ";", ";", ")", "{"};
	String[] printStructure = {"print", "(", ")", ";"};
	ArrayList<Token> tokensList; // lista de tokens recebida do lexico
	int index; // indice atual da lista de tokens
	int bracesToClose; // numero de "}" que precisam ser encontrados
	
	public Parser(ArrayList<Token> tokensList) {
		this.tokensList = tokensList;
		bracesToClose = 0;
	}
	
	public void readTokens() {
		System.out.println("---------- INICIO DA ANALISE SINTATICA ----------");
		for (index = 0; index < tokensList.size(); index++) {
			Token token = tokensList.get(index);
			if (token.lexeme.equals("for")) { // verifica se eh uma producao for
				recognizeFor();
			}
			else if (token.lexeme.equals("print")) { // verifica se eh uma producao print
				recognizePrint();
			}
			else if (token.lexeme.equals("}") && bracesToClose > 0) { // verifica se eh um "}"
				System.out.println("Fechou uma chave } corretamente");
				bracesToClose--;
			}
		}
		if (bracesToClose > 0) {
			System.out.println("Por favor, insira " + bracesToClose + " simbolos de }"); // alerta de que eh preciso inserir simbolo(s) de }
		} else {
			System.out.println("---------- FIM DA ANALISE SINTATICA ----------");
			System.out.println("Nenhum erro sintatico encontrado!");
		}
	}
	
	public boolean recognizePrint() {
		System.out.println("Inicio da analise sintatica do print");
		if (tokensList.get(index).lexeme.equals(printStructure[0])) { // verifica se eh "print"
			index++;
			if (tokensList.get(index).lexeme.equals(printStructure[1])) { // verifica se eh "("
				index++;
				if(tokensList.get(index).type.equals("STR")) { // verifica se eh uma string
					index++;
					if (tokensList.get(index).lexeme.equals(printStructure[2])) { // verifica se eh ")"
						index++;
						if (tokensList.get(index).lexeme.equals(printStructure[3])) { // verifica se eh ";"
							System.out.println("Estrutura do print sintaticamente correta");
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public boolean recognizeFor() {
		System.out.println("Inicio da analise sintatica do for");
		if (tokensList.get(index).lexeme.equals(forStructure[0])) { // verifica se eh "for"
			index++;
			if (tokensList.get(index).lexeme.equals(forStructure[1])) { // verifica se eh "("
				index++;
				if (recognizeInitialization()) { // verifica se eh uma inicializacao
					index++;
					if (tokensList.get(index).lexeme.equals(forStructure[2])) { // verifica se eh um ";"
						index++;
						if (recognizeRelationalOperation()) { // verifica se eh uma condicao
							index++;
							if (tokensList.get(index).lexeme.equals(forStructure[3])) { // verifica se eh um ";"
								index++;
								if (recognizeIncrement()) { // verifica se eh um incremento
									index++;
									if (tokensList.get(index).lexeme.equals(forStructure[4])) { // verifica se eh um ")"
										index++;
										if (tokensList.get(index).lexeme.equals(forStructure[5])) { // verifica se eh um "{"
											bracesToClose++;
											System.out.println("Estrutura do for sintaticamente correta");
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
			if (tokensList.get(index).lexeme.equals("=")) {
				index++;
				if (tokensList.get(index).type.equals("ID") || tokensList.get(index).type.equals("NUM")) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean recognizeIncrement() {
		if (tokensList.get(index).type.equals("ID")) {
			index++;
			if (tokensList.get(index).lexeme.equals("=")) {
				index++;
				if (recognizeArithmeticOperation()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean recognizeRelationalOperation() {
		if (tokensList.get(index).type.equals("ID")) {
			index++;
			if (tokensList.get(index).type.equals("RELOP")) {
				index++;
				if (tokensList.get(index).type.equals("ID") || tokensList.get(index).type.equals("NUM") || tokensList.get(index).type.equals("STR") || 
						tokensList.get(index).lexeme.equals("true") || tokensList.get(index).lexeme.equals("true")) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean recognizeArithmeticOperation() {
		if (tokensList.get(index).type.equals("ID") || tokensList.get(index).type.equals("NUM")) {
			index++;
			if (tokensList.get(index).type.equals("ARIOP")) {
				index++;
				if (tokensList.get(index).type.equals("ID") || tokensList.get(index).type.equals("NUM")) {
					return true;
				}
			}
		}
		return false;
	}
	
}