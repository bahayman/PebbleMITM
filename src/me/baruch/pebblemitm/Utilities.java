package me.baruch.pebblemitm;

public class Utilities {

	public static <T> T coalesce(T ...items) {
	    for(T i : items) if(i != null) return i;
	    return null;
	}
}
