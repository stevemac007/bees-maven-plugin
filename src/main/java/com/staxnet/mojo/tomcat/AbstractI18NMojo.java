package com.staxnet.mojo.tomcat;

//Based on org.codehaus.mojo.tomcat.AbstractI18NMojo (Copyright 2006 Mark Hobson), which was licensed
//under the Apache License, Version 2.0. You may obtain a copy of the License at
//   http://www.apache.org/licenses/LICENSE-2.0

import org.apache.maven.plugin.AbstractMojo;

/**
 * Abstract goal that provides i18n support.
 * 
 */
public abstract class AbstractI18NMojo
    extends AbstractMojo
{
    // ----------------------------------------------------------------------
    // Fields
    // ----------------------------------------------------------------------

    /**
     * The plugin messages.
     */
    private RB rb = new RB(getClass());

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates a new <code>AbstractI18NMojo</code>.
     */
    public AbstractI18NMojo()
    {
    }

    // ----------------------------------------------------------------------
    // Protected Methods
    // ----------------------------------------------------------------------

    /**
     * Gets the message for the given key from this packages resource bundle.
     * 
     * @param key the key for the required message
     * @return the message
     */
    protected String getMessage( String key )
    {
        return rb.getMessage(key);
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
        return rb.getMessage(key, param);
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
        return rb.getMessage(key, param1, param2);
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
        return rb.getMessage(key, params);
    }
}
