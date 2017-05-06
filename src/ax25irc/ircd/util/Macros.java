package ax25irc.ircd.util;

public class Macros
{

    public static void LOG(String msg, Object... args)
    {
        System.out.println("LOG: " + String.format(msg, args));
    }

    public static void ERR(String msg, Object... args)
    {
        System.out.println("ERR: " + String.format(msg, args));
    }

}
