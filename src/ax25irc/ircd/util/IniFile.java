package ax25irc.ircd.util;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IniFile
{

    private static Pattern                   s_section  = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");
    private static Pattern                   s_keyvalue = Pattern.compile("\\s*([^=]*)=(.*)");

    private String                           m_filepath;
    private List<String>                     m_lines;
    private Map<String, Map<String, String>> m_entries;

    public IniFile(String filepath)
    {
        m_filepath = filepath;
        m_lines = FileUtils.loadTextFile(filepath, true);
        m_entries = new CaseIMap<>();

        if (m_lines != null && !m_lines.isEmpty())
        {
            Matcher matcher;
            String section = null;

            for (String l : m_lines)
            {
                matcher = s_section.matcher(l);

                if (matcher.matches())
                {
                    section = l;
                }
                else if (section != null)
                {
                    matcher = s_keyvalue.matcher(l);

                    if (matcher.matches())
                    {
                        String key = matcher.group(1).trim();
                        String value = matcher.group(2).replaceAll("\"", "").trim();
                        Map<String, String> values = m_entries.get(section);

                        if (values == null)
                        {
                            m_entries.put(section, values = new CaseIMap<>());
                        }

                        values.put(key, value);
                    }
                }
            }
        }
    }

    public String getFilePath()
    {
        return m_filepath;
    }

    public String getString(String section, String key, String defaultvalue)
    {
        Map<String, String> kv = m_entries.get(section);

        if (kv == null)
        {
            return defaultvalue;
        }

        return kv.get(key);
    }

    public int getInt(String section, String key, int defaultvalue)
    {
        Map<String, String> kv = m_entries.get(section);

        if (kv == null)
        {
            return defaultvalue;
        }

        return Integer.parseInt(kv.get(key));
    }

    public float getFloat(String section, String key, float defaultvalue)
    {
        Map<String, String> kv = m_entries.get(section);

        if (kv == null)
        {
            return defaultvalue;
        }

        return Float.parseFloat(kv.get(key));
    }

    public double getDouble(String section, String key, double defaultvalue)
    {
        Map<String, String> kv = m_entries.get(section);

        if (kv == null)
        {
            return defaultvalue;
        }

        return Double.parseDouble(kv.get(key));
    }

}