package com.unthinkable.resumescreener.llm;

import com.unthinkable.resumescreener.exception.LlmResponseParseException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonExtractionUtilTest {

    @Test
    void extractsPlainJsonObject() {
        String raw = "{\"score\":8,\"justification\":\"good fit\"}";
        assertThat(JsonExtractionUtil.extractJsonObject(raw)).isEqualTo(raw);
    }

    @Test
    void extractsJsonWrappedInMarkdownCodeFence() {
        String raw = "```json\n{\"score\":7,\"justification\":\"ok\"}\n```";
        assertThat(JsonExtractionUtil.extractJsonObject(raw))
                .isEqualTo("{\"score\":7,\"justification\":\"ok\"}");
    }

    @Test
    void extractsJsonWithLeadingAndTrailingCommentary() {
        String raw = "Sure, here is the result:\n{\"score\":9,\"justification\":\"strong match\"}\nLet me know if you need more.";
        assertThat(JsonExtractionUtil.extractJsonObject(raw))
                .isEqualTo("{\"score\":9,\"justification\":\"strong match\"}");
    }

    @Test
    void handlesNestedObjectsAndBracesInsideStrings() {
        String raw = "{\"skills\":[\"C++\"],\"note\":\"uses { and } in text\",\"nested\":{\"a\":1}}";
        assertThat(JsonExtractionUtil.extractJsonObject(raw)).isEqualTo(raw);
    }

    @Test
    void throwsOnEmptyResponse() {
        assertThatThrownBy(() -> JsonExtractionUtil.extractJsonObject(""))
                .isInstanceOf(LlmResponseParseException.class);
    }

    @Test
    void throwsWhenNoJsonObjectPresent() {
        assertThatThrownBy(() -> JsonExtractionUtil.extractJsonObject("I could not process this request."))
                .isInstanceOf(LlmResponseParseException.class);
    }

    @Test
    void throwsOnUnterminatedJsonObject() {
        assertThatThrownBy(() -> JsonExtractionUtil.extractJsonObject("{\"score\": 5, \"justification\": \"cut off"))
                .isInstanceOf(LlmResponseParseException.class);
    }
}
