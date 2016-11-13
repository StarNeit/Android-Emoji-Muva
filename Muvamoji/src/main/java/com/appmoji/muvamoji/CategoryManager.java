package com.appmoji.muvamoji;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by kou on 15/01/13.
 */
public class CategoryManager {
    private static final CategoryManager INSTANCE = new CategoryManager();

    private final ArrayList<CategoryParam> params = new ArrayList<CategoryParam>();

    /**
     * Category parameter.
     */
    private static class CategoryParam
    {
        public final String id;
        public final String image;

        private CategoryParam(String id, String img)
        {
            this.id = id;
            this.image = img;
        }
    }

    /**
     * Get singleton instance.
     * @return  Singleton instance.
     */
    public static CategoryManager getInstance()
    {
        return INSTANCE;
    }

    /**
     * Initialize object.
     * @param context   Context.
     */
    public void initialize(Context context)
    {
        context = context.getApplicationContext();

        // Declare initialize parameter table.
        final String[][] initParams = {
                { "a"            , "a1"},
                { "b"            , "b1"},
                { "c"            , "c1"},
                { "d"            , "d1"},
                { "e"            , "e1"},
                { "f"            , "f1"},
                { "g"            , "g1"},
                { "h"            , "h1"},
                { "i"            , "i1"},
                { "j"            , "j1"},
                { "k"            , "k1"},
                { "l"            , "l1"},
                { "m"            , "m1"},
                { "ga"         , "ga1"}
        };

        // Clear categories.
        params.clear();

        // Add categories.
        for(String[] initParam : initParams)
        {
            add(
                    initParam[0],
                    initParam[1]
            );
        }
    }

    /**
     * Add category.
     * @param id        Category ID.
     * @param text      Category text.
     */
    public void add(String id, String text)
    {
        // Skip if always have id.
        for(CategoryParam param : params)
        {
            if(param.id.equals(id))
                return;
        }

        // Add category.
        params.add(new CategoryParam(id, text));
    }

    /**
     * Get category count.
     * @return  Category count.
     */
    public int getCategoryCount()
    {
        return params.size();
    }

    /**
     * Get category ID.
     * @param index     Index.
     * @return          Category ID.
     */
    public String getCategoryId(int index)
    {
        return params.get(index).id;
    }

    /**
     * Get category text.
     * @param index     Index.
     * @return          Category text.
     */
    public String getCategoryText(int index)
    {
        return params.get(index).image;
    }

    /**
     * Construct object.(private)
     */
    private CategoryManager()
    {
        // nop
    }
}
