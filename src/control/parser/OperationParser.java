package control.parser;

/**
 * 
 * Classe responsavel por reconhecer operacoes aritmeticas, logicas e relacionais
 *
 */
public class OperationParser {
	
	private FileParser parser;
	
	public OperationParser(FileParser parser) {
		this.parser = parser;
	}
	
	// reconhece a estrutura sintatica de operacao aritmetica
	public boolean recognizeArithmeticOperation() {
		if (parser.getTokensList().get(parser.index).type.equals("ID") || parser.getTokensList().get(parser.index).type.equals("NUM")) {
			parser.index = parser.index + 1;
			while (parser.tokensToRead() && parser.getTokensList().get(parser.index).type.equals("ARIOP")) {
				parser.index = parser.index + 1;
				if (parser.tokensToRead() && parser.getTokensList().get(parser.index).type.equals("ID") || parser.getTokensList().get(parser.index).type.equals("NUM")) {
					parser.index = parser.index + 1;
				}
			}
			if (parser.tokensToRead() && parser.getTokensList().get(parser.index).type.equals("DEL")) {
				return true;
			}
		}
		return false;
	}
	
	// reconhece a estrutura sintatica de operacao logica
	public boolean recognizeLogicalOperation() {
		if (parser.tokensToRead() && (parser.getTokensList().get(parser.index).lexeme.equals("&&") || parser.getTokensList().get(parser.index).lexeme.equals("||"))) {
			parser.index = parser.index + 1;
			if (parser.tokensToRead()) {
				return recognizeRelationalOperation();
			}
		}
		return false;
	}
	
	// reconhece a estrutura sintatica de operacao relacional
	public boolean recognizeRelationalOperation() {
		if (parser.tokensToRead() && parser.getTokensList().get(parser.index).type.equals("ID")) {
			parser.index = parser.index + 1;
			if (parser.tokensToRead() && parser.getTokensList().get(parser.index).type.equals("RELOP")) {
				parser.index = parser.index + 1;
				if (parser.tokensToRead() && parser.getTokensList().get(parser.index).type.equals("ID") || parser.getTokensList().get(parser.index).type.equals("NUM") || parser.getTokensList().get(parser.index).type.equals("STR") || 
						parser.getTokensList().get(parser.index).lexeme.equals("true") || parser.getTokensList().get(parser.index).lexeme.equals("false")) {
					if (parser.getTokensList().get(parser.index + 1).lexeme.equals(";") || parser.getTokensList().get(parser.index + 1).lexeme.equals(")")) {
						return true;
					} else {
						parser.index = parser.index + 1;
						return recognizeLogicalOperation();
					}
				}
			}
		}
		return false;
	}

	public void panicModeArithmeticOperation() {
		parser.addError("ERRO: Operacao aritmetica mal formada na linha " + parser.getTokensList().get(parser.index-1).line);
	}
	
}
