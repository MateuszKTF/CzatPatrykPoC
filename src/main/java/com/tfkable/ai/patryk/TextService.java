package com.tfkable.ai.patryk;

import java.awt.JobAttributes.SidesType;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.springframework.stereotype.Service;
import org.thymeleaf.standard.expression.Each;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

@Service
public class TextService {
	
	String promptTranslate = "translate sentence into english. " +
							"always use only one translation option. " +
							"do not add your own comments or footnotes. " +
							"do not suggest formal forms. " +
							"here's the sentence: ";
	
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
			String translationPrompt = promptTranslate + s;
			String translated = chatModel.generate(translationPrompt);
			translatedSentenceList.add(translated);
			System.out.println(translated);
		}
		return translatedSentenceList;
	}
	
	
	public void createPdf(List<String> translatedSentenceList, String outPut) {
		
		System.out.println("Rozpoczynam tworzenie pliku");
		String pdfPath = "test.pdf";
        try (PDDocument document = new PDDocument()) {
 
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            File fontField = new File("C:\\Windows\\Fonts\\arial.ttf");
            PDType0Font font = PDType0Font.load(document, new FileInputStream(fontField));
 
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(font, 14);
                contentStream.newLineAtOffset(10, 900);
                for(String s : translatedSentenceList) {
                	contentStream.showText(s);
                	contentStream.newLineAtOffset(0, -20);
                }
                contentStream.endText();
            }
            document.save(pdfPath);
 
        } catch (IOException e) {
            e.printStackTrace();
        }
//                
    }
	
	public void createPdf2(List<String> translatedSentenceList, String outPut) {
		System.out.println("Rozpoczynam tworzenie pliku");
		String pdfPath = "C:\\Users\\mateusz.kotowicz\\Desktop\\Projekty Java\\czatPatrykaPoCv2\\src\\main\\resources\\translated\\trans_zajac.pdf";
        try (PDDocument document = new PDDocument()) {
 
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            PDType0Font font;
            try(FileInputStream fontStream = new FileInputStream(new File("C:\\Windows\\Fonts\\arial.ttf"))){
            font = PDType0Font.load(document, fontStream);
            }
 
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(font, 14);
            contentStream.beginText();
            	
            float margin = 50; //Lewy margines
            float pageWidth = page.getMediaBox().getWidth() - 2 * margin;
            float yStart = page.getMediaBox().getHeight() - 50; //Górny margines
            float leading = 18; // Odstęp między linijkami
            float yPosition = yStart;
        
                contentStream.newLineAtOffset(margin, yPosition);
                
                for(String s : translatedSentenceList) {
                	List<String> wrappedLines = wrapText(s, font, 14, pageWidth);
                	for (String line : wrappedLines) {
	                	if (yPosition <=50) {
							contentStream.endText();
							contentStream.close();
							
							page = new PDPage(PDRectangle.A4);
							document.addPage(page);
							contentStream = new PDPageContentStream(document, page);
							contentStream.setFont(font, 14);
			            	contentStream.beginText();
			            	yPosition = yStart;
			            	contentStream.newLineAtOffset(margin, yPosition);
						}
	                	
	                	contentStream.showText(line);
	                	yPosition -=leading;
	                	contentStream.newLineAtOffset(0, -leading);
                	}
                }
            contentStream.endText();
            contentStream.close();
            
            document.save(pdfPath);
 
        } catch (IOException e) {
            e.printStackTrace();
        }
        
	}
	
	private List<String> wrapText(String text, PDType0Font font, float fontSize, float maxWidth) throws IOException {
		List<String> lines = new ArrayList<>();
		String[] words = text.split(" ");
		StringBuilder currentLine = new StringBuilder();
		float spaceWidth = font.getStringWidth(" ") / 1000 * fontSize;
		
		for(String word : words) {
			String testLine = (currentLine.length() == 0) ? word : currentLine + " " + word;
			float textWidth = font.getStringWidth(testLine) / 1000 * fontSize;
			
			if (textWidth > maxWidth) {
				lines.add(currentLine.toString());
				currentLine = new StringBuilder(word);
			}else {
				if (currentLine.length() > 0) {
					currentLine.append(" ");
				}
				currentLine.append(word);
			}
		}
		if (currentLine.length() > 0) {
			lines.add(currentLine.toString());
		}
		
		return lines;
	}
}
	

	
