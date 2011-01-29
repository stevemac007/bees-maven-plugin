package com.staxnet.mojo.tomcat;

public class StringHelper {
    static String join( String[] array, String delim ) {
        String j = "";
        for ( int i=0; i<array.length; i++ ) {
            if (i!=0) j += delim;
            j += array[i];
        }
        return j;
    }
}
