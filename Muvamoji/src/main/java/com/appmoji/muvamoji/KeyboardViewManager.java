package com.appmoji.muvamoji;

import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.view.View;

import com.appmoji.muvamoji.memeicon.Memeicon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kou on 14/10/29.
 */
public class KeyboardViewManager {
    private final int PAGE_COUNT = 3;
    private final MuvamojiKeyboardView[] views = new MuvamojiKeyboardView[PAGE_COUNT];
    private final MuvamojiKeyboard[] keyboards = new MuvamojiKeyboard[PAGE_COUNT];
    private final ArrayList<List<Memeicon>> pages = new ArrayList<List<Memeicon>>();
    private final Context context;

    private int currentView;
    private int currentPage;


    /**
     * Construct object.
     * @param context
     * @param onKeyboardActionListener
     * @param onTouchListener
     */
    public KeyboardViewManager(Context context, KeyboardView.OnKeyboardActionListener onKeyboardActionListener, View.OnTouchListener onTouchListener)
    {
        this.context = context;

        for(int i = 0;  i < PAGE_COUNT;  ++i)
        {
            views[i] = new MuvamojiKeyboardView(context, null, R.attr.keyboardViewStyle);
            views[i].setOnTouchListener(onTouchListener);
            views[i].setOnKeyboardActionListener(onKeyboardActionListener);
            views[i].setPreviewEnabled(false);
            keyboards[i] = MuvamojiKeyboard.create(context);
            views[i].setKeyboard(keyboards[i]);
        }

        currentView = 0;
    }

    /**
     * Initialize manager.
     * @param emojies   Emoji of regist to manager.
     */
    public void initialize(List<Memeicon> emojies)
    {
        initialize(emojies, 0);
    }

    /**
     * Initialize manager.
     * @param emojies       Emoji of regist to manager.
     * @param defaultPage   Default page number.
     */
    public void initialize(List<Memeicon> emojies, int defaultPage)
    {
        pages.clear();

        // Create pages.
        final int keyCountMax = keyboards[0].getKeyCountMax();
        final int emojiCount = emojies == null ? 0 : emojies.size();
        for(int i = 0;  i < emojiCount;  i += keyCountMax)
        {
            pages.add(new ArrayList<Memeicon>(
                    emojies.subList(i, Math.min(i + keyCountMax, emojiCount))
            ));
        }

        // Add dummy if pages is empty.
        if(pages.isEmpty())
            pages.add(new ArrayList<Memeicon>());

        // Set default page.
        currentPage = defaultPage % pages.size();

        // Apply page to view.
        initializePage(currentView, currentPage);
    }

    /**
     * Initialize manager.
     * @param emojies       Emoji of regist to manager.
     * @param defaultPage   Default page number.
     */
    public void initializeForGif(List<Memeicon> emojies, int defaultPage)
    {
        pages.clear();

        // Create pages.
//        final int keyCountMax = 2;
        final int keyCountMax = keyboards[0].getKeyCountMax();
        final int emojiCount = emojies == null ? 0 : emojies.size();
        for(int i = 0;  i < emojiCount;  i += keyCountMax)
        {
            pages.add(new ArrayList<Memeicon>(
                    emojies.subList(i, Math.min(i + keyCountMax, emojiCount))
            ));
        }

        // Add dummy if pages is empty.
        if(pages.isEmpty())
            pages.add(new ArrayList<Memeicon>());

        // Set default page.
        currentPage = defaultPage % pages.size();

        // Apply page to view.
        initializePage(currentView, currentPage);
    }

    /**
     * Initialize manager from emoji name.
     * @param emojiNames    Emoji name of regist to manager.
     */
    public void initializeFromName(List<String> emojiNames)
    {
        initializeFromName(emojiNames, 0);
    }

    /**
     * Initialize manager from emoji name.
     * @param emojiNames    Emoji name of regist to manager.
     * @param defaultPage   Default page number.
     */
    public void initializeFromName(List<String> emojiNames, int defaultPage)
    {
//        final Memeicon memeicon = Memeicon.getInstance();
//        final ArrayList<Memeicon> emojies = new ArrayList<Memeicon>(emojiNames.size());
//        for(String emojiName : emojiNames)
//            emojies.add(emojidex.getEmoji(emojiName));
//        initialize(emojies, defaultPage);
    }

    /**
     * Change view to next.
     */
    public boolean next()
    {
        if ((currentPage + 1) % pages.size() == 0) {
            return false;
        }

        currentView = (currentView + 1) % PAGE_COUNT;
        currentPage = (currentPage + 1) % pages.size();

        initializePage(currentView, currentPage);
        return true;
    }

    /**
     * Change view to prev.
     */
    public boolean prev()
    {
        if (currentPage == 0) {
            return false;
        }
        currentView = (currentView + PAGE_COUNT - 1) % PAGE_COUNT;
        currentPage = (currentPage + pages.size() - 1) % pages.size();

        initializePage(currentView, currentPage);
        return true;
    }

    /**
     * Change view to specified page.
     * @param page  page number.
     */
    public void setPage(int page)
    {
        currentView = (currentView + 1) % PAGE_COUNT;
        currentPage = page % pages.size();

        initializePage(currentView, currentPage);
    }

    /**
     * Change view to specified emoji name.
     * @param emojiName     Emoji name.
     */
    public void setPage(String emojiName)
    {
//        final int count = pages.size();
//        for(int i = 0;  i < count;  ++i)
//        {
//            final List<Memeicon> page = pages.get(i);
//            for(Memeicon emoji : page)
//            {
//                if( emoji.name.equals(emojiName) )
//                {
//                    setPage(i);
//                    return;
//                }
//            }
//        }

        // If not found.
        setPage(0);
    }

    /**
     * Get current view.
     * @return  Current view.
     */
    public MuvamojiKeyboardView getCurrentView()
    {
        return views[currentView];
    }

    /**
     * Get next view.
     * @return  Next view.
     */
    public MuvamojiKeyboardView getNextView()
    {
        return views[(currentView + 1) % PAGE_COUNT];
    }

    /**
     * Get prev view.
     * @return  Prev view.
     */
    public MuvamojiKeyboardView getPrevView()
    {
        return views[(currentView + PAGE_COUNT - 1) % PAGE_COUNT];
    }

    /**
     * Get view array.
     * @return  View array.
     */
    public MuvamojiKeyboardView[] getViews()
    {
        return views;
    }

    /**
     * Get current page number.
     * @return  Current page number.
     */
    public int getCurrentPage()
    {
        return currentPage;
    }

    /**
     * Initialize page.
     * @param destIndex
     * @param pageIndex
     */
    private void initializePage(int destIndex, int pageIndex)
    {
        final List<Memeicon> page = pages.get(pageIndex);
        if(keyboards[destIndex].getKeys().size() != page.size())
            keyboards[destIndex] = MuvamojiKeyboard.create(context);
        keyboards[destIndex].initialize(page);
        views[destIndex].setKeyboard(keyboards[destIndex]);
    }
}
