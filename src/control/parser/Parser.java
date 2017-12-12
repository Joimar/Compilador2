package control.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import model.Token;

public class Parser {
	
	private ArrayList<Token> tokensList; // lista de tokens recebida do lexico
	FileWriter fw;
	BufferedWriter bw;
	
	public Parser(ArrayList<Token> tokensList) {
		this.tokensList = tokensList;
	}
	
	public void readTokens(String dir, String filename) throws IOException {
		System.out.println("---------- INICIO DA ANALISE SINTATICA ----------");
		FileParser fp = new FileParser(tokensList);
		fp.fileParsing();
		System.out.println("---------- FIM DA ANALISE SINTATICA ----------");
		
		File folder = new File(dir + "/results");
		if (!folder.exists()) {
		    boolean result = false;
		    folder.mkdir();
		    result = true;
		    if(result) {
		        System.out.println("Folder created");  
		    }
		}
		
		fw = new FileWriter(dir + "/results/" + filename);
		bw = new BufferedWriter(fw);
		ArrayList<String> errorsList = fp.getErrorsList();
		if (errorsList.size() == 0) {
			//System.out.println("Codigo sintaticamente correto");
			bw.write("Codigo sintaticamente correto");
		} else {
			for (int i = 0; i < errorsList.size(); i++) {
				System.out.println(errorsList.get(i));
				bw.write(errorsList.get(i));
			}
		}
		bw.close();
		fw.close();
	}
}