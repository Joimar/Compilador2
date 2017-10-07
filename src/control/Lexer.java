package control;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Lexer {
	int index = 0;
	int line = 1;
	FileWriter fw;
	BufferedWriter bw;
	ArrayList<String> error = new ArrayList<String>();
	String lastToken = " ";
	
	public static void main(String[] args) {
		Lexer la = new Lexer();
		try {
			String dir_codes = "entrada";
			String[] filenames = la.getFilenames(dir_codes);
			System.out.println(filenames.length + " arquivos encontrados");
			for (int i = 0; i < filenames.length; i++) {
				System.out.println("====================================");
				//System.out.println(filenames[i]);
				//System.out.println(la.readTextFile(dir_codes + "/" + filenames[i]));
				la.recognizeCode(dir_codes, filenames[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (la.bw != null)
					la.bw.close();
				
				if (la.fw != null)
					la.fw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Reconhece um codigo atribuindo a funcao de reconhecimento dos lexemas
	 * para o automato mais indicado.
	 * @param code
	 * @return Verdadeiro se o codigo for aceito
	 * @throws IOException 
	 */
	boolean recognizeCode(String dir, String filename) throws IOException {
		String code = readTextFile(dir + "/" + filename);
		index = 0;
		fw = new FileWriter(dir + "/results/" + filename);
		bw = new BufferedWriter(fw);

		try {
			while (index < code.length() && code.charAt(index) != 3) {
				while (code.charAt(index) == 9 || code.charAt(index) == 10
						||code.charAt(index) == 13 || code.charAt(index) == 32) {//ignora tab, nova linha, espacos
					if (code.charAt(index) == 10 || code.charAt(index) == 13) {
						line++;
					}
					index++;
				}
				// Comentário
				if (code.charAt(index) == '/' && (code.charAt(index+1) == '/'
						|| code.charAt(index+1) == '*')) {
					String answer = recognizeComment(code);
					if (answer.equals("err")) {
						System.out.println("Comentario mal formado");
						bw.write("Comentário mal formado\n");
					} else if (answer.equals("EOF")) {
						System.out.println("Comentario nao fechado");
						bw.write("Comentário não fechado\n");
					} else {
						lastToken = "COM";
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
					//System.out.println("Delimitador");
					lastToken = "DEL";
					bw.write(line + " " + code.charAt(index) + " delimitador" + "\n");
					index++;
				}
				// Cadeia de caracteres
				else if (code.charAt(index) == '"') {
					String answer = recognizeString(code);
					if (answer.equals("EOF") || answer.equals("err")) {
						//System.out.println("Cadeia de caracteres nao fechada");
					} else {
						//System.out.println("Cadeia de caracteres");
						lastToken = "STR";
						bw.write(line + " " + answer.substring(1, answer.length() - 1) + " Cadeia de caracteres\n");
					}
				}
				// Operador relacional
				else if ((code.charAt(index) == '!' && code.charAt(index+1) == '=')
						|| code.charAt(index) == '=' || code.charAt(index) == '<'
						|| code.charAt(index) == '>') {
					String answer = recognizeRelop(code);
					lastToken = "RELOP";
					//System.out.println("Operador relacional");
				}
				// Operador lógico
				else if (code.charAt(index) == '!' || code.charAt(index) == '&'
						|| code.charAt(index) == '|') {
					String answer = recognizeLogop(code);
					if (answer.equals("err")) {
						//System.out.println("Operador lógico mal formado");
					} else {
						lastToken = "LOGOP";
						//System.out.println("Operador lógico");
					}
				}
				// Identificador ou palavra reservada
				else if ((code.charAt(index) >= 65 && code.charAt(index) <= 90)
						|| (code.charAt(index) >= 97 && code.charAt(index) <= 122)) {
					String answer = recognizeID(code);
					if (answer.equals("err")) {
						//
					} else {
						if (answer.equals("class") || answer.equals("final") || answer.equals("if")
								|| answer.equals("else") || answer.equals("for") || answer.equals("scan")
								|| answer.equals("print") || answer.equals("int") || answer.equals("float")
								|| answer.equals("bool") || answer.equals("true") || answer.equals("false")
								|| answer.equals("string")) {
							lastToken = "RES";
							bw.write(line + " " + answer + " palavra_reservada\n");
						} else {
							lastToken = "ID";
							bw.write(line + " " + answer + " identificador\n");
						}
						//System.out.println(answer);
					}
				}
				// Número
				else if (isNumber(code, index) || isNegativeNumber(code)) {
					String answer = recognizeNumber(code);
					if (answer.equals("err")) {
						//System.out.println("Número mal formado");
					} else {
						//System.out.println("Número:" + answer);
						lastToken = "NUM";
						bw.write(line + " " + answer + " número\n");
					}
				}
				// Operador aritmético
				else if (code.charAt(index) == '+' || code.charAt(index) == '-'
						|| code.charAt(index) == '*' || code.charAt(index) == '/'
						|| code.charAt(index) == '%') {
					lastToken = "ARIOP";
					bw.write(line + " " + code.charAt(index) + " operador_aritmetico\n");
					index++;
				}
				// Desconhecido
				else {
					error.add(line + " " + code.charAt(index) + " simbolo_invalido\n");
					index++;
				}
			}
		} catch (StringIndexOutOfBoundsException e) {
			System.out.println("Código lido");
			bw.write("\n");
			for (int i = 0; i < error.size(); i++) {
				bw.write(error.get(i));
			}
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
			line++;
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
				if (code.charAt(index) == 10 || code.charAt(index) == 13) {
					line++;
				}
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
	 * @throws IOException 
	 */
	String recognizeString(String code) throws IOException {
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
					if (code.charAt(index) == 10 || code.charAt(index) == 13) {
						//bw.write(line + " " + lexema.toString() + " string_mal_formado\n");
						error.add(line + " " + lexema.toString() + " string_mal_formado\n");
						line++;
						index++;
						return "err";
					} else {
						lexema.append(code.charAt(index));
					}
					index++;
				}
			}
			//bw.write(line + " " + lexema.toString() + " string_mal_formado\n");
			error.add(line + " " + lexema.toString() + " string_mal_formado\n");
			return "EOF";
		} else {
			//bw.write(line + " " + lexema.toString() + " string_mal_formado\n");
			error.add(line + " " + lexema.toString() + " string_mal_formado\n");
			return "err";
		}
	}
	
	/**
	 * Reconhece operadores relacionais
	 * @param code
	 * @return
	 * @throws IOException
	 */
	String recognizeRelop(String code) throws IOException {
		StringBuilder lexema = new StringBuilder();
		String value = "";
		if (code.charAt(index) == '!' && code.charAt(index+1) == '=') {
			lexema.append("!=");
			index+=2;
			value = "NE";
		} else if (code.charAt(index) == '=') {
			lexema.append("=");
			index++;
			value = "EQ";
		} else if (code.charAt(index) == '<' && code.charAt(index+1) == '=') {
			lexema.append("<=");
			index+=2;
			value = "LE";
		} else if (code.charAt(index) == '>' && code.charAt(index+1) == '=') {
			lexema.append(">=");
			index+=2;
			value = "GE";
		} else if (code.charAt(index) == '<') {
			lexema.append("<");
			index++;
			value = "LT";
		} else if (code.charAt(index) == '>') {
			lexema.append(">");
			index++;
			value = "GT";
		}
		bw.write(line + " " + lexema.toString() + " operador_relacional\n");
		return lexema.toString();
	}

	/**
	 * Reconhece operadores lógicos
	 * @param code
	 * @return
	 * @throws IOException
	 */
	String recognizeLogop(String code) throws IOException {
		StringBuilder lexema = new StringBuilder();
		String value = "";
		if (code.charAt(index) == '&') {
			index++;
			if (code.charAt(index) == '&') {
				lexema.append("&&");
				index++;
				value = "AND";
			} else {
				//bw.write(line + " & operador_logico_mal_formado\n");
				error.add(line + " & operador_logico_mal_formado\n");
				lexema.append("err");
				return lexema.toString();
			}
		} else if (code.charAt(index) == '|') {
			index++;
			if (code.charAt(index) == '|') {
				lexema.append("||");
				index++;
				value = "OR";
			} else {
				//bw.write(line + " | operador_logico_mal_formado\n");
				error.add(line + " | operador_logico_mal_formado\n");
				lexema.append("err");
				return lexema.toString();
			}
		} else if (code.charAt(index) == '!') {
			lexema.append("!");
			index++;
			value = "NOT";
		}
		bw.write(line + " " + lexema.toString() + " operador_logico\n");
		return lexema.toString();
	}

	/**
	 * Reconhece identificadores e palavras reservadas
	 * @param code
	 * @return
	 * @throws IOException 
	 */
	String recognizeID(String code) throws IOException {
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
			if (code.charAt(index) == 9 || code.charAt(index) == 10
					|| code.charAt(index) == 13 || code.charAt(index) == 32
					|| code.charAt(index) == 33 || code.charAt(index) == 37
					|| code.charAt(index) == 38 || code.charAt(index) == 40
					|| code.charAt(index) == 41 || code.charAt(index) == 42
					|| code.charAt(index) == 43 || code.charAt(index) == 44
					|| code.charAt(index) == 45 || code.charAt(index) == 47
					|| code.charAt(index) == 59 || code.charAt(index) == 60
					|| code.charAt(index) == 61 || code.charAt(index) == 62
					|| code.charAt(index) == 91 || code.charAt(index) == 93
					|| code.charAt(index) == 123 || code.charAt(index) == 124
					|| code.charAt(index) == 125) {
				return lexema.toString();
			}
		}
		lexema.append(code.charAt(index));
		//bw.write(line + " " + lexema.toString() + " identificador_mal_formado\n");
		error.add(line + " " + lexema.toString() + " identificador_mal_formado\n");
		index++;
		return "err";
	}
	
	/**
	 * Reconhece números
	 * @param code
	 * @return
	 */
	String recognizeNumber(String code) {
		StringBuilder lexema = new StringBuilder();

		
		if (isNumber(code, index)) { // número positivo
			lexema.append(code.charAt(index));
			index++;
		} else if (code.charAt(index) == '-') { // número negativo
			index++;
			while (code.charAt(index) == 9 || code.charAt(index) == 10
							|| code.charAt(index) == 13 || code.charAt(index) == 32) {
				if (code.charAt(index) == 10 || code.charAt(index) == 13) {
					line++;
				}
				index++;
			}
			if (isNumber(code, index)) {
				lexema.append("-" + code.charAt(index));
				index++;
			}
		}
		
		while (isNumber(code, index)) { // enquanto houver dígitos, acrescentar ao lexema
			lexema.append(code.charAt(index));
			index++;
		}
		
		if (code.charAt(index) == '.' && isNumber(code, index+1)) { // número com casa decimal
			lexema.append("." + code.charAt(index+1));
			index+=2;
			while (isNumber(code, index)) {
				lexema.append(code.charAt(index));
				index++;
			}
		} 
		
		if (isDelimiter(code, index)) { // analisar se é um número válido
			return lexema.toString();
		}
		
		//index++;
		return "err";
	}
	
	/**
	 * Verifica se o caractere no índice especificado é do tipo número
	 * @param code
	 * @param index
	 * @return
	 */
	boolean isNumber(String code, int index) {
		return code.charAt(index) >= 48 && code.charAt(index) <= 57;
	}
	
	/**
	 * Verifica se é número negativo
	 * @param code
	 * @param index
	 * @return
	 */
	boolean isNegativeNumber(String code) {
		int auxIndex = index;
		// precisa começar sinal negativo e o token anterior não pode ser número nem identificador
		if (code.charAt(auxIndex) == '-' && !lastToken.equals("NUM") && !lastToken.equals("ID")) { 
			auxIndex++;
			// pode haver espaços entre o sinal negativo e o primeiro dígito
			while (code.charAt(auxIndex) == 9 || code.charAt(auxIndex) == 10
							|| code.charAt(auxIndex) == 13 || code.charAt(auxIndex) == 32) {
				auxIndex++;
			}
			if (isNumber(code, auxIndex)) { // verifica se o primeiro caractere após os espaços é um dígito
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Verifica se chegou a um delimitador de identificador ou número
	 * @param code
	 * @param index
	 * @return
	 */
	boolean isDelimiter(String code, int index) {
		return code.charAt(index) == 32 || code.charAt(index) == 33
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
				|| code.charAt(index) == 10 || code.charAt(index) == 13;
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
		ArrayList<String> filenames = new ArrayList<String>();
		//String[] filenames = new String[listOfFiles.length];
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				filenames.add(listOfFiles[i].getName());
			} else if (listOfFiles[i].isDirectory()) {
				// Fazer nada
			}
		}
		String[] filenamesArray = new String[filenames.size()];
		filenames.toArray(filenamesArray);
		return filenamesArray;
	}
}