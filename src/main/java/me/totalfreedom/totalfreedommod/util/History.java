package me.totalfreedom.totalfreedommod.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import me.totalfreedom.totalfreedommod.TotalFreedomMod;

public class History
{

    public static void reportHistory(final CommandSender sender, final String username)
    {

        Bukkit.getScheduler().runTaskAsynchronously(TotalFreedomMod.plugin(), new Runnable()
        {
            @Override
            public void run()
            {
                UUID uuid = null;
                FHistory history = null;
                uuid = UUIDFetcher.fetch(username);
                if (uuid != null)
                {
                    Gson gson = new GsonBuilder().create();
                    String compactUuid = uuid.toString().replace("-", "");
                    try
                    {
                        URL url = new URL("https://api.mojang.com/user/profiles/"
                                + compactUuid + "/names");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(
                                conn.getInputStream()));

                        FName[] oldNames = gson.fromJson(reader, FName[].class);
                        reader.close();
                        conn.disconnect();
                        Arrays.sort(oldNames);
                        history = new FHistory(uuid, oldNames);
                        printHistory(sender, oldNames);
                    }
                    catch (Exception ex)
                    {
                        FLog.severe(ex);
                    }

                    if (history == null)
                    {
                        sender.sendMessage(ChatColor.RED + "Player could not be found!");
                        return;
                    }

                }
            }
        }
        );
    }

    public static void printHistory(CommandSender sender, FName[] oldNames)
    {
        if (oldNames.length == 1)
        {
            sender.sendMessage(ChatColor.GREEN + oldNames[0].getName() + ChatColor.GOLD + " has never changed their name.");
        }
        else
        {
            sender.sendMessage(ChatColor.GOLD + "Original name: " + ChatColor.GREEN + oldNames[0].getName());

            for (int i = 1; i < oldNames.length; i++)
            {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(oldNames[i].changedToAt);
                String formattedDate = df.format(date);
                sender.sendMessage(ChatColor.BLUE + formattedDate + ChatColor.GOLD + " changed to " + ChatColor.GREEN + oldNames[i].getName());
            }
        }
    }
}

class FName implements Comparable<FName>
{

    public String name;
    public long changedToAt;

    public int compareTo(FName other)
    {
        return Long.compare(this.changedToAt, other.changedToAt);
    }

    public String getName()
    {
        return name;
    }

}
