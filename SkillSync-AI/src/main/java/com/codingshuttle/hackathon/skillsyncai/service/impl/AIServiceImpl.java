package com.codingshuttle.hackathon.skillsyncai.service.impl;

import com.codingshuttle.hackathon.skillsyncai.dto.ParsedResumeDTO;
import com.codingshuttle.hackathon.skillsyncai.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@EnableRetry
public class AIServiceImpl implements AIService {

    private final ChatClient openAiChatClient;
    private final ChatClient ollamaChatClient;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;

    public AIServiceImpl(ChatClient.Builder builder,
            @Qualifier("openAiChatModel") ChatModel openAiChatModel,
            @Qualifier("ollamaChatModel") ChatModel ollamaChatModel,
            @Qualifier("openAiEmbeddingModel") EmbeddingModel embeddingModel,
            VectorStore vectorStore) {
        this.openAiChatClient = ChatClient.create(openAiChatModel);
        this.ollamaChatClient = ChatClient.create(ollamaChatModel);
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
    }

    @Override
    public List<Double> generateEmbedding(String text) {
        log.debug("Generating embedding for text length: {}", text.length());
        float[] floatEmbedding = embeddingModel.embed(text);
        List<Double> embedding = new ArrayList<>(floatEmbedding.length);
        for (float f : floatEmbedding) {
            embedding.add((double) f);
        }
        return embedding;
    }

    @Override
    public void storeResumeEmbedding(Long resumeId, String resumeContent, Map<String, Object> metadata) {
        log.info("Storing resume embedding for resumeId: {}", resumeId);
        metadata.put("resumeId", resumeId.toString());
        metadata.put("docType", "RESUME");
        Document document = new Document(resumeContent, metadata);
        vectorStore.add(List.of(document));
        log.info("Successfully stored resume embedding for resumeId: {}", resumeId);
    }

    @Override
    public List<Document> searchSimilarResumes(String query, int topK) {
        log.info("Searching for similar resumes with query length: {}, topK: {}", query.length(), topK);
        List<Document> results = vectorStore.similaritySearch(query);
        log.info("Found {} similar resumes", results.size());
        return results.stream().limit(topK).toList();
    }

    @Override
    public ParsedResumeDTO parseResume(Resource resumeFile) {
        log.info("Starting resume parsing for file: {}", resumeFile.getFilename());

        TikaDocumentReader reader = new TikaDocumentReader(resumeFile);
        List<Document> documents = reader.get();
        String content = documents.stream()
                .map(Document::getText)
                .reduce("", String::concat);

        log.debug("Extracted text content from resume (length: {})", content.length());

        try {
            return extractWithClient(openAiChatClient, content);
        } catch (Exception e) {
            log.error("OpenAI extraction failed: {}. Falling back to Ollama.", e.getMessage());
            try {
                log.info("Attempting Ollama fallback...");
                return extractWithClient(ollamaChatClient, content);
            } catch (Exception ex) {
                log.error("Ollama fallback also failed: {}", ex.getMessage());
                throw ex;
            }
        }
    }

    private ParsedResumeDTO extractWithClient(ChatClient client, String content) {
        return client.prompt()
                .user(u -> u.text("""
                        Extract the following details from the resume content below:
                        1. Full Name
                        2. Email
                        3. Skills (as a list)
                        4. Years of Experience (integer estimation)
                        5. Education (brief summary)
                        6. Professional Summary

                        Resume Content:
                        {content}
                        """)
                        .param("content", content))
                .call()
                .entity(ParsedResumeDTO.class);
    }
}
