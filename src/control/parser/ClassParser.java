package control.parser;

/**
 * 
 * Classe responsavel por reconhecer o conteudo de uma classe, ou seja, metodos e atributos
 *
 */
public class ClassParser {
	
	private FileParser parser;
	
	public ClassParser(FileParser parser) {
		this.parser = parser;
	}
	
	// reconhece o conteudo de uma classe, isto eh, metodos e atributos
	public boolean recognizeClassContent() {
		boolean isVector = false;
		boolean isFinal = false;
		if (parser.getTokensList().get(parser.index).lexeme.equals("final")) { // atributo pode ser constante
			isFinal = true;
			parser.index = parser.index + 1;
		}
		if (parser.tokensToRead() && (parser.isAttributeType() || parser.getTokensList().get(parser.index).type.equals("ID"))) {
			parser.index = parser.index + 1;
			if (parser.tokensToRead() && parser.getTokensList().get(parser.index).lexeme.equals("=") && isVector == false && isFinal == false) { // inicializacao de atributo
				parser.index = parser.index - 1; // para comecar a varredura de inicializacao de variavel pelo id
				if (!new VariableParser(parser).recognizeInitialization()) { // verifica se a atribuicao esta correta
					panicModeAttributeInitialization();
				} else {
					parser.index = parser.index + 1;
					if (parser.tokensToRead() && parser.getTokensList().get(parser.index).lexeme.equals(";")) { // inicializacao
						System.out.println("Inicializacao de atributo correta na linha " + parser.getTokensList().get(parser.index).line);
						return true;
					} else if (parser.tokensToRead() && parser.getTokensList().get(parser.index).type.equals("ARIOP")) { // inicializacao com operacao aritmetica
						parser.index = parser.index + 1;
						if (parser.tokensToRead() && new OperationParser(parser).recognizeArithmeticOperation()) {
							System.out.println("Inicializacao de atributo com operacao aritmetica correta na linha " + parser.getTokensList().get(parser.index).line);
							return true;
						} else {
							panicModeAttributeInitialization();
						}
					} else {
						panicModeAttributeInitialization();
					}
				}
			} else { // declaracao de atributo ou metodo
				if (parser.tokensToRead() && new VariableParser(parser).recognizeVector()) { // verifica se eh vetor ou matriz
					isVector = true;
					parser.index = parser.index + 1;
				}
				if (parser.tokensToRead() && parser.getTokensList().get(parser.index).lexeme.equals("main") && isVector == false && isFinal == false) { // declaracao da main nao pode ter vetor nem final
					parser.index = parser.index - 1;
					if (!new MethodParser(parser).recognizeMain()) {
						panicModeMethod();
					} else {
						System.out.println("Main correta na linha " + parser.getTokensList().get(parser.index).line);
						return true;
					}
				} else if (parser.tokensToRead() && parser.getTokensList().get(parser.index).type.equals("ID")) {
					parser.index = parser.index + 1;
					if (parser.tokensToRead() && parser.getTokensList().get(parser.index).lexeme.equals("(") && isVector == false && isFinal == false) { // declaracao de metodo nao pode ter vetor nem final
						parser.index = parser.index - 2; // para comecar a varredura da estrutura do metodo a partir do tipo de retorno
						if (!new MethodParser(parser).recognizeMethod()) {
							panicModeMethod();
						} else {
							System.out.println("Metodo correto na linha " + parser.getTokensList().get(parser.index).line);
							return true;
						}
					} else if (parser.getTokensList().get(parser.index).lexeme.equals(";") || parser.getTokensList().get(parser.index).lexeme.equals(",")) { // declaracao de variavel
						parser.index = parser.index - 1; // para comecar a varredura da estrutura de declaracao de variavel a partir do nome
						if (!new VariableParser(parser).recognizeVariableDeclaration()) {
							panicModeAttributeDeclaration();
						} else {
							System.out.println("Declaracao de atributo correta na linha " + parser.getTokensList().get(parser.index).line);
							return true;
						}
					} else {
						panicModeAttributeDeclaration();
					}
				} else {
					panicModeAttributeDeclaration();
				}
			}
		}
		return false;
	}
	
	public void panicModeAttributeDeclaration() {
		parser.addError("ERRO: Declaracao de atributo mal formada na linha " + parser.getTokensList().get(parser.index-1).line);
	}
	
	public void panicModeAttributeInitialization() {
		parser.addError("ERRO: Inicializacao de atributo mal formada na linha " + parser.getTokensList().get(parser.index-1).line);
	}

	public void panicModeMethod() {
		parser.addError("ERRO: Metodo mal formado na linha " + parser.getTokensList().get(parser.index-1).line);
	}
	
}
