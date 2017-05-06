package ax25irc.ircd.util;

import java.util.HashMap;
import java.util.Map;

public class VarMap
{

    private Map<String, String>  m_strings;
    private Map<String, Integer> m_integers;
    private Map<String, Float>   m_floats;

    public VarMap()
    {
        m_strings = new HashMap<String, String>();
        m_integers = new HashMap<String, Integer>();
        m_floats = new HashMap<String, Float>();
    }

    public void putString(String key, String s)
    {
        m_strings.put(key, s);
    }

    public void putInteger(String key, int i)
    {
        m_integers.put(key, i);
    }

    public void putFloat(String key, float f)
    {
        m_floats.put(key, f);
    }

    public String getString(String key)
    {
        String result = m_strings.get(key);

        if (result == null)
        {
            result = "";
        }

        return result;
    }

    public int getInteger(String key)
    {
        Integer result = m_integers.get(key);

        if (result == null)
        {
            result = -1;
        }

        return result;
    }

    public float getFloat(String key)
    {
        Float result = m_floats.get(key);

        if (result == null)
        {
            result = -1.0f;
        }

        return result;
    }

}
