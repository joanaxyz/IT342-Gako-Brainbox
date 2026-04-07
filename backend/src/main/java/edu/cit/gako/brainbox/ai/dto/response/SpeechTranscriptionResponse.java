package edu.cit.gako.brainbox.ai.dto.response;

public record SpeechTranscriptionResponse(
    String text,
    String model
) {}
