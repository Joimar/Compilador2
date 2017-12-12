package control;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import model.Token;

public class Lexer {
	int index = 0;
	int line = 1;
	//FileWriter fw;
	//BufferedWriter bw;
	ArrayList<String> error = new ArrayList<String>();
	ArrayList<Token> tokensList = new ArrayList<Token>();
	String lastToken = " ";

	
	/**
	 * Reconhece um codigo atribuindo a funcao de reconhecimento dos lexemas
	 * para o automato mais indicado.
	 * @param code
	 * @return Verdadeiro se o codigo for aceito
	 * @throws IOException 
	 */
	boolean recognizeCode(String dir, String filename) throws IOException {
		// if the directory does not exist, create it
		File folder = new File(dir + "/results");
		if (!folder.exists()) {
		    boolean result = false;
		    folder.mkdir();
		    result = true;
		    if(result) {
		        System.out.println("Folder created");  
		    }
		}
		
		String code = readTextFile(dir + "/" + filename);
		index = 0;
		//fw = new FileWriter(dir + "/results/" + filename);
		//bw = new BufferedWriter(fw);

		try {
			while (index < code.length() && code.charAt(index) != 3) {
				while (code.charAt(index) == 9 || code.charAt(index) == 10
						||code.charAt(index) == 13 || code.charAt(index) == 32) {//ignora tab, nova linha, espacos
					if (code.charAt(index) == 10) {
						line++;
					}
					index++;
				}
				// Comentario
				if (code.charAt(index) == '/' && (code.charAt(index+1) == '/'
						|| code.charAt(index+1) == '*')) {
					String answer = recognizeComment(code);
					if (answer.equals("err")) {
						System.out.println("Comentario mal formado");
						//bw.write("Comentario mal formado\n");
					} else if (answer.equals("EOF")) {
						System.out.println("Comentario nao fechado");
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
					//bw.write(line + " " + code.charAt(index) + " delimitador" + "\n");
					tokensList.add(new Token(lastToken, String.valueOf(code.charAt(index)), line));
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
						//bw.write(line + " " + answer.substring(1, answer.length() - 1) + " Cadeia de caracteres\n");
						tokensList.add(new Token(lastToken, answer.substring(1, answer.length() - 1), line));
					}
				}
				// Operador relacional
				else if ((code.charAt(index) == '!' && code.charAt(index+1) == '=')
						|| code.charAt(index) == '=' || code.charAt(index) == '<'
						|| code.charAt(index) == '>') {
					String answer = recognizeRelop(code);
					lastToken = "RELOP";
					tokensList.add(new Token(lastToken, answer, line));
					//System.out.println("Operador relacional");
				}
				// Operador logico
				else if (code.charAt(index) == '!' || code.charAt(index) == '&'
						|| code.charAt(index) == '|') {
					String answer = recognizeLogop(code);
					if (answer.equals("err")) {
						//System.out.println("Operador logico mal formado");
					} else {
						lastToken = "LOGOP";
						tokensList.add(new Token(lastToken, answer, line));
						//System.out.println("Operador logico");
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
							//bw.write(line + " " + answer + " palavra_reservada\n");
						} else {
							lastToken = "ID";
							//bw.write(line + " " + answer + " identificador\n");
						}
						tokensList.add(new Token(lastToken, answer, line));
						//System.out.println(answer);
					}
				}
				// Numero
				else if (isNumber(code, index) || isNegativeNumber(code)) {
					String answer = recognizeNumber(code);
					if (answer.equals("err")) {
						//System.out.println("Numero mal formado");
					} else {
						//System.out.println("Numero:" + answer);
						lastToken = "NUM";
						//bw.write(line + " " + answer + " numero\n");
						tokensList.add(new Token(lastToken, answer, line));
					}
				}
				// Operador aritmetico
				else if (code.charAt(index) == '+' || code.charAt(index) == '-'
						|| code.charAt(index) == '*' || code.charAt(index) == '/'
						|| code.charAt(index) == '%') {
					lastToken = "ARIOP";
					//bw.write(line + " " + code.charAt(index) + " operador_aritmetico\n");
					tokensList.add(new Token(lastToken, String.valueOf(code.charAt(index)), line));
					index++;
				}
				// Desconhecido
				else {
					StringBuilder lexema = new StringBuilder();
					while (!isDelimiter(code, index)) {
						lexema.append(code.charAt(index));
						index++;
					}
					error.add(line + " " + lexema.toString() + " simbolo_invalido\n");
					//index++;
				}
			}
		} catch (StringIndexOutOfBoundsException e) {
			// Fazer nada
		} finally {
			//System.out.println("Codigo lido");
			//bw.write("\n");
			//for (int i = 0; i < error.size(); i++) {
				//bw.write(error.get(i));
			//}
			//bw.close();
			//fw.close();
		}
		return true;
	}
	
	/**
	 * Reconhece comentarios
	 * @return 0-Nao reconhecido; 1-Reconhecido; 2-Comentario nao fechado
	 * @throws IOException 
	 */
	String recognizeComment(String code) throws IOException {
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
				if (code.charAt(index) == 10) {
					line++;
				}
				index++;
			} else {
				star = true;
				lexema.append(code.charAt(index));
				index++;
			}
		}
		error.add(line-1 + " " + lexema.toString().substring(0, lexema.toString().length() - 1) + " comentario_mal_formado\n");
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
		boolean simboloNaoReconhecido = false; // indica se a string possui algum simbolo nao reconhecido
		int errorLine = line; // caso a string esteja errada e tenha mais de uma linha, a linha exibida na saida sera a primeira da string
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
					if (!simboloNaoReconhecido) {
						return lexema.toString();
					} else {
						error.add(errorLine + " " + lexema.toString() + " string_mal_formado\n");
						return "err";
					}
				} else {
					lexema.append(code.charAt(index));
					if (code.charAt(index) < 32 || code.charAt(index) > 126) { 
						if (code.charAt(index) == 10) {
							line++;
						}
						simboloNaoReconhecido = true; // nao eh um simbolo reconhecido pela string
					} 
					index++;
				}
			}
			//bw.write(line + " " + lexema.toString() + " string_mal_formado\n");
			error.add(errorLine + " " + lexema.toString() + " string_mal_formado\n");
			return "EOF";
		} else {
			//bw.write(line + " " + lexema.toString() + " string_mal_formado\n");
			error.add(errorLine + " " + lexema.toString() + " string_mal_formado\n");
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
		//bw.write(line + " " + lexema.toString() + " operador_relacional\n");
		return lexema.toString();
	}

	/**
	 * Reconhece operadores logicos
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
		//bw.write(line + " " + lexema.toString() + " operador_logico\n");
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
			while ((code.charAt(index) >= 65 && code.charAt(index) <= 90) // letra maiuscula
				|| (code.charAt(index) >= 97 && code.charAt(index) <= 122)// letra minuscula
				|| (code.charAt(index) >= 48 && code.charAt(index) <= 57) // digito
				|| (code.charAt(index) == 95)) {                          // underline
				
				lexema.append(code.charAt(index));
				index++;
			}
			// analisar se o identificador eh valido
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
					|| code.charAt(index) == 125 || code.charAt(index) == 58) {
				return lexema.toString();
			}
		}
		while (!isDelimiter(code, index)) {
			lexema.append(code.charAt(index));
			index++;
		}
		error.add(line + " " + lexema.toString() + " identificador_mal_formado\n");
		return "err";
	}
	
	/**
	 * Reconhece numeros
	 * @param code
	 * @return
	 */
	String recognizeNumber(String code) {
		StringBuilder lexema = new StringBuilder();
		
		if (isNumber(code, index)) { // numero positivo
			lexema.append(code.charAt(index));
			index++;
		} else if (code.charAt(index) == '-') { // numero negativo
			index++;
			while (code.charAt(index) == 9 || code.charAt(index) == 10
							|| code.charAt(index) == 13 || code.charAt(index) == 32) {
				if (code.charAt(index) == 10) {
					line++;
				}
				index++;
			}
			if (isNumber(code, index)) {
				lexema.append("-" + code.charAt(index));
				index++;
			}
		}
		
		while (isNumber(code, index)) { // enquanto houver digitos, acrescentar ao lexema
			lexema.append(code.charAt(index));
			index++;
		}
		
		if (code.charAt(index) == '.' && isNumber(code, index+1)) { // numero com casa decimal
			lexema.append("." + code.charAt(index+1));
			index+=2;
			while (isNumber(code, index)) {
				lexema.append(code.charAt(index));
				index++;
			}
		} 
		
		if (isDelimiter(code, index)) { // analisar se eh um numero valido
			return lexema.toString();
		} else {
			while (!isDelimiter(code, index)) {
				lexema.append(code.charAt(index));
				index++;
			}
			error.add(line + " " + lexema.toString() + " numero_mal_formado\n");
		}
		
		//index++;
		return "err";
	}
	
	/**
	 * Verifica se o caractere no indice especificado eh do tipo numero
	 * @param code
	 * @param index
	 * @return
	 */
	boolean isNumber(String code, int index) {
		return code.charAt(index) >= 48 && code.charAt(index) <= 57;
	}
	
	/**
	 * Verifica se eh numero negativo
	 * @param code
	 * @param index
	 * @return
	 */
	boolean isNegativeNumber(String code) {
		int auxIndex = index;
		// precisa comecar sinal negativo e o token anterior nao pode ser numero nem identificador
		if (code.charAt(auxIndex) == '-' && !lastToken.equals("NUM") && !lastToken.equals("ID")) { 
			auxIndex++;
			// pode haver espacos entre o sinal negativo e o primeiro digito
			while (code.charAt(auxIndex) == 9 || code.charAt(auxIndex) == 10
							|| code.charAt(auxIndex) == 13 || code.charAt(auxIndex) == 32) {
				auxIndex++;
			}
			if (isNumber(code, auxIndex)) { // verifica se o primeiro caractere apos os espacos eh um digito
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Verifica se chegou a um delimitador de identificador ou numero
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
				|| code.charAt(index) == 10 || code.charAt(index) == 13 || code.charAt(index) == 58;
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