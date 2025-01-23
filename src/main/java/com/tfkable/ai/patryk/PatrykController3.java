package com.tfkable.ai.patryk;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
	FileContentReader reader;

	@RequestMapping("/index")
	public String index() {
		return "index";
	}

	@PostConstruct
	public void init() {

	}

	@RequestMapping("/sendMessage")
	@ResponseBody
	public String sendMessage(@RequestBody String message) throws IOException {

		System.out.println("message " + message);
		
		String question = "Odpowiedz na pytanie używając tylko treści embeddingów. "
				+ "Odpowiedz rozpocznij od wskazania nazwy dokumentu z którego pochodzą informacje. "
				+ "Odpowiedzi z poszczególnych dokumentów rozdziel pustą linia i zacznij od nowej linii. "
				+ "Pytanie brzmi: " + message;

		EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

		EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

		EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
				.documentSplitter(DocumentSplitters.recursive(500, 0)).embeddingModel(embeddingModel)
				.embeddingStore(embeddingStore).build();

		List<Document> documents = reader.readFilesFromDirectory();
		ingestor.ingest(documents);
		
		ChatLanguageModel chatModel = OllamaChatModel.builder().baseUrl("http://10.7.10.150:11434").modelName("mistral")
				.build();

		MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder().id("my-chat-id") // Opcjonalne:
																								// identyfikator pamięci
				.maxMessages(50) // Maksymalna liczba wiadomości w oknie
				.chatMemoryStore(new InMemoryChatMemoryStore()) // Domyślna implementacja przechowywania
				.build();

		ConversationalRetrievalChain chain = ConversationalRetrievalChain.builder().chatMemory(chatMemory)
				.chatLanguageModel(chatModel).retriever(EmbeddingStoreRetriever.from(embeddingStore, embeddingModel))
				.build();

		String answer = chain.execute(question);
		System.out.println(answer);
		return answer;

	}
}
