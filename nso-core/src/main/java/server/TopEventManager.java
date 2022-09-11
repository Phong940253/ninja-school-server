package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import patch.EventItem;

public class TopEventManager {
    public static final Map<Integer, ArrayList<EventEntry>> topXH;
    public static final Timer t;

    public static void init() {
        for (int i = 0; i < EventItem.entrys.length; i++) {
            int eventItemId = EventItem.entrys[i].getOutput().getId();
            TopEventManager.topXH.put(eventItemId, new ArrayList<EventEntry>());
        }
        util.Debug("load TXH");
        for (int i = 0; i < EventItem.entrys.length; i++) {
            int eventItemId = EventItem.entrys[i].getOutput().getId();
            initTXH(eventItemId);
        }
    }

    public static void reload() {
        init();
    }

    static {
        t = new Timer(true);
        topXH = new HashMap<>();
    }

    public static EventEntry[] getTopXH(final int eventItemId) {
        final ArrayList<EventEntry> txh = TopEventManager.topXH.getOrDefault(eventItemId, new ArrayList<>());

        final EventEntry[] txhA = new EventEntry[txh.size()];
        for (int i = 0; i < txhA.length; i++) {
            txhA[i] = txh.get(i);
        }

        return txhA;
    }

    public static void initTXH(final int eventItemId) {
        TopEventManager.topXH.get(eventItemId).clear();
        final ArrayList<EventEntry> txh = TopEventManager.topXH.get(eventItemId);

        SQLManager.executeQuery(String.format(
                "SELECT name, eventData->> '$.annual.a%d' as count FROM `ninja` WHERE eventData->> '$.annual.a%d' is not null order by -eventData->> '$.annual.a%d';",
                eventItemId, eventItemId, eventItemId), (red) -> {
                    int i = 1;
                    while (red.next()) {
                        final EventEntry tXHE = new EventEntry();
                        tXHE.name = red.getString("name");
                        tXHE.index = i;
                        tXHE.count = red.getLong("count");
                        txh.add(tXHE);
                        i++;
                    }
                    red.close();
                });

    }

    public static String getStringTXH(final int eventItemId) {
        StringBuilder str = new StringBuilder();
        if (TopEventManager.topXH.get(eventItemId).isEmpty()) {
            str = new StringBuilder("Chưa có thông tin");
        } else {
            for (final EventEntry txh : TopEventManager.topXH.get(eventItemId)) {
                str.append(txh.index).append(". ").append(txh.name).append(": ").append(txh.count).append(" lần.\n");
            }
        }

        return str.toString();
    }

    public static class EventEntry {
        int index;
        String name;
        long count;
    }
}
