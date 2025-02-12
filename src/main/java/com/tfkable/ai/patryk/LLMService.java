package com.tfkable.ai.patryk;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Service;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;


@Service
public class LLMService {
	private final FileContentReader fileContentReader;
	private final ChatLanguageModel chatModel;
	private final ChatMemory chatMemory;
	private final ConversationalRetrievalChain retrievalChain;
	
	private final String modelUrl = "http://localhost:11434";
	private final String modelName = "mistral";
	
	public LLMService(FileContentReader fileContentReader) throws IOException{
		this.fileContentReader = fileContentReader;
		
		this.chatModel = OllamaChatModel.builder()
				.timeout(Duration.ofMinutes(1L))
				.baseUrl(modelUrl)
				.modelName(modelName)
				.build();
		
		this.chatMemory = MessageWindowChatMemory.builder()
				.id("my-chat-id") // Opcjonalne identyfikator pamięci
				.maxMessages(50) // Maksymalna liczba wiadomości w oknie
				.chatMemoryStore(new InMemoryChatMemoryStore()) // Domyślna implementacja przechowywania
				.build();
		
		EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
		AllMiniLmL6V2EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
		
		EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
				.documentSplitter(DocumentSplitters.recursive(500, 0))
				.embeddingModel(embeddingModel)
				.embeddingStore(embeddingStore)
				.build();
		
		List<Document> documents = fileContentReader.readFilesFromDirectory();
		ingestor.ingest(documents);
		
		this.retrievalChain = ConversationalRetrievalChain.builder()
				.chatMemory(chatMemory)
				.chatLanguageModel(chatModel)
				.retriever(EmbeddingStoreRetriever.from(embeddingStore, embeddingModel))
				.build();
		
	}
	
	public String sendMessageToLLM(String question) {
		return retrievalChain.execute(question);
	}
}
