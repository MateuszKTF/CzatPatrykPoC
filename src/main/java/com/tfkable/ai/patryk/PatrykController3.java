package com.tfkable.ai.patryk;
 
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
 
import javax.annotation.PostConstruct;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
 
import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
 
@Controller
public class PatrykController3 {
 
	@Autowired
	private LLMService llmService;
	@Autowired
	private FileContentReader fileContentReader;
	@Autowired
	private TextService textService;
	
	private final String modelUrl = "http://localhost:11434";
	private final String modelName = "mistral";
	
	private List<Document> documents;
	private final String questionTemplate = "Twoim zadaniem jest odpowiedzenie na pytanie wyłącznie na podstawie podanych dokumentów. "
			+ "W każdej odpowiedzi musisz wskazać źródło (nazwę dokumentu), z którego pochodzi informacja. "
			+ "Jeśli znajdziesz informacje w kilku dokumentach, podziel je pustą linią i zaznacz źrodło każdej części. \n\n"
			+ "Zawsze odpowiadaj po Polsku. \n\n"
			+ "Podawaj tylko najistotniejsze informacje, bez zbędnych szczegółów.\n\n "
			+ "Pytanie: ";
	
	
	ChatLanguageModel chatModel = OllamaChatModel.builder()
			.timeout(Duration.ofMinutes(1L))
			.baseUrl(modelUrl)
			.modelName(modelName)
			.build();
 
	@RequestMapping("/index")
	public String index() {
		return "index";
	}
 
	@PostConstruct
	public void init() {
//		try {
//			System.out.println("Inicjalizowanie plików...");
//			documents = fileContentReader.readFilesFromDirectory();
//			System.out.println("Załadowano " + documents.size() + " dokumentów");
//		} catch (IOException e) {
//			System.out.println("Błąd odczytu plików: " + e.getMessage());
//		}
//		try {
//			List<String> initExtractPdf = textService.extractPdf("C:\\Users\\mateusz.kotowicz\\Desktop\\Projekty Java\\czatPatrykaPoCv2\\src\\main\\resources\\pliki\\zajaczek.pdf");
//			initExtractPdf.stream().forEach(s -> System.out.println(s));
//			List<String> initTranslatedPdfList = textService.translatePDF(initExtractPdf);
//			textService.createPdf2(initTranslatedPdfList, "C:\\Users\\mateusz.kotowicz\\Desktop\\Projekty Java\\czatPatrykaPoCv2\\src\\main\\resources\\pliki");
//		}catch (IOException e) {
//			System.out.println("Błąd" + e);
//		}
		
		
	}
	
	@RequestMapping("/ask")
	@ResponseBody
	public String ask() {
		return chatModel.generate("Jak sie masz?");
	}
 
	@RequestMapping("/sendMessage")
	@ResponseBody
	public String sendMessage(@RequestBody String message) throws IOException {
		System.out.println("message " + message);
		String question = questionTemplate + message; 
		System.out.println("Przetwarzanie pytania do LLM: " + question);
		String answer = llmService.sendMessageToLLM(question);
		System.out.println(answer);
		return answer;
 
	}
}
