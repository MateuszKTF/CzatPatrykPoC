package com.tfkable.ai.patryk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.data.repository.init.ResourceReader;
import org.springframework.stereotype.Service;

import dev.langchain4j.data.document.Document;

@Service
public class FileContentReader {

//	ćwiczenia:
//
//	1 zablokuj przycisk po kliknięciu, odblokuj kiedy przyjdzie odpowiedź
//	  użyj attr("disabled",true) i attr("disabled",false)
//	2 pokaż ikonę ładowania w czasie oczekiwania na odpowiedź serwera, ukryj po otrzymaniu odpowiedzi
//	  użyj metod jquery show() i hide()
//	3 dodaj pytania użytkownika do głównego okna czatu, na podobnej zasadzie jak odpowiedzi serwera
//	3 stwórz klasę typu serwis i przenieś tam logikę komunikacji z LLM
//	4 wyciągnij obiekty do komunikacji z modelem poza metodę
//	5 wyciągnij logikę oczytywania plików do metody init
//	6 wyciągnij zmienne typu String z metod do poziomu klasy (zmienne instancji)
//	7 spróbuj udoskonalić promp żeby pokazywał z którego źródła ma inforacje i żeby udzielał bardziej precyzyjnej odpowiedzi
//
//	na luzie, później to zrobimy wspólnie

    public List<Document> readFilesFromDirectory() throws IOException {
    	
    	String directoryPath = "pliki"; 
        URL resource = ResourceReader.class.getClassLoader().getResource(directoryPath);
    	
        File directory = new File(resource.getFile());
        List<Document> documents = new ArrayList<>();

        if (!directory.isDirectory()) {
            throw new IOException("Podana ścieżka nie jest katalogiem.");
        }

        for (File file : directory.listFiles()) {
            try {
				if (file.isFile()) {
				    String fileName = file.getName().toLowerCase();

				    if (fileName.endsWith(".pdf")) {
				        documents.add(createDocument(file, readPdfFile(file)));
				    } else if (fileName.endsWith(".doc")) {
				        documents.add(createDocument(file, readDocFile(file)));
				    } else if (fileName.endsWith(".docx")) {
				        documents.add(createDocument(file, readDocxFile(file)));
				    }
				}
			} catch (Exception e) {
				System.out.println(e);
			}
        }
        
        return documents;
    }

    private Document createDocument(File file, String content) {
        return new Document(content);
    }

    private static String readPdfFile(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }

    private static String readDocFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             HWPFDocument document = new HWPFDocument(fis);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }

    private static String readDocxFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            StringBuilder content = new StringBuilder();

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                content.append(paragraph.getText()).append(System.lineSeparator());
            }

            return content.toString();
        }
    }
}
