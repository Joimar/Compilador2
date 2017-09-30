package control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LexicalAnalyzer {
	int index = 0;
	
	public static void main(String[] args) {
		LexicalAnalyzer la = new LexicalAnalyzer();
		try {
			String dir_codes = "entrada";
			String[] filenames = la.getFilenames(dir_codes);
			System.out.println(filenames.length + " arquivos encontrados");
			for (int i = 0; i < filenames.length; i++) {
				System.out.println("====================================");
				//System.out.println(filenames[i]);
				//System.out.println(la.readTextFile(dir_codes + "/" + filenames[i]));
				la.recognizeCode(la.readTextFile(dir_codes + "/" + filenames[i]));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reconhece um codigo atribuindo a funcao de reconhecimento dos lexemas
	 * para o automato mais indicado.
	 * @param code
	 * @return Verdadeiro se o codigo for aceito
	 */
	boolean recognizeCode(String code) {
		index = 0;
		try {
			while (index < code.length() && code.charAt(index) != 3) {
				while (code.charAt(index) == 9 || code.charAt(index) == 10
						||code.charAt(index) == 13 || code.charAt(index) == 32) {//ignora tab, nova linha, espacos
					index++;
				}
				// Comentário
				if (code.charAt(index) == '/' && (code.charAt(index+1) == '/'
						|| code.charAt(index+1) == '*')) {
					String answer = recognizeComment(code);
					if (answer.equals("err")) {
						System.out.println("Comentario mal formado");
					} else if (answer.equals("EOF")) {
						System.out.println("Comentario nao fechado");
					} else {
						System.out.println("Comentario");
						//System.out.println(answer);
					}
				}
				// Delimitador
				else if (code.charAt(index) == ';' || code.charAt(index) == ','
						|| code.charAt(index) == '(' || code.charAt(index) == ')'
						|| code.charAt(index) == '[' || code.charAt(index) == ']'
						|| code.charAt(index) == '{' || code.charAt(index) == '}'
						|| code.charAt(index) == ':') {
					index++;
					System.out.println("Delimitador");
				}
				// Cadeia de caracteres
				else if (code.charAt(index) == '"') {
					String answer = recognizeString(code);
					if (answer.equals("EOF")) {
						System.out.println("Cadeia de caracteres nao fechada");
					} else {
						System.out.println("Cadeia de caracteres");
						//System.out.println(answer);
					}
				}
				// Operador relacional
				else if ((code.charAt(index) == '!' && code.charAt(index+1) == '=')
						|| code.charAt(index) == '=' || code.charAt(index) == '<'
						|| code.charAt(index) == '>') {
					String answer = recognizeRelop(code);
					System.out.println("Operador relacional");
					//System.out.println(answer);
				}
				// Operador lógico
				else if (code.charAt(index) == '!' || code.charAt(index) == '&'
						|| code.charAt(index) == '|') {
					String answer = recognizeLogop(code);
					if (answer.equals("err")) {
						System.out.println("Operador lógico mal formado");
					} else {
						System.out.println("Operador lógico");
						//System.out.println(answer);
					}
				}
				// Identificador ou palavra reservada
				else if ((code.charAt(index) >= 65 && code.charAt(index) <= 90)
						|| (code.charAt(index) >= 97 && code.charAt(index) <= 122)) {
					String answer = recognizeID(code);
					if (answer.equals("err")) {
						System.out.println("Identificador mal formado");
					} else {
						if (answer.equals("class") || answer.equals("final") || answer.equals("if")
								|| answer.equals("else") || answer.equals("for") || answer.equals("scan")
								|| answer.equals("print") || answer.equals("int") || answer.equals("float")
								|| answer.equals("bool") || answer.equals("true") || answer.equals("false")
								|| answer.equals("string")) {
							System.out.println("Palavra reservada");
						} else {
							System.out.println("Identificador");
						}
						//System.out.println(answer);
					}
				}
				// Desconhecido
				else {
					index++;
					System.out.println("Desconhecido");
				}
			}
		} catch (StringIndexOutOfBoundsException e) {
			System.out.println("Código lido");
		}
		return true;
	}
	
	/**
	 * Reconhece comentarios
	 * @return 0-Nao reconhecido; 1-Reconhecido; 2-Comentario nao fechado
	 */
	String recognizeComment(String code) {
		StringBuilder lexema = new StringBuilder();
		if (code.charAt(index) == '/') {
			lexema.append(code.charAt(index));
			index++;
		} else {
			return "err";
		}
		if (code.charAt(index) == '*') {//comentario de bloco
			lexema.append(code.charAt(index));
			index++;
		} else if (code.charAt(index) == '/') {//comentario de linha
			lexema.append(code.charAt(index));
			index++;
			while (index < code.length() && code.charAt(index) != 10 && code.charAt(index) != 13) {
				lexema.append(code.charAt(index));
				index++;
			}
			return lexema.toString();
		} else {
			return "err";
		}
		boolean star = false;
		while (index < code.length()) {
			if (star && code.charAt(index) == ('/')) {
				lexema.append(code.charAt(index));
				index++;
				return lexema.toString();
			}
			if (code.charAt(index) != ('*')) {
				star = false;
				lexema.append(code.charAt(index));
				index++;
			} else {
				star = true;
				lexema.append(code.charAt(index));
				index++;
			}
		}
		return "EOF";
	}
	
	/**
	 * Reconhece cadeia de caracteres
	 * @param code
	 * @return
	 */
	String recognizeString(String code) {
		StringBuilder lexema = new StringBuilder();
		if (code.charAt(index) == '"') {
			lexema.append(code.charAt(index));
			index++;
			while (index < code.length()) {
				if (code.charAt(index) == '\\' && code.charAt(index+1) == '"') {
					lexema.append('"');
					index+=2;
				} else if (code.charAt(index) == '"') {
					lexema.append('"');
					index++;
					return lexema.toString();
				} else {
					lexema.append(code.charAt(index));
					index++;
				}
			}
			return "EOF";
		} else {
			return "err";
		}
	}
	
	/**
	 * Reconhece operadores relacionais
	 * @param code
	 * @return
	 */
	String recognizeRelop(String code) {
		StringBuilder lexema = new StringBuilder();
		if (code.charAt(index) == '!' && code.charAt(index+1) == '=') {
			lexema.append("!=");
			index+=2;
		} else if (code.charAt(index) == '=') {
			lexema.append("=");
			index++;
		} else if (code.charAt(index) == '<' && code.charAt(index+1) == '=') {
			lexema.append("<=");
			index+=2;
		} else if (code.charAt(index) == '>' && code.charAt(index+1) == '=') {
			lexema.append(">=");
			index+=2;
		} else if (code.charAt(index) == '<') {
			lexema.append("<");
			index++;
		} else if (code.charAt(index) == '>') {
			lexema.append(">");
			index++;
		}
		return lexema.toString();
	}

	/**
	 * Reconhece operadores lógicos
	 * @param code
	 * @return
	 */
	String recognizeLogop(String code) {
		StringBuilder lexema = new StringBuilder();
		if (code.charAt(index) == '&') {
			index++;
			if (code.charAt(index) == '&') {
				lexema.append("&&");
				index++;
			} else {
				lexema.append("err");
			}
		} else if (code.charAt(index) == '|') {
			index++;
			if (code.charAt(index) == '|') {
				lexema.append("||");
				index++;
			} else {
				lexema.append("err");
			}
		} else if (code.charAt(index) == '!') {
			lexema.append("!");
			index++;
		}
		return lexema.toString();
	}

	/**
	 * Reconhece identificadores e palavras reservadas
	 * @param code
	 * @return
	 */
	String recognizeID(String code) {
		StringBuilder lexema = new StringBuilder();
		if ((code.charAt(index) >= 65 && code.charAt(index) <= 90)
				|| (code.charAt(index) >= 97 && code.charAt(index) <= 122)) {
			lexema.append(code.charAt(index));
			index++;
			while ((code.charAt(index) >= 65 && code.charAt(index) <= 90) // letra maiúscula
				|| (code.charAt(index) >= 97 && code.charAt(index) <= 122)// letra minúscula
				|| (code.charAt(index) >= 48 && code.charAt(index) <= 57) // dígito
				|| (code.charAt(index) == 95)) {                          // underline
				
				lexema.append(code.charAt(index));
				index++;
			}
			// analisar se o identificador é válido
			if (code.charAt(index) == 32 || code.charAt(index) == 33
					|| code.charAt(index) == 34 || code.charAt(index) == 37
					|| code.charAt(index) == 38 || code.charAt(index) == 40
					|| code.charAt(index) == 41 || code.charAt(index) == 42
					|| code.charAt(index) == 43 || code.charAt(index) == 44
					|| code.charAt(index) == 45 || code.charAt(index) == 47
					|| code.charAt(index) == 59 || code.charAt(index) == 60
					|| code.charAt(index) == 61 || code.charAt(index) == 62
					|| code.charAt(index) == 91 || code.charAt(index) == 93
					|| code.charAt(index) == 123 || code.charAt(index) == 124
					|| code.charAt(index) == 125 || code.charAt(index) == 9
					|| code.charAt(index) == 10 || code.charAt(index) == 13) {
				return lexema.toString();
			}
		}
		index++;
		return "err";
	}
	
	/**
	 * Le arquivo e retorna todo o conteudo dentro de uma string
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	String readTextFile(String filename) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
	    StringBuilder sb = new StringBuilder();
		try {
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		} finally {
		    br.close();
		}
	    return sb.toString();
	}
	
	/**
	 * Obtem os nomes dos arquivos em um diretorio, ignorando subdiretorios
	 * @param path
	 * @return Nomes dos arquivos
	 */
	String[] getFilenames(String path) {
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		String[] filenames = new String[listOfFiles.length];
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				filenames[i] = listOfFiles[i].getName();
			} else if (listOfFiles[i].isDirectory()) {
				// Fazer nada
			}
		}
		return filenames;
	}
}
