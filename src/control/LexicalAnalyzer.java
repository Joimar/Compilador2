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
			// Provável comentário
			if (code.charAt(index) == '/' && (code.charAt(index+1) == '/'
					|| code.charAt(index+1) == '*')) {
				String answer = recognizeComment(code);
				if (answer.equals("err")) {
					System.out.println("Comentario mal formado");
				} else if (answer.equals("EOF")) {
					System.out.println("Comentario nao fechado");
				} else {
					System.out.println("Comentario reconhecido");
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
