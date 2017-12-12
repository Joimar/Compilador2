package control.parser;

import java.util.ArrayList;

import model.Token;

/**
 * 
 * Classe responsavel por reconhecer codigos de classes e variaveis globais
 *
 */
public class FileParser {
	
	int index; // indice atual da lista de tokens
	private ArrayList<Token> tokensList; // lista de tokens recebida do lexico
	private ArrayList<String> errorsList; // lista de erros sintaticos
	private String[] classStructure = {"class", "<name>", "{", "<content>", "}"}; // estrutura sintatica de uma classe
	
	public FileParser(ArrayList<Token> tokensList) {
		this.tokensList = tokensList;
		errorsList = new ArrayList<String>();
	}
	
	// metodo inicial que chama o reconhecimento de classes e variaveis globais
	public void fileParsing() {
		for (index = 0; index < tokensList.size(); index++) {
			if (tokensList.get(index).lexeme.equals("class")) { // declaracao de classe
				if (!recognizeClass()) {
					panicModeClass();
				} else {
					System.out.println("Classe correta na linha " + tokensList.get(index).line);
				}
			} else { // declaracao de variavel global
				if (tokensList.get(index).lexeme.equals("final")) { // pode ser constante
					index++;
					if (tokensToRead() &&  (isAttributeType() || tokensList.get(index).type.equals("ID"))) {
						recognizeGlobalVariable();
					}
				} else if (isAttributeType() || tokensList.get(index).type.equals("ID")) {
					recognizeGlobalVariable();
				} else {
					// Comando nao reconhecido
				}
			}
		}
	}
	
	// reconhece a estrutura sintatica de uma classe
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
						while (tokensToRead() && new ClassParser(this).recognizeClassContent()) { // enquanto houver comandos validos dentro da classe 
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
	
	// reconhece a estrutura sintatica de uma variavel global
	public boolean recognizeGlobalVariable() {
		if (isAttributeType() || tokensList.get(index).type.equals("ID")) {
			index++;
			if (tokensToRead() && tokensList.get(index).lexeme.equals("=")) { // inicializacao de variavel global
				index--; // para comecar a varredura de inicializacao de variavel pelo id
				if (!new VariableParser(this).recognizeInitialization()) { // verifica se a atribuicao esta correta
					panicModeGlobalVariableInitialization();
				} else {
					index++;
					if (tokensToRead() && tokensList.get(index).lexeme.equals(";")) { // inicializacao
						System.out.println("Inicializacao de variavel global correta na linha " + tokensList.get(index).line);
						return true;
					} else if (tokensToRead() && tokensList.get(index).type.equals("ARIOP")) { // inicializacao com operacao aritmetica
						index++;
						if (tokensToRead() && new OperationParser(this).recognizeArithmeticOperation()) {
							System.out.println("Inicializacao de variavel global com operacao aritmetica correta na linha " + tokensList.get(index).line);
							return true;
						} else {
							panicModeGlobalVariableInitialization();
						}
					} else {
						panicModeGlobalVariableInitialization();
					}
				}
			} else { // declaracao de variavel global
				if (tokensToRead() && new VariableParser(this).recognizeVector()) { // verifica se eh vetor ou matriz
					index++;
				}
				if (tokensToRead() && tokensList.get(index).type.equals("ID")) {
					index++;
					if (tokensToRead() && (tokensList.get(index).lexeme.equals(";") || tokensList.get(index).lexeme.equals(","))) {
						index--; // para comecar a varredura da estrutura de declaracao de variavel a partir do id
						if (!new VariableParser(this).recognizeVariableDeclaration()) {
							panicModeGlobalVariableDeclaration();
						} else {
							System.out.println("Declaracao de variavel global correta na linha " + tokensList.get(index).line);
							return true;
						}
					} else {
						panicModeGlobalVariableDeclaration();
					}
				} 
				//else {
					//panicModeGlobalVariableDeclaration();
				//}
			}
		}
		return false;
	}
	
	public void panicModeClass() {
		addError("ERRO: Classe mal formada na linha " + tokensList.get(index-1).line);
	}
	
	public void panicModeGlobalVariableInitialization() {
		addError("ERRO: Inicializacao de variavel global mal formada na linha " + tokensList.get(index-1).line);
	}
	
	public void panicModeGlobalVariableDeclaration() {
		addError("ERRO: Declaracao de variavel global mal formada na linha " + tokensList.get(index-1).line);
	}
	
	public ArrayList<Token> getTokensList() {
		return tokensList;
	}
	
	public ArrayList<String> getErrorsList() {
		return errorsList;
	}
	
	public void addError(String message) {
		errorsList.add(message);
	}
	
	public boolean isAttributeType() {
		return (tokensList.get(index).lexeme.equals("int") || tokensList.get(index).lexeme.equals("float") ||
				tokensList.get(index).lexeme.equals("bool") || tokensList.get(index).lexeme.equals("string"));
	}
	
	public boolean tokensToRead() {
		return index < tokensList.size();
	}

}
