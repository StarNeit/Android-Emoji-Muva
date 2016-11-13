package com.appmoji.muvamoji.memeicon;

import android.content.Context;
import android.content.res.Resources;

import com.appmoji.muvamoji.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tiemin on 3/9/16.
 */
public class MemeiconManager {
    private final ArrayList<Memeicon> emojies = new ArrayList<Memeicon>();

    private final HashMap<String, Memeicon> emojiTableFromName = new HashMap<String, Memeicon>();
    private final HashMap<List<Integer>, Memeicon> emojiTableFromCodes = new HashMap<List<Integer>, Memeicon>();
    private final HashMap<String, ArrayList<Memeicon>> categorizedEmojies = new HashMap<String, ArrayList<Memeicon>>();

    private int nextOriginalCode;
    private Context context;

    public MemeiconManager(Context context) {
        this.context = context.getApplicationContext();
        reset();
        initialize();
    }

    private void initialize() {
        add("a", 55);
        add("b", 97);
        add("c", 21);
        add("d", 62);
        add("e", 105);
        add("f", 16);
        add("g", 111);
        add("h", 42);
        add("i", 19);
        add("j", 48);
        add("k", 68);
        add("l", 90);
        add("m", 37);
        add("ga", 14);
    }

    private void add(String category, int count) {
        ArrayList<Memeicon> arrayList = new ArrayList<Memeicon>();

        for (int i=0; i < count; i++) {
            int code = nextOriginalCode++;
            Memeicon memeicon = new Memeicon(context.getResources(), category, i+1, code);
            if (category.equals("ga")) {
                memeicon.setIsGif(true);
            }
            emojiTableFromCodes.put(memeicon.getCodes(), memeicon);
            emojiTableFromName.put(memeicon.getFilename(), memeicon);
            arrayList.add(i, memeicon);
        }

        categorizedEmojies.put(category, arrayList);
    }

    public List<Memeicon> getEmojiList(String category) {
        return categorizedEmojies.get(category);
    }

    /**
     * Get emoji from emoji name.
     * @param name  Emoji name.
     * @return      Emoji.(If emoji is not found, return null.)
     */
    public Memeicon getEmoji(String name)
    {
        return emojiTableFromName.get(name);
    }
    /**
     * Get emoji from emoji codes.
     * @param codes Emoji codes.
     * @return      Emoji.(If emoji is not found, return null.)
     */
    public Memeicon getEmoji(List<Integer> codes)
    {
        return emojiTableFromCodes.get(codes);
    }

    /**
     * Reset manager.
     */
    private void reset()
    {
        emojies.clear();
        emojiTableFromCodes.clear();
        categorizedEmojies.clear();

        nextOriginalCode = context.getResources().getInteger(R.integer.default_code_start);
    }
}
