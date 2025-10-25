package com.example.cardiocheck;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AIChatActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private ChatAdapter adapter;
    private EditText etMessage;
    private OpenAIClient aiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);
        aiClient = new OpenAIClient(this);

        recycler = findViewById(R.id.recyclerChat);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter();
        recycler.setAdapter(adapter);

        etMessage = findViewById(R.id.etMessage);
        ImageButton btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String q = etMessage.getText().toString().trim();
                if (TextUtils.isEmpty(q)) return;
                etMessage.setText("");
                adapter.addMessage(new ChatMessage(ChatMessage.Type.USER, q));
                adapter.addMessage(new ChatMessage(ChatMessage.Type.AI, "Escribiendo..."));
                recycler.scrollToPosition(adapter.getItemCount() - 1);
                new Thread(() -> {
                    final String resp = aiClient.getChatResponse(q);
                    runOnUiThread(() -> {
                        adapter.replaceLastTyping(resp);
                        recycler.scrollToPosition(adapter.getItemCount() - 1);
                    });
                }).start();
            }
        });
    }
}
