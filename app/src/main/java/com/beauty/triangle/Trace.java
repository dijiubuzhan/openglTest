package com.beauty.triangle;

public class Trace {
    //产品上线时记得改成false
    private final static boolean all = true;
    private final static boolean i = true;
    public static final String TRACE_TAG_VIEW  = "xxx";

    private final static String defaultTag = "MyTag:";

    private Trace() {
    }

    public static void traceBegin(String tag,String msg) {
        if (all && i) {
            android.util.Log.i(defaultTag, msg);
        }
    }

    public static void traceEnd(String tag) {
        if (all && i) {
            android.util.Log.i(defaultTag, tag);
        }
    }


}

