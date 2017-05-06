package ax25irc.ircd.server;

import ax25irc.ircd.util.Macros;

import java.util.List;

public class ServerInfo
{

    private String m_name;
    private int    m_hopcount;
    private String m_info;

    public ServerInfo(String name, String hopcount, String info)
    {
        m_name = name;

        try
        {
            m_hopcount = Integer.parseInt(hopcount);
        } catch (NumberFormatException e)
        {
            Macros.ERR("Failed to format the hopcount number that other connected server published, set it to 0.");

            m_hopcount = 0;
        }

        m_info = info;
    }

    public ServerInfo(List<String> parameters)
    {
        if (parameters.size() >= 1)
        {
            m_name = parameters.get(0);
        }
        else
        {
            m_name = "";
        }

        if (parameters.size() >= 2)
        {
            try
            {
                m_hopcount = Integer.parseInt(parameters.get(1));
            } catch (NumberFormatException e)
            {
                Macros.ERR("Failed to format the hopcount number that other connected server published, set it to 0.");

                m_hopcount = 0;
            }
        }
        else
        {
            m_hopcount = 0;
        }

        if (parameters.size() >= 3)
        {
            m_info = parameters.get(2);
        }
        else
        {
            m_info = "";
        }
    }

    @Override
    public String toString()
    {
        return m_name + " " + m_hopcount + " :" + m_info;
    }

    public String getName()
    {
        return m_name;
    }

    public int getHopCount()
    {
        return m_hopcount;
    }

    public String getInfo()
    {
        return m_info;
    }

}
