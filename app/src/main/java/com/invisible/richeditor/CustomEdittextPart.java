package com.invisible.richeditor;

/**
 * Created by ives.yeung on 2016/11/10.
 */

public class CustomEdittextPart {
    private int start;
    private int end;

    public CustomEdittextPart(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public boolean isValid() {
        return start < end;
    }
}