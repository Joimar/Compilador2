package model;

public class Token {
	
	public String type;
	public String lexeme;
	public int line;
	
	public Token(String type, String lexeme, int line) {
		this.type = type;
		this.lexeme = lexeme;
		this.line = line;
	}

}
