package com.dell.nfclib;

public class bookDetails
{
    String bookId, bookAuthor, bookNFC, bookQuantity, bookName;
    String bookCoverURL;

    public bookDetails()
    {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public String getBookName()
    {
        return bookName;
    }

    public void setBookName(String bookName)
    {
        this.bookName = bookName;
    }

    public String getBookId()
    {
        return bookId;
    }

    public void setBookId(String bookId)
    {
        this.bookId = bookId;
    }

    public String getBookAuthor()
    {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor)
    {
        this.bookAuthor = bookAuthor;
    }

    public String getBookNFC()
    {
        return bookNFC;
    }

    public void setBookNFC(String bookNFC)
    {
        this.bookNFC = bookNFC;
    }

    public String getBookQuantity()
    {
        return bookQuantity;
    }

    public void setBookQuantity(String bookQuantity)
    {
        this.bookQuantity = bookQuantity;
    }

    public String getBookCoverURL() {
        return bookCoverURL;
    }

    public void setBookCoverURL(String bookCoverURL) {
        this.bookCoverURL = bookCoverURL;
    }
}
