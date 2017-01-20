package com.vivek.codemozo.utils;


import android.text.Html;
import android.text.Spanned;

public class HtmlUtils {

    public static String getLink(String text, String url) {
        return "<b><a href=\"" + url + "\">" + text + "</a></b>";
    }

    public static String getBold(String text) {
        return "<b>" + text + "</b>";
    }

    public static String getItalic(String text) {
        return "<i>" + text + "</i>";
    }

    public static String getUnderline(String text) {
        return "<u>" + text + "</u>";
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

}
