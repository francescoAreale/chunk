package com.chunk.ereafra.chunk.Model.Interface;

// i need this class to follow SRP and DIP
public interface VisualizeChunkInterface<T> {

    public void showChunk(T objectToShow);
}
