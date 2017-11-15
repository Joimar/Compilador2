package control;

import java.io.IOException;

public class Main {
	public static void main(String[] args) {
		Lexer la = new Lexer();
		try {
			String dir_codes = "entrada";
			String[] filenames = la.getFilenames(dir_codes);
			System.out.println(filenames.length + " arquivos encontrados\n");
			for (int i = 0; i < filenames.length; i++) {
				//System.out.println("====================================");
				//System.out.println(filenames[i]);
				//System.out.println(la.readTextFile(dir_codes + "/" + filenames[i]));
				la.error.clear();
				la.recognizeCode(dir_codes, filenames[i]);			
				Parser parser = new Parser(la.tokensList);
				parser.readTokens();
				System.out.println("---------- FIM DA ANALISE SINTATICA ----------");
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
}
