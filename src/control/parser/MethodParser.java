package control.parser;

/**
 * 
 * Classe responsavel por reconhecer metodos, incluindo o metodo main
 *
 */
public class MethodParser {
	
	private FileParser parser;
	private String[] mainStructure = {"bool", "main", "(", ")", "{", "<commands>", "<return>", "}"};
	private String[] methodStructure = {"<return_type>", "<name>", "(", "<parameters>", ")", "{", "<commands>", "<return>", "}"};
	private String[] methodReturnStructure = {"<", ":", "<return>", ":", ">"};
	
	public MethodParser(FileParser parser) {
		this.parser = parser;
	}
	
	// reconhece a estrutura sintatica de um metodo
	public boolean recognizeMethod() {
		boolean isCorrect = true;
		int methodIndex = 0;
		while (methodIndex < methodStructure.length) {
			if (parser.tokensToRead()) {
				if (methodIndex == 0) { // verifica se o retorno do metodo esta correto
					if (!(parser.isAttributeType() || parser.getTokensList().get(parser.index).type.equals("ID"))) {
						isCorrect = false;
					}
					methodIndex++;
					parser.index = parser.index + 1;
				} else if (methodIndex == 1) { // verifica se o nome do metodo eh valido
					if (!parser.getTokensList().get(parser.index).type.equals("ID")) {
						isCorrect = false;
					}
					methodIndex++;
					parser.index = parser.index + 1;
				} else if (methodIndex == 3) { // verifica se os parametros do metodo estao corretos
					if (!recognizeMethodParameters()) {
						isCorrect = false;
					}
					methodIndex++;
					parser.index = parser.index + 1;
				} else if (methodIndex == 6) { // verifica se os comandos estao corretos
					if (parser.getTokensList().get(parser.index).lexeme.equals("<")) { // se for um um "<" eh porque nao ha nenhum comando dentro do metodo, apenas o retorno
						methodIndex++;
					} else {
						while (parser.tokensToRead() && new CommandParser(parser).recognizeCommand()) { // enquanto houver comandos validos dentro do metodo
							parser.index = parser.index + 1;							
						}
						if (!parser.getTokensList().get(parser.index).lexeme.equals("<")) { // nao ha mais comandos dentro do metodo, achou o "<" do inicio do retorno
							parser.index = parser.index + 1;
						}
						methodIndex++;
					}
				} else if (methodIndex == 7) {
					if (!recognizeMethodReturn()) {
						isCorrect = false;
					}
					methodIndex++;
					parser.index = parser.index + 1;
				} else { // verifica se os demais tokens estao corretos
					if (!parser.getTokensList().get(parser.index).lexeme.equals(methodStructure[methodIndex])) {
						isCorrect = false;
					}
					if (methodIndex < 8) {
						parser.index = parser.index + 1;
					}
					methodIndex++;
				}
			}
			//if (!isCorrect) {
				//forparser.index = parser.index + 1;
			//}		
		}
		return isCorrect;
	}
	
	// reconhece a estrutura sintatica dos parametros de um metodo
	public boolean recognizeMethodParameters() {
		boolean isFirstParameter = true;
		while (parser.tokensToRead() && !parser.getTokensList().get(parser.index).lexeme.equals(")")) {
			if (isFirstParameter) { // se for o primeiro parametro, nao tem virgula antes
				if (parser.isAttributeType() || parser.getTokensList().get(parser.index).type.equals("ID")) { // verifica se o tipo do parametro esta correto
					parser.index = parser.index + 1;
				} else {
					return false;
				}
				if (parser.tokensToRead() && parser.getTokensList().get(parser.index).type.equals("ID")) { // verifica se o nome do parametro eh valido
					parser.index = parser.index + 1;
					isFirstParameter = false; // se tiver proximo parametro, nao sera mais o primeiro
				} else {
					return false;
				}
			} else {
				if (parser.getTokensList().get(parser.index).lexeme.equals(",")) {
					parser.index = parser.index + 1;
				} else {
					return false;
				}
				if (parser.tokensToRead() && (parser.isAttributeType() || parser.getTokensList().get(parser.index).type.equals("ID"))) { // verifica se o tipo do parametro esta correto
					parser.index = parser.index + 1;
				} else {
					return false;
				}
				if (parser.tokensToRead() && (parser.getTokensList().get(parser.index).type.equals("ID"))) { // verifica se o nome do parametro eh valido
					parser.index = parser.index + 1;
				} else {
					return false;
				}
			}
		}
		parser.index = parser.index - 1; // achou o ")", entao a leitura continua a partir dele
		return true;
	}
	
	// reconhece a estrutura sintatica do retorno de um metodo
	public boolean recognizeMethodReturn() {
		boolean isCorrect = true;
		int methodReturnIndex = 0;
		while (methodReturnIndex < methodReturnStructure.length) {
			if (parser.tokensToRead()) {
				if (methodReturnIndex == 2) {
					if (!parser.getTokensList().get(parser.index).type.equals("ID")) {
						isCorrect = false;
					}
					methodReturnIndex++;
					parser.index = parser.index + 1;
				} else {
					if (!parser.getTokensList().get(parser.index).lexeme.equals(methodReturnStructure[methodReturnIndex])) {
						isCorrect = false;
					}
					if (methodReturnIndex < 4) {
						parser.index = parser.index + 1;
					}
					methodReturnIndex++;
				}
			}
		}
		return isCorrect;
	}
	
	// reconhece a estrutura sintatica do metodo main
	public boolean recognizeMain() {
		boolean isCorrect = true;
		int mainIndex = 0;
		while (mainIndex < mainStructure.length) {
			if (parser.tokensToRead()) {
				if (mainIndex == 0) { // verifica se o retorno da main eh bool
					if (!parser.getTokensList().get(parser.index).lexeme.equals("bool")) {
						isCorrect = false;
					}
					mainIndex++;
					parser.index = parser.index + 1;
				} else if (mainIndex == 1) { // verifica se o nome da main esta correta
					if (!parser.getTokensList().get(parser.index).lexeme.equals("main")) {
						isCorrect = false;
					}
					mainIndex++;
					parser.index = parser.index + 1;
				} else if (mainIndex == 5) { // verifica se os comandos estao corretos
					if (parser.getTokensList().get(parser.index).lexeme.equals("<")) { // se for um um "<" eh porque nao ha nenhum comando dentro da main, apenas o retorno
						mainIndex++;
					} else {
						while (parser.tokensToRead() && new CommandParser(parser).recognizeCommand()) { // enquanto houver comandos validos dentro da main
							parser.index = parser.index + 1;							
						}
						if (!parser.getTokensList().get(parser.index).lexeme.equals("<")) { // nao ha mais comandos dentro da main, achou o "<" do inicio do retorno
							parser.index = parser.index + 1;
						}
						mainIndex++;
					}
				} else if (mainIndex == 6) {
					if (!recognizeMethodReturn()) {
						isCorrect = false;
					}
					mainIndex++;
					parser.index = parser.index + 1;
				} else { // verifica se os demais tokens estao corretos
					if (!parser.getTokensList().get(parser.index).lexeme.equals(mainStructure[mainIndex])) {
						isCorrect = false;
					}
					if (mainIndex < 7) {
						parser.index = parser.index + 1;
					}
					mainIndex++;
				}
			}
			//if (!isCorrect) {
				//forparser.index = parser.index + 1;
			//}		
		}
		return isCorrect;
	}
	
}
