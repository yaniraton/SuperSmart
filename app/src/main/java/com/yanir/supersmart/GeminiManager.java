package com.yanir.supersmart;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.ImagePart;
import com.google.ai.client.generativeai.type.Part;
import com.google.ai.client.generativeai.type.TextPart;

import java.util.ArrayList;
import java.util.List;

import kotlin.Result;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

/**
 * The {@code GeminiManager} class provides a simplified interface for interacting with the Gemini AI model.
 * It handles the initialization of the {@link GenerativeModel} and provides methods for sending text prompts
 * and prompts with images to the model.
 */
public class GeminiManager {
    private static GeminiManager instance;
    private GenerativeModel gemini;
    private final String TAG = "GeminiManager";

    /**
     * Private constructor to enforce the Singleton pattern.
     * Initializes the {@link GenerativeModel} with the specified model name and API key.
     */
    private GeminiManager() {
        gemini = new GenerativeModel(
                "gemini-2.0-flash",
                BuildConfig.Gemini_API_Key
        );
        Log.i(TAG, "GeminiManager initialized with model: " + gemini.getModelName()+
                " and API Key: " + gemini.getApiKey());
    }

    /**
     * Returns the singleton instance of {@code GeminiManager}.
     *
     * @return The singleton instance of {@code GeminiManager}.
     */
    public static GeminiManager getInstance() {
        if (instance == null) {
            instance = new GeminiManager();
        }
        return instance;
    }

    /**
     * Sends a text prompt along with a photo to the Gemini model and receives a text response.
     *
     * @param prompt   The text prompt to send to the model.
     * @param photo    The photo to send to the model.
     * @param callback The callback to receive the response or error.
     */
    public void sendTextWithPhotoPrompt(String prompt, Bitmap photo, GeminiCallback callback) {
        List<Part> parts = new ArrayList<>();
        parts.add(new TextPart(prompt));
        parts.add(new ImagePart(photo));

        Content[] content = new Content[1];
        content[0] = new Content(parts);

        gemini.generateContent(content,
                new Continuation<GenerateContentResponse>() {
            @NonNull
            @Override
            public CoroutineContext getContext() {
                return EmptyCoroutineContext.INSTANCE;
            }

            @Override
            public void resumeWith(@NonNull Object result) {
                if (result instanceof Result.Failure) {
                    Log.i(TAG, "Error: " + ((Result.Failure) result).exception.getMessage());
                    callback.onFailure(((Result.Failure) result).exception);
                } else {
                    Log.i(TAG, "Response: " + ((GenerateContentResponse) result).getText());
                    callback.onSuccess(((GenerateContentResponse) result).getText());
                }
            }
        });
    }

}
