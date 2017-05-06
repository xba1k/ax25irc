package ax25irc.ircd.server;

public class CMDs
{

    /* Replies */
    public static final String RPL_LUSERCLIENT      = "251";
    public static final String RPL_LUSEROP          = "252";
    public static final String RPL_LUSERUNKNOWN     = "253";
    public static final String RPL_LUSERCHANNELS    = "254";
    public static final String RPL_LUSERME          = "255";
    public static final String RPL_NONE             = "300";
    public static final String RPL_USERHOST         = "302";
    public static final String RPL_ISON             = "303";
    public static final String RPL_AWAY             = "301";
    public static final String RPL_UNAWAY           = "305";
    public static final String RPL_NOAWAY           = "306";
    public static final String RPL_WHOISUSER        = "311";
    public static final String RPL_WHOISSERVER      = "312";
    public static final String RPL_WHOISOPERATOR    = "313";
    public static final String RPL_WHOISIDLE        = "317";
    public static final String RPL_ENDOFWHOIS       = "318";
    public static final String RPL_WHOISCHANNELS    = "319";
    public static final String RPL_WHOWASUSER       = "314";
    public static final String RPL_ENDOFWHOAWAS     = "369";
    public static final String RPL_LISTSTATRT       = "321";
    public static final String RPL_LIST             = "322";
    public static final String RPL_LISTEND          = "323";
    public static final String RPL_CHANNELMODEIS    = "324";
    public static final String RPL_NOTOPIC          = "331";
    public static final String RPL_TOPIC            = "332";
    public static final String RPL_INVITING         = "341";
    public static final String RPL_SUMMONING        = "342";
    public static final String RPL_VERSION          = "351";
    public static final String RPL_WHOREPLY         = "352";
    public static final String RPL_ENDOFWHO         = "315";
    public static final String RPL_NAMREPLY         = "353";
    public static final String RPL_ENDOFNAMES       = "366";
    public static final String RPL_LINKS            = "364";
    public static final String RPL_ENDOFLINKS       = "365";
    public static final String RPL_BANLIST          = "367";
    public static final String RPL_ENDOFBANLIST     = "368";
    public static final String RPL_INFO             = "371";
    public static final String RPL_ENDOFINFO        = "374";
    public static final String RPL_MOTDSTART        = "375";
    public static final String RPL_MOTD             = "372";
    public static final String RPL_ENDOFMOTD        = "376";
    public static final String RPL_YOUREOPER        = "381";
    public static final String RPL_REHASHING        = "382";
    public static final String RPL_TIME             = "391";
    public static final String RPL_USERSSTART       = "392";
    public static final String RPL_USERS            = "393";
    public static final String RPL_ENDOFUSERS       = "394";
    public static final String RPL_NOUSERS          = "395";

    /* Errors */
    public static final String ERR_NOSUCHNICK       = "401";
    public static final String ERR_NOSUCHSERVER     = "402";
    public static final String ERR_NOSUCHCHANNEL    = "403";
    public static final String ERR_NEEDMOREPARAMS   = "461";
    public static final String ERR_USERONCHANNEL    = "443";
    public static final String ERR_NOTONCHANNEL     = "442";
    public static final String ERR_USERNOTINCHANNEL = "441";
    public static final String ERR_NICKNAMEINUSE    = "433";
    public static final String ERR_NICKCOLLISION    = "436";
    public static final String ERR_CANNOTSENDTOCHAN = "404";

}
