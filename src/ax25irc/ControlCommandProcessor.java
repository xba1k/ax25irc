/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ax25irc;

import ax25irc.ircd.server.IRCServer;
import ax25irc.ircd.server.MessageListener;
import ax25irc.ircd.server.ServMessage;
import java.util.Arrays;

/**
 *
 * @author alex
 */
public class ControlCommandProcessor implements MessageListener {

    AX25Irc service;

    public ControlCommandProcessor(AX25Irc service) {
        this.service = service;
    }

    public void onMessage(ServMessage message) {

        String params[] = message.getText().split(" ");

        if (params[0].equalsIgnoreCase("help")) {

            switch (params.length) {

                case 1:
                    message.reply("Supported commands:");
                    message.reply("HELP MODE DIGI");
                    message.reply("type HELP <command> for detailed info on a command.");
                    break;

                case 2:
                    if (params[1].equalsIgnoreCase("mode")) {
                        message.reply("MODE <mode> - display or set the global message format for direct messages. Supported modes are " + Arrays.asList(AX25Irc.MessageMode.values()));
                    } else if (params[1].equalsIgnoreCase("help")) {
                        message.reply("HELP <command> - display help about supported commands.");
                    } else if (params[1].equalsIgnoreCase("digi")) {
                        message.reply("DIGI <GROUP|DIRECT> <digis> - display or set APRS digipeater paths.");
                    } else {
                        message.reply("No such command");
                    }
            }

        } else if (params[0].equalsIgnoreCase("mode")) {

            switch (params.length) {

                case 1:
                    message.reply("Global mode set to " + service.getMessageMode().name() + ". Supported modes are " + Arrays.asList(AX25Irc.MessageMode.values()));
                    break;
                case 2:

                    AX25Irc.MessageMode m = null;

                    if (params[1].equalsIgnoreCase(AX25Irc.MessageMode.APRS.name())) {
                        m = AX25Irc.MessageMode.APRS;
                    } else if (params[1].equalsIgnoreCase(AX25Irc.MessageMode.AX25.name())) {
                        m = AX25Irc.MessageMode.AX25;
                    }

                    if (m != null) {

                        message.reply("Setting global mode to " + m.name());
                        service.setMessageMode(m);
                    } else {

                        message.reply("Unsupported mode type " + params[1]);

                    }
                    break;

            }

        } else if (params[0].equalsIgnoreCase("digi")) {

            switch (params.length) {

                case 1:
                    message.reply("Group digi path is set to " + Arrays.asList(service.getAprsMessageProcessor().getGroupDigis()) + ". Direct digi path is set to " + Arrays.asList(service.getAprsMessageProcessor().getDirectDigis()));
                    break;
                case 2:
                    if (params[1].equalsIgnoreCase("group")) {
                        message.reply("Group digi path is set to " + Arrays.asList(service.getAprsMessageProcessor().getGroupDigis()));
                    } else if (params[1].equalsIgnoreCase("direct")) {
                        message.reply("Direct digi path is set to " + Arrays.asList(service.getAprsMessageProcessor().getDirectDigis()));
                    } else {
                        message.reply("Invalid parameter " + params[1]);
                    }

                case 3:
                    if (params[1].equalsIgnoreCase("group")) {
                        message.reply("Setting group digi path to " + params[2]);
                        service.getAprsMessageProcessor().setGroupDigis(params[2]);
                    } else if (params[1].equalsIgnoreCase("direct")) {
                        message.reply("Setting direct digi path to " + params[2]);
                        service.getAprsMessageProcessor().setDirectDigis(params[2]);
                    } else {
                        message.reply("Invalid parameter " + params[1]);
                    }

            }

        }

    }

}
