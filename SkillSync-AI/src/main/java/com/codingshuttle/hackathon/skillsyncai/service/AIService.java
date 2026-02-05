package com.codingshuttle.hackathon.skillsyncai.service;

import com.codingshuttle.hackathon.skillsyncai.dto.ParsedResumeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    private final ChatClient.Builder chatClientBuilder;
    private final EmbeddingModel embeddingModel;

    /**
     * Generates embedding vector for a given text.
     */
    public List<Double> generateEmbedding(String text) {
        log.debug("Generating embedding for text length: {}", text.length());
        float[] floatEmbedding = embeddingModel.embed(text);
        List<Double> embedding = new ArrayList<>(floatEmbedding.length);
        for (float f : floatEmbedding) {
            embedding.add((double) f);
        }
        return embedding;
    }

    /**
     * Parses a resume file (PDF/Doc) and extracts structured data.
     */
    public ParsedResumeDTO parseResume(Resource resumeFile) {
        log.info("Starting resume parsing for file: {}", resumeFile.getFilename());
        // 1. Extract Text using Tika
        TikaDocumentReader reader = new TikaDocumentReader(resumeFile);
        List<Document> documents = reader.get();
        String content = documents.stream()
                .map(Document::getText)
                .reduce("", String::concat);

        log.debug("Extracted text content from resume (length: {})", content.length());

        // 2. Query LLM for structured extraction
        ChatClient chatClient = chatClientBuilder.build();

        log.info("Sending resume content to LLM for extraction");

        return chatClient.prompt()
                .user(u -> u.text("Extract the following details from the resume content below:\n" +
                        "1. Full Name\n" +
                        "2. Email\n" +
                        "3. Skills (as a list)\n" +
                        "4. Years of Experience (integer estimation)\n" +
                        "5. Education (brief summary)\n" +
                        "6. Professional Summary\n\n" +
                        "Resume Content:\n{content}")
                        .param("content", content))
                .call()
                .entity(ParsedResumeDTO.class);
    }
}
