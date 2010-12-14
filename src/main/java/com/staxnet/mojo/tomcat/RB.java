package com.staxnet.mojo.tomcat;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class RB {
	/**
     * The plugin messages.
     */
    private ResourceBundle messages;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates a new <code>AbstractI18NMojo</code>.
     */
    public RB(Class<?> cls)
    {
        String packageName = cls.getPackage().getName();

        messages = ResourceBundle.getBundle( packageName + ".messages" );
    }
    
	/**
     * Gets the message for the given key from this packages resource bundle.
     * 
     * @param key the key for the required message
     * @return the message
     */
    protected String getMessage( String key )
    {
        try
        {
            return messages.getString( key );
        }
        catch ( NullPointerException exception )
        {
            return "???" + key + "???";
        }
        catch ( MissingResourceException exception )
        {
            return "???" + key + "???";
        }
        catch ( ClassCastException exception )
        {
            return "???" + key + "???";
        }
    }

    /**
     * Gets the message for the given key from this packages resource bundle
     * and formats it with the given parameter.
     * 
     * @param key the key for the required message
     * @param param the parameter to be used to format the message with
     * @return the formatted message
     */
    protected String getMessage( String key, Object param )
    {
        return MessageFormat.format( getMessage( key ), new Object[] { param } );
    }

    /**
     * Gets the message for the given key from this packages resource bundle
     * and formats it with the given parameters.
     * 
     * @param key the key for the required message
     * @param param1 the first parameter to be used to format the message with
     * @param param2 the second parameter to be used to format the message with
     * @return the formatted message
     */
    protected String getMessage( String key, Object param1, Object param2 )
    {
        return MessageFormat.format( getMessage( key ), new Object[] { param1, param2 } );
    }

    /**
     * Gets the message for the given key from this packages resource bundle
     * and formats it with the given parameters.
     * 
     * @param key the key for the required message
     * @param params the parameters to be used to format the message with
     * @return the formatted message
     */
    protected String getMessage( String key, Object[] params )
    {
        return MessageFormat.format( getMessage( key ), params );
    }
}
