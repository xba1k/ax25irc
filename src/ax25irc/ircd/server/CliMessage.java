package ax25irc.ircd.server;

import java.util.ArrayList;
import java.util.List;

public class CliMessage
{

    private String       m_message;
    private String       m_prefix;
    private String       m_command;
    private List<String> m_parameters;

    public CliMessage(String message)
    {
        m_message = message;
        m_prefix = "";
        m_command = "";
        m_parameters = new ArrayList<String>();

        String[] message_split_s = m_message.split("\\s+");

        /* Detect if there's prefix or not */
        if (m_message.startsWith(":"))
        {
            m_prefix = message_split_s[0];
            m_command = message_split_s[1];

            String parameters = "";

            for (int i = 2; i < message_split_s.length; i++)
            {
                parameters += ((i == 2) ? "" : " ") + message_split_s[i];
            }

            parseParameters(parameters);
        }
        else
        {
            m_prefix = "";
            m_command = message_split_s[0];

            String parameters = "";

            for (int i = 1; i < message_split_s.length; i++)
            {
                parameters += ((i == 1) ? "" : " ") + message_split_s[i];
            }

            parseParameters(parameters);
        }
    }

    private void parseParameters(String parameters)
    {
        /* Detect what kind of parameters we are receiving */
        String[] params_split_s = parameters.split("\\s+");
        String[] params_split_c = parameters.split("\\s*,\\s*");

        /* Parameters separated by , for some commands */
        if ((m_command.equalsIgnoreCase("JOIN") || m_command.equalsIgnoreCase("PART")) && params_split_c.length > 1)
        {
            for (String s : params_split_c)
            {
                m_parameters.add(s.trim());
            }
        }
        /* Parameters separated by SPACE */
        else
        {
            List<List<String>> params_combined = new ArrayList<List<String>>();
            int param_separator = 0;

            for (String s : params_split_s)
            {
                if (s.startsWith(":"))
                {
                    params_combined.add(new ArrayList<String>());
                    param_separator++;
                }

                if (params_combined.size() == 0 && !s.startsWith(":"))
                {
                    m_parameters.add(s);
                }
                else
                {
                    params_combined.get(param_separator - 1).add(s);
                }
            }

            for (List<String> l : params_combined)
            {
                String parameter = "";

                for (String p : l)
                {
                    parameter += p + " ";
                }

                parameter = parameter.replace(":", "");

                if (!parameter.trim().isEmpty())
                {
                    m_parameters.add(parameter.trim());
                }
            }
        }
    }

    public String getMessage()
    {
        return m_message;
    }

    public String getPrefix()
    {
        return m_prefix;
    }

    public String getCommand()
    {
        return m_command;
    }

    public List<String> getParameters()
    {
        return m_parameters;
    }

    public String getParameter(int i)
    {
        if (i < 0 || i >= m_parameters.size())
        {
            return "";
        }

        return m_parameters.get(i);
    }

}
