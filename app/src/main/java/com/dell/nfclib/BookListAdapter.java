package com.dell.nfclib;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookViewHolder>
{
    List<bookDetails>list;

    public BookListAdapter(List<bookDetails> list)
    {
        this.list = list;
    }


    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.book_display_layout, parent, false);

        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position)
    {
        bookDetails details = list.get(position);
//        String imageUrl = details.bookName.trim().toLowerCase().replace(" ", "-");

        holder.bookAuthor.setText(details.getBookAuthor());
        holder.bookName.setText(details.getBookName());
        holder.bookQuantity.setText(details.getBookQuantity());
        // Making use of the Picasso library...
        Picasso.get().load(details.getBookCoverURL()).into(holder.bookCover);

    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    public class BookViewHolder extends RecyclerView.ViewHolder
    {
        TextView bookAuthor, bookName, bookQuantity;
        ImageView bookCover;
        public BookViewHolder(@NonNull View itemView)
        {
            super(itemView);
            bookAuthor = itemView.findViewById(R.id.bookAuthorTextView);
            bookName = itemView.findViewById(R.id.bookNameTextView);
            bookQuantity = itemView.findViewById(R.id.bookQuantityTextView);
            bookCover = itemView.findViewById(R.id.bookCoverImageView);
        }
    }
}
