package com.example.cardiocheck;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_USER = 1;
    private static final int VIEW_AI = 2;

    private final List<ChatMessage> data = new ArrayList<>();

    public void addMessage(ChatMessage m) {
        data.add(m);
        notifyItemInserted(data.size() - 1);
    }

    public void replaceLastTyping(String text) {
        for (int i = data.size() - 1; i >= 0; i--) {
            ChatMessage m = data.get(i);
            if (m.getType() == ChatMessage.Type.AI && "Escribiendo...".equals(m.getText())) {
                data.set(i, new ChatMessage(ChatMessage.Type.AI, text));
                notifyItemChanged(i);
                return;
            }
        }
        addMessage(new ChatMessage(ChatMessage.Type.AI, text));
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).getType() == ChatMessage.Type.USER ? VIEW_USER : VIEW_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_USER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
            return new UserVH(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_ai, parent, false);
            return new AIVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage m = data.get(position);
        if (holder instanceof UserVH) ((UserVH) holder).tv.setText(m.getText());
        else ((AIVH) holder).tv.setText(m.getText());
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class UserVH extends RecyclerView.ViewHolder {
        TextView tv;
        UserVH(@NonNull View itemView) { super(itemView); tv = itemView.findViewById(R.id.tvMessage); }
    }
    static class AIVH extends RecyclerView.ViewHolder {
        TextView tv;
        AIVH(@NonNull View itemView) { super(itemView); tv = itemView.findViewById(R.id.tvMessage); }
    }
}

