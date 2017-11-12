/**
 * Project:     BooksExplorer
 * Date:        11/9/2017
 * Description: String utilities
 */
package com.exercise.booksexplorer.util;

import java.util.List;

/**
 * Created by alek on 11/12/17.
 */

public class StringUtils {
    /**
     * Concatenates string list with comma separator
     *
     * @param strArr a list of strings
     * @return String
     */
    public static String concat(List<String> strArr) {
        if (strArr == null)
            return "";
        StringBuilder sb = new StringBuilder();
        for (String category: strArr) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(category);
        }
        return sb.toString();
    }

}
