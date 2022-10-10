package dev.tacker.hotpotato.utils;

import java.util.logging.Logger;

public class Logging {

    private final Logger logger = Logger.getLogger("Minecraft");
    private final String prefix;
    private final String debug_prefix;
    private final boolean debug;

    public Logging(String prefix, String debug_prefix, boolean debug) {
        this.prefix = prefix;
        this.debug_prefix = debug_prefix;
        this.debug = debug;
    }

    public void debug(String msg) {
        if (debug) {
            logger.info(prefix + debug_prefix + msg);
        }
    }

    public void log(String msg) {
        logger.info(prefix + msg);
    }

    public void error(String msg) {
        logger.severe(prefix + msg);
    }

}
