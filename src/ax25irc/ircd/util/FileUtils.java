package ax25irc.ircd.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FileUtils
{

    public static List<String> loadTextFile(String filepath, boolean trimws)
    {
        InputStream input;
        BufferedReader reader;
        List<String> result = new ArrayList<String>();

        try
        {
            input = FileUtils.class.getClassLoader().getResourceAsStream(filepath);

            if (input == null)
            {
                Macros.ERR("Input file <%s> either doesn't exist or is a directory.", filepath);
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(input));
            String line;

            while ((line = reader.readLine()) != null)
            {
                line = trimws ? line.trim() : line;

                if (line.isEmpty() && trimws)
                    continue;

                result.add(line);
            }

            reader.close();

        } catch (IOException e)
        {
            e.printStackTrace();
        }

        Macros.LOG("File <%s> has been loaded successfully.", filepath);

        return result;
    }

}
