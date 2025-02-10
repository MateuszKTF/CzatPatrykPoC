package com.tfkable.ai.patryk;

import java.awt.JobAttributes.SidesType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.springframework.stereotype.Service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

@Service
public class TextService {
	
	ChatLanguageModel chatModel = OllamaChatModel.builder()
            .baseUrl("http://localhost:11434")
            .modelName("mistral")
            .build();
	
	public List<String> extractPdf(String filePath) throws IOException {
		File file = new File(filePath);
		if(!file.exists() || !file.isFile()) {
			throw new IOException("Plik nie istnieje" + filePath);
		}
		
		
		PDDocument document = PDDocument.load(file);
		PDFTextStripper pdfStripper = new PDFTextStripper();
		String text = pdfStripper.getText(document);
		document.close();
		
		List<String> sentences = new ArrayList<>();
		Pattern sentencePattern = Pattern.compile("[^.!?]+[.!?]");
		Matcher matcher = sentencePattern.matcher(text);
		while (matcher.find()) {
			sentences.add(matcher.group().trim()); 
			}
		
		return sentences;
	}
	
	public List<String> translatePDF(List<String> sentences){
		List<String> translatedSentenceList = new ArrayList<>();
		
		for(String s : sentences) {
			String translationPrompt = "Przetłumacz to zdanie na angielski: " + s;
			String translated = chatModel.generate(translationPrompt);
			translatedSentenceList.add(translated);
			System.out.println(translated);
		}
		return translatedSentenceList;
	}
	
	
	public void createPdf(List<String> translatedSentenceList, String outPut) {
        String pdfPath = "example_pdfbox.pdf";
        try (PDDocument document = new PDDocument()) {
 
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
 
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText("Witaj w dokumencie PDF wygenerowanym w Javie!");
                contentStream.endText();
            }
            document.save(pdfPath);
 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
	
//	"Translate the sentence into English." +
//	" No additional footnotes"

	
