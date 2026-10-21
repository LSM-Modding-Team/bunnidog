package com.bunnidogs.mod.genetics;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/** Pure, scoped presentation translation. English message IDs never enter saved genes, records or item data. */
public final class Texts {
    private Texts() {}
    private static final ConcurrentHashMap<String,String> KEYS=new ConcurrentHashMap<>();
    private static final ThreadLocal<BiFunction<String,String,String>> TRANSLATOR=new ThreadLocal<>();
    /** Stable gettext-style IDs let shared pure readouts keep an English fallback without depending on Minecraft. */
    public static String key(String english) {
        return KEYS.computeIfAbsent(english,message->{
            try {
                return "text.bunnidogs."+HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                        .digest(message.getBytes(StandardCharsets.UTF_8))).substring(0,16);
            } catch (NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
        });
    }
    private static final Pattern FORMAT = Pattern.compile("%(?:(\\d+)\\$)?([+\\-0#,(]*\\d*(?:\\.\\d+)?)([sdf])|%%");
    /** Minecraft changes numeric conversions to %s on load. Recover their precision from the English template. */
    private static String restoreFormats(String english,String translated) {
        var formats=new ArrayList<String>();
        var source=FORMAT.matcher(english);
        while (source.find()) if (!source.group().equals("%%")) formats.add(source.group(2)+source.group(3));
        if (formats.isEmpty()) return translated;
        var target=FORMAT.matcher(translated); var result=new StringBuilder(); int next=0;
        while (target.find()) {
            if (target.group().equals("%%")) { target.appendReplacement(result,"%%"); continue; }
            int slot=target.group(1)==null ? next++ : Integer.parseInt(target.group(1))-1;
            String replacement=slot>=0 && slot<formats.size() ? "%"+(target.group(1)==null ? "" : target.group(1)+"$")+formats.get(slot) : target.group();
            target.appendReplacement(result,java.util.regex.Matcher.quoteReplacement(replacement));
        }
        return target.appendTail(result).toString();
    }
    public static String t(String english,Object... args) {
        if (english.isEmpty()) return english;
        return named(key(english),english,args);
    }
    public static String named(String key,String english,Object... args) {
        var translator=TRANSLATOR.get();
        String pattern=translator==null ? english : restoreFormats(english,translator.apply(key,english));
        return args.length==0 ? pattern : String.format(Locale.ROOT,pattern,args);
    }
    public static <T> T using(BiFunction<String,String,String> translator,Supplier<T> work) {
        var previous=TRANSLATOR.get(); TRANSLATOR.set(translator);
        try { return work.get(); }
        finally { if (previous==null) TRANSLATOR.remove(); else TRANSLATOR.set(previous); }
    }
}
