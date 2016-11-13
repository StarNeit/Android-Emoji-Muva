package com.appmoji.muvamoji.memeicon;

/**
 * Created by tiemin on 3/9/16.
 */
public enum MemeiconFormat {
    PNG(".png", "mojis"),
    GIF(".gif", "gifs"),
    ;

    private final String extension;
    private final String relativeDir;

    /**
     * Convert resolution name to format.
     * @param type    File Type name.
     * @return              Format of resolution.(If resolution is not found, return null.)
     */
    public static MemeiconFormat toFormat(String type)
    {
        for(MemeiconFormat format : MemeiconFormat.values())
            if (type.equals(format.relativeDir))
                return format;
        return null;
    }

    /**
     * Get extension of format.
     * @return  Extension of format.
     */
    String getExtension()
    {
        return extension;
    }

    /**
     * Get relative directory path of format.
     * @return  Relative directory path.
     */
    String getRelativeDir()
    {
        return relativeDir;
    }

    /** Constructor */
    private MemeiconFormat(String extension, String relativeDir) {
        this.extension = extension;
        this.relativeDir = relativeDir;
    }
}
