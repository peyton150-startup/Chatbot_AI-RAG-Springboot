package com.harmony.chatbot.rag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Page {
    private String id;
    private String source;  // matches JSON "source"
    private String text;    // matches JSON "text"
    private String slug;    // optional
    private String title;   // optional
    private double[] embedding; // optional for dynamic embeddings

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double[] getEmbedding() { return embedding; }
    public void setEmbedding(double[] embedding) { this.embedding = embedding; }
}
