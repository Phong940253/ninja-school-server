package server;

import real.ItemData;
import real.ClanManager;

import java.util.Timer;
import java.util.ArrayList;

public class BXHManager {
    public static final ArrayList<Entry>[] bangXH;
    public static final Timer t;

    public static void init() {
        for (int i = 0; i < BXHManager.bangXH.length; ++i) {
            BXHManager.bangXH[i] = new ArrayList<Entry>();
        }
        util.Debug("load BXH");
        for (int i = 0; i < BXHManager.bangXH.length; ++i) {
            initBXH(i);
        }
    }

    public static void initBXH(final int type) {
        BXHManager.bangXH[type].clear();
        final ArrayList<Entry> bxh = BXHManager.bangXH[type];
        switch (type) {
            case 0: {

                SQLManager.executeQuery("SELECT `name`,`yen`,`level` FROM `ninja` WHERE (`yen` > 0) ORDER BY `yen` DESC LIMIT 10;", (red) -> {

                    int i = 1;

                    while (red.next()) {
                        final String name = red.getString("name");
                        final int coin = red.getInt("yen");
                        final int level = red.getInt("level");
                        final Entry bXHE = new Entry();
                        bXHE.nXH = new long[2];
                        bXHE.name = name;
                        bXHE.index = i;
                        bXHE.nXH[0] = coin;
                        bXHE.nXH[1] = level;
                        bxh.add(bXHE);
                        ++i;
                    }
                    red.close();

                });


                break;
            }
            case 1: {

                SQLManager.executeQuery("SELECT `name`,`exp`,`level` FROM `ninja` WHERE (`exp` > 0) ORDER BY `exp` DESC LIMIT 10;", (red) -> {

                    int i = 1;
                    while (red.next()) {
                        final String name = red.getString("name");
                        final long exp = red.getLong("exp");
                        final int level2 = red.getInt("level");
                        final Entry bXHE2 = new Entry();
                        bXHE2.nXH = new long[2];
                        bXHE2.name = name;
                        bXHE2.index = i;
                        bXHE2.nXH[0] = exp;
                        bXHE2.nXH[1] = level2;
                        bxh.add(bXHE2);
                        ++i;
                    }
                    red.close();

                });

                break;
            }
            case 2: {

                SQLManager.executeQuery("SELECT `name`,`level` FROM `clan` WHERE (`level` > 0) ORDER BY `level` DESC LIMIT 10;", (red) -> {
                    int i = 1;
                    while (red.next()) {
                        final String name = red.getString("name");
                        final int level3 = red.getInt("level");
                        final Entry bXHE3 = new Entry();
                        bXHE3.nXH = new long[1];
                        bXHE3.name = name;
                        bXHE3.index = i;
                        bXHE3.nXH[0] = level3;
                        bxh.add(bXHE3);
                        ++i;
                    }
                    red.close();
                });


                break;
            }
            case 3: {

                SQLManager.executeQuery("SELECT `name`,`bagCaveMax`,`itemIDCaveMax` FROM `ninja` WHERE (`bagCaveMax` > 0) ORDER BY `bagCaveMax` DESC LIMIT 10;", (red) -> {
                    int i = 1;
                    while (red.next()) {
                        final String name = red.getString("name");
                        final int cave = red.getInt("bagCaveMax");
                        final short id = red.getShort("itemIDCaveMax");
                        final Entry bXHE = new Entry();
                        bXHE.nXH = new long[2];
                        bXHE.name = name;
                        bXHE.index = i;
                        bXHE.nXH[0] = cave;
                        bXHE.nXH[1] = id;
                        bxh.add(bXHE);
                        ++i;
                    }
                    red.close();
                });


                break;
            }
        }
    }

    public static Entry[] getBangXH(final int type) {
        final ArrayList<Entry> bxh = BXHManager.bangXH[type];
        final Entry[] bxhA = new Entry[bxh.size()];
        for (int i = 0; i < bxhA.length; ++i) {
            bxhA[i] = bxh.get(i);
        }
        return bxhA;
    }

    public static String getStringBXH(final int type) {
        StringBuilder str = new StringBuilder();
        switch (type) {
            case 0: {
                if (BXHManager.bangXH[type].isEmpty()) {
                    str = new StringBuilder("Chưa có thông tin");
                    break;
                }
                for (final Entry bxh : BXHManager.bangXH[type]) {
                    str.append(bxh.index).append(". ").append(bxh.name).append(": ").append(util.getFormatNumber(bxh.nXH[0])).append(" yên - cấp: ").append(bxh.nXH[1]).append("\n");
                }
                break;
            }
            case 1: {
                if (BXHManager.bangXH[type].isEmpty()) {
                    str = new StringBuilder("Chưa có thông tin");
                    break;
                }
                for (final Entry bxh : BXHManager.bangXH[type]) {
                    str.append(bxh.index).append(". ").append(bxh.name).append(": ").append(util.getFormatNumber(bxh.nXH[0])).append(" kinh nghiệm - cấp: ").append(bxh.nXH[1]).append("\n");
                }
                break;
            }
            case 2: {
                if (BXHManager.bangXH[type].isEmpty()) {
                    str = new StringBuilder("Chưa có thông tin");
                    break;
                }
                for (final Entry bxh : BXHManager.bangXH[type]) {
                    final ClanManager clan = ClanManager.getClanByName(bxh.name);
                    if (clan != null) {
                        str.append(bxh.index).append(". Gia tộc ").append(bxh.name).append(" trình độ cấp ").append(bxh.nXH[0]).append(" do ").append(clan.getmain_name()).append(" làm tộc trưởng, thành viên ").append(clan.members.size()).append("/").append(clan.getMemMax()).append("\n");
                    } else {
                        str.append(bxh.index).append(". Gia tộc ").append(bxh.name).append(" trình độ cấp ").append(bxh.nXH[0]).append(" đã bị giải tán\n");
                    }
                }
                break;
            }
            case 3: {
                if (BXHManager.bangXH[type].isEmpty()) {
                    str = new StringBuilder("Chưa có thông tin");
                    break;
                }
                for (final Entry bxh : BXHManager.bangXH[type]) {
                    str.append(bxh.index).append(". ").append(bxh.name).append(" nhận được ").append(util.getFormatNumber(bxh.nXH[0])).append(" ").append(ItemData.ItemDataId((int) bxh.nXH[1]).name).append("\n");
                }
                break;
            }
        }
        return str.toString();
    }

    static {
        bangXH = new ArrayList[4];
        t = new Timer(true);
    }

    public static class Entry {
        int index;
        String name;
        long[] nXH;
    }
}
