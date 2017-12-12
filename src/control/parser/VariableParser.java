package control.parser;

/**
 * 
 * Classe responsavel por reconhecer declaracao e inicializacao de variaveis
 *
 */
public class VariableParser {
	
	private FileParser parser;
	
	public VariableParser(FileParser parser) {
		this.parser = parser;
	}
	
	// reconhece a estrutura sintatica de declaracao de uma variavel global, atributo ou local
	public boolean recognizeVariableDeclaration() {
		boolean isFirstVariable = true;
		while (parser.tokensToRead() && !parser.getTokensList().get(parser.index).lexeme.equals(";")) {
			if (isFirstVariable) { // se for a primeira variavel, nao tem virgula antes
				if (parser.getTokensList().get(parser.index).type.equals("ID")) { // verifica se o nome da variavel eh valido
					parser.index = parser.index + 1;
					isFirstVariable = false;
				} else {
					return false;
				}
			} else {
				if (parser.getTokensList().get(parser.index).lexeme.equals(",")) {
					parser.index = parser.index + 1;
				} else {
					return false;
				}
				if (parser.tokensToRead() && parser.getTokensList().get(parser.index).type.equals("ID")) { // verifica se o nome da variavel eh valido
					parser.index = parser.index + 1;
				} else {
					return false;
				}
			}
		}
		return true;
	}
	
	// reconhece a a estrutura sintatica de inicializacao de uma variavel global, atributo ou local
	public boolean recognizeInitialization() { 
		if (parser.getTokensList().get(parser.index).type.equals("ID")) {
			parser.index = parser.index + 1;
			if (parser.tokensToRead() && parser.getTokensList().get(parser.index).lexeme.equals("=")) {
				parser.index = parser.index + 1;
				if (parser.tokensToRead() && parser.getTokensList().get(parser.index).type.equals("ID") || parser.getTokensList().get(parser.index).type.equals("NUM") || parser.getTokensList().get(parser.index).type.equals("STR") || 
						parser.getTokensList().get(parser.index).lexeme.equals("true") || parser.getTokensList().get(parser.index).lexeme.equals("false")) {
					return true;
				}
			}
		}
		return false;
	}
	
	// reconhece a estrutura sintatica de um vetor
	public boolean recognizeVector() {
		if (parser.getTokensList().get(parser.index).lexeme.equals("[")) {
			parser.index = parser.index + 1;
			if (parser.tokensToRead() && parser.getTokensList().get(parser.index).type.equals("NUM")) {
				parser.index = parser.index + 1;
				if (parser.tokensToRead() && parser.getTokensList().get(parser.index).lexeme.equals("]")) {
					if (parser.tokensToRead() && parser.getTokensList().get(parser.index+1).lexeme.equals("[")) {
						parser.index = parser.index + 2;
						if (parser.tokensToRead() && parser.getTokensList().get(parser.index).type.equals("NUM")) {
							parser.index = parser.index + 1;
							if (parser.tokensToRead() && parser.getTokensList().get(parser.index).lexeme.equals("]")) {
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
