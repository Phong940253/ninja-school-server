package threading;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import lombok.val;
import patch.*;
import battle.ClanBattleData;
import battle.GBattle;
import cache.ItemCache;
import cache.ItemOptionCache;
import cache.MapCache;
import cache.MobCache;
import cache.NpcCache;
import clan.ClanThanThu;
import real.*;

import java.io.*;

import server.*;
import real.User;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;

import org.json.simple.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONValue;
import org.json.simple.JSONArray;

import static real.ItemData.*;
import static real.ItemData.getListItemByMaxLevel;
import static server.GameScr.LAT_HINH_ID;
import static server.GameScr.LAT_HINH_RARE_IDS;
import static server.util.concatArray;

@SuppressWarnings("ALL")
public class Manager {
    public static final int EVENT_NONE = 0;
    public static final int EVENT_HE = 1;
    public static final int EVENT_TRUNG_THU = 2;
    public static final int EVENT_TET_NGUYEN_DAN = 4;
    public static final int EVENT_8_3 = 5;
    public static final int EVENT_VUA_HUNG = 6;

    public static ItemCache[] itemCaches;
    public static ItemOptionCache[] itemOptionCaches;
    public static MapCache[] mapCaches;
    public static NpcCache[] npcCaches;
    public static MobCache[] mobCaches;

    public static int TIME_MAINTAIN = 5;
    public static int BOSS_WAIT_TIME_UNIT;
    public static int MIN_YEN_BOSS;
    public static int MAX_YEN_BOSS;
    public static int TIME_DISCONNECT = 10;
    public static int TIME_NO_LOGIN_DISCONNECT;
    public static int MAX_LEVEL;
    public static int MAX_LUGGAGE;
    public static int MAX_ITEM_QUANTITY;

    public static int YEN_COEF;
    public static int MAX_LEVEL_RECEIVE_YEN_COEF;
    public static int LUONG_COEF;
    public static int MAX_LEVEL_RECEIVE_LUONG_COEF;
    public static int DROP_LUONG_PERCENT;

    public static int EVENT_ITEM_DROP_PERCENT;
    public static long EVENT_TOP_END_TIME;

    public int PORT;
    public static String host;
    public static String mysql_host;
    public static String mysql_port;
    public static String mysql_database;
    public static String mysql_user;
    public static String mysql_pass;
    public static byte vsData;
    public static byte vsMap;
    public static byte vsSkill;
    public static byte vsItem;
    public byte[][] tasks;
    private byte[][] maptasks;
    static Server server;
    public RotationLuck[] rotationluck;
    public byte EVENT;
    public String[] NinjaS;
    public static short MAX_CLIENT;

    public static byte MAX_PERCENT = 100;
    public static byte N_YEN;
    public static byte PERCENT_TA_TL = 100;
    public static long YEN_TL;
    public static long YEN_TA;
    public static short[] LANG_CO_ITEM_IDS;
    public static short[] VDMQ_ITEM_IDS;
    public static short[] EMPTY = new short[0];

    public static short[] EXPAND_TASK_ITEM_IDS;

    public static long MIN_TIME_REFRESH_MOB;
    public static long MIN_TIME_REFRESH_BOSS;
    public static short[] BOSS_ITEM_LV45;
    public static short[] BOSS_ITEM_LV55;
    public static short[] BOSS_ITEM_LV60;
    public static short[] BOSS_ITEM_LV65;
    public static short[] BOSS_ITEM_LV75;
    public static short[] BOSS_ITEM_LV99;
    public static short[] BOSS_ITEM_LV90;
    public static short[] BOSS_ITEM_LV100;
    public static short[] BOSS_ITEM_LV110;
    public static short[] BOSS_ITEM_LV130;
    public static short[] BOSS_ITEM_LV140;
    public static short[] BOSS_ITEM_LV150;
    public static short[] BOSS_LC_ITEM;
    public static short[] BOSS_DEFAULT_ITEM;
    public static byte PERCENT_DAME_BOSS;

    public static long TIME_DESTROY_MAP;
    public static short N_ITEM_BOSS;

    public static short[] ID_FEATURES;
    public static short[] IDS_THUONG_LV70;
    public static short[] IDS_THUONG_LV90;
    public static short[] IDS_THUONG_LV130;
    public static short[] IDS_THUONG_LV150;
    public static short MULTI_EXP;
    public static short MULTI_TOPUP;
    public static short N_THREAD_STOP;
    public static short MAX_CLIENT_PER_SOCKET;
    public static short MAX_SOCKET_PER_CLIENT;

    public static short[] MOMENT_REFRESH_BATTLE;

    public static short[] LDGT_REWARD_ITEM_ID;

    /**
     * 0 MIN
     * 1 MAX
     */
    public static int[] MIN_MAX_YEN_RUONG_MAY_MAN = new int[2];
    public static int[] MIN_MAX_YEN_RUONG_TINH_SAO = new int[2];
    public static int[] MIN_MAX_YEN_RUONG_MA_QUAI = new int[2];

    public static String TOPUP_CARD_API;
    public static String TOPUP_CARD_API_KEY;
    public static String NSO_MS_API;

    public Manager() {
        entrys = new HashMap<>();
        ItemSell.entrys = new ConcurrentHashMap<>();
        this.rotationluck = new RotationLuck[3];
        this.NinjaS = new String[] { "Chưa vào lớp", "Ninja Kiếm", "Ninja Phi Tiêu", "Ninja Kunai", "Ninja Cung",
                "Ninja Đao", "Ninja Quạt" };
        preload();
    }

    public void preload() {
        this.loadConfigFile();
        this.EVENT = 0;
        if (this.rotationluck[0] == null) {
            this.rotationluck[0] = new RotationLuck("Vòng xoay vip", (byte) 1, (short) 120, 5, 1_000,
                    500_000_000, "lượng");
            this.rotationluck[0].start();
        }
        if (rotationluck[1] == null) {
            this.rotationluck[1] = new RotationLuck("Vòng xoay thường", (byte) 0, (short) 120, 50_000, 1_000_000,
                    100_000_000);
            this.rotationluck[1].start();
        }
        if (rotationluck[2] == null) {
            this.rotationluck[2] = new RotationLuck("Tài xỉu", (byte) 2, (short) 60, 50_000, 1_000_000,
                    100_000_000);
            this.rotationluck[2].start();
        }
        this.loadCache();
        this.loadProperties();
        this.loadDataBase();
    }

    public static long TIME_REFRESH_MOB;
    public static long TIME_REFRESH_BOSS;
    public static byte MIN_DA_LV;
    public static byte MAX_DA_LV;
    public static byte N_DA;
    public static byte N_HP_MP;
    public static byte N_PMNG;

    public static String[] MENU_EVENT_NPC;
    public static short ID_EVENT_NPC;
    public static String[] EVENT_NPC_CHAT;

    private void loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream("application.properties")) {

            // load properties from file
            properties.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            MAX_PERCENT = Byte.parseByte(properties.getProperty("drop-item-percent"));
            PERCENT_TA_TL = Byte.parseByte(properties.getProperty("percent-ta-tl"));

            YEN_COEF = Integer.parseInt(properties.getProperty("yen-coef"));
            MAX_LEVEL_RECEIVE_YEN_COEF = Integer.parseInt(properties.getProperty("max-level-receive-yen-coef"));
            LUONG_COEF = Integer.parseInt(properties.getProperty("luong-coef"));
            MAX_LEVEL_RECEIVE_LUONG_COEF = Integer.parseInt(properties.getProperty("max-level-receive-luong-coef"));
            DROP_LUONG_PERCENT = Integer.parseInt(properties.getProperty("drop-luong-percent"));

            TIME_REFRESH_MOB = 1000 * Long.parseLong(properties.getProperty("time-refresh-mob"));
            TIME_REFRESH_BOSS = 1000 * Long.parseLong(properties.getProperty("time-refresh-boss"));
            MIN_DA_LV = Byte.parseByte(properties.getProperty("min-da-lv"));
            MAX_DA_LV = Byte.parseByte(properties.getProperty("max-da-lv"));
            N_YEN = Byte.parseByte(properties.getProperty("n-yen"));
            LANG_CO_ITEM_IDS = parseShortArray(properties.getProperty("lang-co-item-ids"));
            VDMQ_ITEM_IDS = parseShortArray(properties.getProperty("vdmq-item-ids"));
            EXPAND_TASK_ITEM_IDS = parseShortArray(properties.getProperty("expand-task-item-ids"));
            N_DA = Byte.parseByte(properties.getProperty("n-da"));
            N_HP_MP = Byte.parseByte(properties.getProperty("n-hp-mp"));
            N_PMNG = Byte.parseByte(properties.getProperty("n-pmng"));
            MAX_LEVEL = Integer.parseInt(properties.getProperty("max-level"));
            MAX_LUGGAGE = Integer.parseInt(properties.getProperty("max-luggage"));
            MAX_ITEM_QUANTITY = Integer.parseInt(properties.getProperty("max-item-quantity"));

            EVENT_ITEM_DROP_PERCENT = Integer.parseInt(properties.getProperty("event-item-drop-percent"));
            EVENT_TOP_END_TIME = Long.parseLong(properties.getProperty("event-top-end-time"));

            BOSS_WAIT_TIME_UNIT = Integer.parseInt(properties.getProperty("boss-wait-time-unit"));

            BOSS_ITEM_LV45 = parseShortArray(properties.getProperty("boss-item-lv45"));
            BOSS_ITEM_LV55 = parseShortArray(properties.getProperty("boss-item-lv55"));
            BOSS_ITEM_LV60 = parseShortArray(properties.getProperty("boss-item-lv60"));
            BOSS_ITEM_LV65 = parseShortArray(properties.getProperty("boss-item-lv65"));
            BOSS_ITEM_LV75 = parseShortArray(properties.getProperty("boss-item-lv75"));
            BOSS_ITEM_LV99 = parseShortArray(properties.getProperty("boss-item-lv99"));
            BOSS_ITEM_LV90 = parseShortArray(properties.getProperty("boss-item-lv90"));
            BOSS_ITEM_LV100 = parseShortArray(properties.getProperty("boss-item-lv100"));
            BOSS_ITEM_LV110 = parseShortArray(properties.getProperty("boss-item-lv110"));
            BOSS_ITEM_LV130 = parseShortArray(properties.getProperty("boss-item-lv130"));
            BOSS_ITEM_LV140 = parseShortArray(properties.getProperty("boss-item-lv140"));
            BOSS_ITEM_LV150 = parseShortArray(properties.getProperty("boss-item-lv150"));
            BOSS_DEFAULT_ITEM = parseShortArray(properties.getProperty("boss-default-item"));

            BOSS_LC_ITEM = parseShortArray(properties.getProperty("boss-item-lc"));
            N_ITEM_BOSS = Short.parseShort(properties.getProperty("n-item-boss"));
            IDS_THUONG_LV70 = parseShortArray(properties.getProperty("thuong-lv70"));
            IDS_THUONG_LV90 = parseShortArray(properties.getProperty("thuong-lv90"));
            IDS_THUONG_LV130 = parseShortArray(properties.getProperty("thuong-lv130"));
            IDS_THUONG_LV150 = parseShortArray(properties.getProperty("thuong-lv150"));

            // Server.MOMENT_BOSS_REFRESH =
            // parseShortArray(properties.getProperty("moment-boss-refresh"));
            Server.DURATION_TIME_BOSS_REFRESH = Short.parseShort(properties.getProperty("duration-time-boss-refresh"));
            Server.MAX_BOSS = Short.parseShort(properties.getProperty("max-boss"));

            MULTI_EXP = Short.parseShort(properties.getProperty("multi-exp"));
            MULTI_TOPUP = Short.parseShort(properties.getProperty("multi-topup"));
            TIME_MAINTAIN = Integer.parseInt(properties.getProperty("time-bao-tri"));
            N_THREAD_STOP = Short.parseShort(properties.getProperty("n-thread-stop"));

            TIME_DESTROY_MAP = Integer.parseInt(properties.getProperty("time-Destroy-Map"));
            MAX_CLIENT = Short.parseShort(properties.getProperty("max-Client"));
            TIME_DISCONNECT = Short.parseShort(properties.getProperty("time-disconnect"));
            TIME_NO_LOGIN_DISCONNECT = Short.parseShort(properties.getProperty("time-no-login-disconnect"));

            Resource.TIME_REMOVE_RESOURCE = Long.parseLong(properties.getProperty("time-remove-resource")) * 60000;
            User.DIFFER_USE_ITEM_TIME = Short.parseShort(properties.getProperty("differ-use-item-time"));
            User.MAX_USE_ITEM_FAST = Short.parseShort(properties.getProperty("max-use-item-fast"));
            PERCENT_DAME_BOSS = Byte.parseByte(properties.getProperty("percent-dame-boss"));
            MOMENT_REFRESH_BATTLE = parseShortArray(properties.getProperty("moment-refresh-battle"));
            GBattle.WATING_TIME = Integer.parseInt(properties.getProperty("waiting-time"));
            GBattle.START_TIME = Long.parseLong(properties.getProperty("start-time"));

            EVENT = Byte.parseByte(properties.getProperty("event-type"));
            Body.PERCENT_DAME_PEOPLE = Short.parseShort(properties.getProperty("damage-people-rate"));
            EventItem.entrys = Mapper.converter.readValue(properties.getProperty("event-input" + EVENT),
                    EventItem[].class);

            User.MIN_TIME_RESET_POINT = Long.parseLong(properties.getProperty("min-time-reset-point"));
            Server.TIME_SLEEP_SHINWA_THREAD = Long.parseLong(properties.getProperty("time-run-shinwa-thread"));
            LAT_HINH_ID = parseShortArray(properties.getProperty("lat-hinh-id"));
            LAT_HINH_RARE_IDS = parseShortArray(properties.getProperty("lat-hinh-rare-ids"));

            MIN_MAX_YEN_RUONG_MAY_MAN = parseIntArray(properties.getProperty("yen-ruong-may-man"));
            MIN_MAX_YEN_RUONG_TINH_SAO = parseIntArray(properties.getProperty("yen-ruong-tinh-sao"));
            MIN_MAX_YEN_RUONG_MA_QUAI = parseIntArray(properties.getProperty("yen-ruong-ma-quai"));
            LDGT_REWARD_ITEM_ID = parseShortArray(properties.getProperty("ldgt-reward-item-id"));
            GameScr.ArryenLuck = parseIntArray(properties.getProperty("yen-lat-hinh"));

            ITEM_LV_10 = getListItemByMaxLevel(10, MAX_PERCENT, N_YEN, N_DA, N_HP_MP, N_PMNG);
            ITEM_LV_20 = getListItemByMaxLevel(20, MAX_PERCENT, N_YEN, N_DA, N_HP_MP, N_PMNG);
            ITEM_LV_30 = getListItemByMaxLevel(30, MAX_PERCENT, N_YEN, N_DA, N_HP_MP, N_PMNG);
            ITEM_LV_40 = getListItemByMaxLevel(40, MAX_PERCENT, N_YEN, N_DA, N_HP_MP, N_PMNG);
            ITEM_LV_50 = getListItemByMaxLevel(50, MAX_PERCENT, N_YEN, N_DA, N_HP_MP, N_PMNG);
            ITEM_LV_60 = getListItemByMaxLevel(60, MAX_PERCENT, N_YEN, N_DA, N_HP_MP, N_PMNG);
            ITEM_LV_70 = getListItemByMaxLevel(70, MAX_PERCENT, N_YEN, N_DA, N_HP_MP, N_PMNG);
            ITEM_LV_80 = getListItemByMaxLevel(80, MAX_PERCENT, N_YEN, N_DA, N_HP_MP, N_PMNG);
            ITEM_LV_90 = getListItemByMaxLevel(80, MAX_PERCENT, N_YEN, N_DA, N_HP_MP, N_PMNG);
            ITEM_LV_100 = getListItemByMaxLevel(80, MAX_PERCENT, N_YEN, N_DA, N_HP_MP, N_PMNG);
        } catch (IOException e) {
            e.printStackTrace();
            Server.getInstance().stop();
        }
    }

    private String[] parseStringArray(String token) {
        return token.split("\\,\\s*?");
    }

    private short[] parseShortArray(String input) {
        val tokens = input.split("\\,\\s*?");
        val result = new short[tokens.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Short.parseShort(tokens[i].trim());
        }
        return result;
    }

    private int[] parseIntArray(String input) {
        val tokens = input.split("\\,\\s*?");
        val result = new int[tokens.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Integer.parseInt(tokens[i].trim());
        }
        return result;
    }

    public static Map getMapid(final int id) {
        synchronized (Manager.server.getMaps()) {
            for (short i = 0; i < Manager.server.getMaps().length; ++i) {
                final Map map = Manager.server.getMapById(i);
                if (map != null && map.id == id) {
                    return map;
                }
            }
            return null;
        }
    }

    @SneakyThrows
    private void loadConfigFile() {

        final byte[] ab = GameScr.loadFile("ninja.conf").toByteArray();
        if (ab == null) {
            util.Debug("Config file not found!");
            System.exit(0);
        }
        final String data = new String(ab);
        final HashMap<String, String> configMap = new HashMap<String, String>();
        final StringBuilder sbd = new StringBuilder();
        boolean bo = false;
        for (int i = 0; i <= data.length(); ++i) {
            final char es;
            if (i == data.length() || (es = data.charAt(i)) == '\n') {
                bo = false;
                final String sbf = sbd.toString().trim();
                if (sbf != null && !sbf.equals("") && sbf.charAt(0) != '#') {
                    final int j = sbf.indexOf(58);
                    if (j > 0) {
                        final String key = sbf.substring(0, j).trim();
                        final String value = sbf.substring(j + 1).trim();
                        configMap.put(key, value);
                        util.Debug("config: " + key + "-" + value);
                    }
                }
                sbd.setLength(0);
            } else {
                if (es == '#') {
                    bo = true;
                }
                if (!bo) {
                    sbd.append(es);
                }
            }
        }

        if (configMap.containsKey("host")) {
            this.host = configMap.get("host");
        } else {
            this.host = "localhost";
        }
        if (configMap.containsKey("port")) {
            this.PORT = Short.parseShort(configMap.get("port"));
        } else {
            this.PORT = 14444;
        }

//        this.mysql_host = System.getenv("DB_HOST");
//        this.mysql_port = System.getenv("DB_PORT");
//        this.mysql_user = System.getenv("DB_USER");
//        this.mysql_pass = System.getenv("DB_PASS");
//        this.mysql_database = System.getenv("DB_DATABASE");
        this.mysql_host = "localhost";
        this.mysql_port = "3306";
        this.mysql_user = "root";
        this.mysql_pass = "01676940253";
        this.mysql_database = "nja";

        try {
            util.setDebug(Boolean.parseBoolean(System.getenv("DEBUG")));
        } catch (Exception e) {
            util.setDebug(false);
        }
        util.setDebug(true);
        if (configMap.containsKey("version-Data")) {
            this.vsData = Byte.parseByte(configMap.get("version-Data"));
        } else {
            this.vsData = 54;
        }
        if (configMap.containsKey("version-Map")) {
            this.vsMap = Byte.parseByte(configMap.get("version-Map"));
        } else {
            this.vsMap = 86;
        }
        if (configMap.containsKey("version-Skill")) {
            this.vsSkill = Byte.parseByte(configMap.get("version-Skill"));
        } else {
            this.vsSkill = 10;
        }
        if (configMap.containsKey("version-Item")) {
            this.vsItem = Byte.parseByte(configMap.get("version-Item"));
        } else {
            this.vsItem = 70;
        }

        TOPUP_CARD_API = System.getenv("TOPUP_CARD_API");
        TOPUP_CARD_API_KEY = System.getenv("TOPUP_CARD_API_KEY");
        NSO_MS_API = System.getenv("NSO_MS_API");
        MAX_CLIENT_PER_SOCKET = Short.parseShort(configMap.get("max-Client-Per-Socket"));
        MAX_SOCKET_PER_CLIENT = Short.parseShort(configMap.get("max-Socket-Per-Client"));
        SQLManager.create(mysql_host, mysql_port, mysql_database, mysql_user, mysql_pass);

    }

    private void loadCache() {
        System.out.println("Load ItemOptionCache..");
        try {
            SQLManager.executeQuery("SELECT * FROM `optionitem`;", (res) -> {
                if (res.last()) {
                    itemOptionCaches = new ItemOptionCache[res.getRow()];
                    res.beforeFirst();
                }
                int i = 0;
                while (res.next()) {
                    final ItemOptionCache iotemplate = new ItemOptionCache();
                    iotemplate.id = res.getInt("id");
                    iotemplate.name = res.getString("name");
                    iotemplate.type = res.getByte("type");
                    itemOptionCaches[i] = iotemplate;
                    ++i;
                }
                res.close();
            });

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        System.out.println("Load itemCache..");
        try {
            SQLManager.executeQuery("SELECT * FROM `item`;", (res) -> {
                if (res.last()) {
                    itemCaches = new ItemCache[res.getRow()];
                    res.beforeFirst();
                }
                int i = 0;
                while (res.next()) {
                    final ItemCache itemCache = new ItemCache();
                    itemCache.id = res.getShort("id");
                    itemCache.type = res.getByte("type");
                    itemCache.gender = res.getByte("gender");
                    itemCache.name = res.getString("name");
                    itemCache.description = res.getString("description");
                    itemCache.level = res.getInt("level");
                    itemCache.iconID = res.getShort("iconID");
                    itemCache.part = res.getShort("part");
                    itemCache.isUpToUp = res.getBoolean("uptoup");
                    itemCaches[i] = itemCache;
                    ++i;
                }
                res.close();
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        System.out.println("Load Map mapCache..");
        try {
            SQLManager.executeQuery("SELECT * FROM `map`;", (res) -> {
                if (res.last()) {
                    mapCaches = new MapCache[res.getRow()];
                    res.beforeFirst();
                }
                int i = 0;
                while (res.next()) {
                    final MapCache mapCache = new MapCache();
                    mapCache.mapName = res.getString("name");
                    mapCaches[i] = mapCache;
                    ++i;
                }
                res.close();
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        System.out.println("Load Map npcCache..");
        try {
            SQLManager.executeQuery("SELECT * FROM `npc`;", (res) -> {
                if (res.last()) {
                    npcCaches = new NpcCache[res.getRow()];
                    res.beforeFirst();
                }
                int i = 0;
                while (res.next()) {
                    final NpcCache npcCache = new NpcCache();
                    npcCache.name = res.getString("name");
                    npcCache.headId = res.getShort("head");
                    npcCache.bodyId = res.getShort("body");
                    npcCache.legId = res.getShort("leg");
                    final JSONArray jarr = (JSONArray) JSONValue.parse(res.getString("menu"));
                    npcCache.menu = new String[jarr.size()][];
                    for (int j = 0; j < npcCache.menu.length; j++) {
                        final JSONArray jarr2 = (JSONArray) jarr.get(j);
                        npcCache.menu[j] = new String[jarr2.size()];
                        for (int k2 = 0; k2 < npcCache.menu[j].length; k2++) {
                            npcCache.menu[j][k2] = jarr2.get(k2).toString();
                        }
                    }

                    npcCaches[i] = npcCache;
                    ++i;
                }
                res.close();
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        System.out.println("Load Map mobCache..");
        try {
            SQLManager.executeQuery("SELECT * FROM `mob`;", (res) -> {
                if (res.last()) {
                    mobCaches = new MobCache[res.getRow()];
                    res.beforeFirst();
                }
                int i = 0;
                while (res.next()) {
                    final MobCache mobCache = new MobCache();
                    mobCache.type = res.getByte("type");
                    mobCache.name = res.getString("name");
                    mobCache.hp = res.getInt("hp");
                    mobCache.rangeMove = res.getByte("rangeMove");
                    mobCache.speed = res.getByte("speed");

                    mobCaches[i] = mobCache;
                    ++i;
                }
                res.close();
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        Service.createCacheItem();
        Service.createCacheMap();
    }

    public void loadDataBase() {
        entrys.clear();
        ItemSell.entrys.clear();

        final int[] i = { 0 };
        try {

            /**
             * OK
             */
            SQLManager.executeQuery("SELECT * FROM `tasks`;", (res) -> {

                if (res.last()) {
                    this.tasks = new byte[res.getRow()][];
                    this.maptasks = new byte[this.tasks.length][];
                    res.beforeFirst();
                }
                while (res.next()) {
                    final JSONArray jarr = (JSONArray) JSONValue.parse(res.getString("tasks"));
                    final JSONArray jarr2 = (JSONArray) JSONValue.parse(res.getString("maptasks"));
                    this.tasks[i[0]] = new byte[jarr.size()];
                    this.maptasks[i[0]] = new byte[this.tasks.length];
                    for (byte j = 0; j < jarr.size(); ++j) {
                        this.tasks[i[0]][j] = Byte.parseByte(jarr.get((int) j).toString());
                        this.maptasks[i[0]][j] = Byte.parseByte(jarr2.get((int) j).toString());
                    }
                    ++i[0];
                }
                res.close();

            });

            i[0] = 0;
            SQLManager.executeQuery("SELECT * FROM `level`;", (res) -> {

                while (res.next()) {
                    final Level level = new Level();
                    level.level = Integer.parseInt(res.getString("level"));
                    level.exps = Long.parseLong(res.getString("exps"));
                    level.ppoint = Short.parseShort(res.getString("ppoint"));
                    level.spoint = Short.parseShort(res.getString("spoint"));
                    Level.addLevel(level);
                    ++i[0];
                }
                res.close();

            });
            Level.onFinishAddLevel();
            i[0] = 0;
            SQLManager.executeQuery("SELECT * FROM `effect`;", (res) -> {

                while (res.next()) {
                    final EffectData eff = new EffectData();
                    eff.id = Byte.parseByte(res.getString("id"));
                    eff.type = Byte.parseByte(res.getString("type"));
                    eff.name = res.getString("name");
                    eff.iconId = Short.parseShort(res.getString("iconId"));
                    EffectData.entrys.add(eff);
                    ++i[0];
                }
                res.close();

            });

            i[0] = 0;
            SQLManager.executeQuery("SELECT * FROM `item`;", (res) -> {

                while (res.next()) {
                    final ItemData item = new ItemData();
                    item.id = Short.parseShort(res.getString("id"));
                    item.type = Byte.parseByte(res.getString("type"));
                    item.nclass = Byte.parseByte(res.getString("class"));
                    item.skill = Byte.parseByte(res.getString("skill"));
                    item.gender = Byte.parseByte(res.getString("gender"));
                    item.name = res.getString("name");
                    item.description = res.getString("description");
                    item.level = Byte.parseByte(res.getString("level"));
                    item.iconID = Short.parseShort(res.getString("iconID"));
                    item.part = Short.parseShort(res.getString("part"));
                    item.isUpToUp = (Byte.parseByte(res.getString("uptoup")) == 1);
                    item.isExpires = (Byte.parseByte(res.getString("isExpires")) == 1);
                    item.seconds_expires = Long.parseLong(res.getString("secondsExpires"));
                    item.saleCoinLock = Integer.parseInt(res.getString("saleCoinLock"));
                    item.itemoption = new ArrayList<Option>();
                    JSONArray Option = (JSONArray) JSONValue.parse(res.getString("ItemOption"));
                    for (int k = 0; k < Option.size(); ++k) {
                        final JSONObject job = (JSONObject) Option.get(k);
                        final Option option = new Option(Integer.parseInt(job.get((Object) "id").toString()),
                                Integer.parseInt(job.get((Object) "param").toString()));
                        item.itemoption.add(option);
                    }
                    item.option1 = new ArrayList<Option>();
                    Option = (JSONArray) JSONValue.parse(res.getString("Option1"));
                    for (int k = 0; k < Option.size(); ++k) {
                        final JSONObject job = (JSONObject) Option.get(k);
                        final Option option = new Option(Integer.parseInt(job.get((Object) "id").toString()),
                                Integer.parseInt(job.get((Object) "param").toString()));
                        item.option1.add(option);
                    }
                    item.option2 = new ArrayList<Option>();
                    Option = (JSONArray) JSONValue.parse(res.getString("Option2"));
                    for (int k = 0; k < Option.size(); ++k) {
                        final JSONObject job = (JSONObject) Option.get(k);
                        final Option option = new Option(Integer.parseInt(job.get((Object) "id").toString()),
                                Integer.parseInt(job.get((Object) "param").toString()));
                        item.option2.add(option);
                    }
                    item.option3 = new ArrayList<Option>();
                    Option = (JSONArray) JSONValue.parse(res.getString("Option3"));
                    for (int k = 0; k < Option.size(); ++k) {
                        final JSONObject job = (JSONObject) Option.get(k);
                        final Option option = new Option(Integer.parseInt(job.get((Object) "id").toString()),
                                Integer.parseInt(job.get((Object) "param").toString()));
                        item.option3.add(option);
                    }
                    entrys.put((int) item.id, item);
                    ++i[0];
                }
                res.close();

            });

            i[0] = 0;
            SQLManager.executeQuery("SELECT * FROM `mob`;", (res) -> {

                while (res.next()) {
                    final MobData md = new MobData();
                    md.id = Integer.parseInt(res.getString("id"));
                    md.type = Byte.parseByte(res.getString("type"));
                    md.name = res.getString("name");
                    md.hp = Integer.parseInt(res.getString("hp"));
                    md.rangeMove = Byte.parseByte(res.getString("rangeMove"));
                    md.speed = Byte.parseByte(res.getString("speed"));

                    MobData.entrys.put(md.id, md);
                    ++i[0];
                }
                res.close();

            });

            SQLManager.executeQuery("SELECT * FROM `npc`;", (res) -> {
                while (res.next()) {
                    Npc npc = new Npc();
                    npc.id = res.getByte("id");
                    npc.name = res.getString("name");
                    npc.head = res.getShort("head");
                    npc.body = res.getShort("body");
                    npc.leg = res.getShort("leg");
                    npc.type = res.getByte("type");
                    Npc.npcTemplates.put(npc.id, npc);
                }
            });

            i[0] = 0;
            SQLManager.executeQuery("SELECT * FROM `map`;", (res) -> {

                if (res.last()) {
                    MapTemplate.arrTemplate = new MapTemplate[res.getRow()];
                    res.beforeFirst();
                }
                while (res.next()) {
                    final MapTemplate temp = new MapTemplate();
                    temp.id = res.getInt("id");
                    temp.tileID = res.getByte("tileId");
                    temp.bgID = res.getByte("bgID");
                    temp.name = res.getString("name");
                    temp.typeMap = res.getByte("typeMap");
                    temp.maxplayers = res.getByte("maxplayer");
                    temp.numarea = res.getByte("numzone");
                    temp.x0 = res.getShort("x0");
                    temp.y0 = res.getShort("y0");
                    JSONArray jarr3 = (JSONArray) JSONValue.parse(res.getString("Vgo"));
                    temp.vgo = new Vgo[jarr3.size()];
                    for (byte j = 0; j < jarr3.size(); ++j) {
                        temp.vgo[j] = new Vgo();
                        final JSONArray jar2 = (JSONArray) JSONValue.parse(jarr3.get((int) j).toString());
                        final Vgo vg = temp.vgo[j];
                        vg.minX = Short.parseShort(jar2.get(0).toString());
                        vg.minY = Short.parseShort(jar2.get(1).toString());
                        vg.maxX = Short.parseShort(jar2.get(2).toString());
                        if (vg.maxX == -1) {
                            vg.maxX = (short) (vg.minX + 24);
                        }
                        vg.maxY = Short.parseShort(jar2.get(3).toString());
                        if (vg.maxY == -1) {
                            vg.maxY = (short) (vg.minY + 24);
                        }
                        vg.mapid = Short.parseShort(jar2.get(4).toString());
                        vg.goX = Short.parseShort(jar2.get(5).toString());
                        vg.goY = Short.parseShort(jar2.get(6).toString());
                    }
                    jarr3 = (JSONArray) JSONValue.parse(res.getString("Mob"));
                    temp.arMobid = new short[jarr3.size()];
                    temp.arrMobx = new short[jarr3.size()];
                    temp.arrMoby = new short[jarr3.size()];
                    temp.arrMobstatus = new byte[jarr3.size()];
                    temp.arrMoblevel = new int[jarr3.size()];
                    temp.arrLevelboss = new byte[jarr3.size()];
                    temp.arrisboss = new boolean[jarr3.size()];
                    for (short l = 0; l < jarr3.size(); ++l) {
                        final JSONArray entry = (JSONArray) jarr3.get((int) l);
                        temp.arMobid[l] = Short.parseShort(entry.get(0).toString());
                        temp.arrMoblevel[l] = Integer.parseInt(entry.get(1).toString());
                        temp.arrMobx[l] = Short.parseShort(entry.get(2).toString());
                        temp.arrMoby[l] = Short.parseShort(entry.get(3).toString());
                        temp.arrMobstatus[l] = Byte.parseByte(entry.get(4).toString());
                        temp.arrLevelboss[l] = Byte.parseByte(entry.get(5).toString());
                        temp.arrisboss[l] = Boolean.parseBoolean(entry.get(6).toString());
                    }
                    jarr3 = (JSONArray) JSONValue.parse(res.getString("NPC"));
                    temp.npc = new Npc[jarr3.size()];
                    for (byte j = 0; j < jarr3.size(); ++j) {
                        temp.npc[j] = new Npc();
                        final JSONArray jar2 = (JSONArray) JSONValue.parse(jarr3.get((int) j).toString());
                        final Npc npc = temp.npc[j];
                        npc.type = Byte.parseByte(jar2.get(0).toString());
                        npc.x = Short.parseShort(jar2.get(1).toString());
                        npc.y = Short.parseShort(jar2.get(2).toString());
                        npc.id = Byte.parseByte(jar2.get(3).toString());
                    }
                    MapTemplate.arrTemplate[i[0]] = temp;
                    ++i[0];
                }
                res.close();

            });

            i[0] = 0;
            SQLManager.executeQuery("SELECT * FROM `skill`;", (res) -> {

                while (res.next()) {
                    final SkillData skill = new SkillData();
                    skill.id = Short.parseShort(res.getString("id"));
                    skill.nclass = Byte.parseByte(res.getString("class"));
                    skill.name = res.getString("name");
                    skill.maxPoint = Byte.parseByte(res.getString("maxPoint"));
                    skill.type = Byte.parseByte(res.getString("type"));
                    skill.iconId = Short.parseShort(res.getString("iconId"));
                    skill.desc = res.getString("desc");
                    final JSONArray Skilltemplate = (JSONArray) JSONValue.parse(res.getString("SkillTemplates"));
                    for (final Object template : Skilltemplate) {
                        final JSONObject job2 = (JSONObject) template;
                        final SkillTemplates temp2 = new SkillTemplates();
                        temp2.skillId = Short.parseShort(job2.get((Object) "skillId").toString());
                        temp2.point = Byte.parseByte(job2.get((Object) "point").toString());
                        temp2.level = Integer.parseInt(job2.get((Object) "level").toString());
                        temp2.manaUse = Short.parseShort(job2.get((Object) "manaUse").toString());
                        temp2.coolDown = Integer.parseInt(job2.get((Object) "coolDown").toString());
                        temp2.dx = Short.parseShort(job2.get((Object) "dx").toString());
                        temp2.dy = Short.parseShort(job2.get((Object) "dy").toString());
                        temp2.maxFight = Byte.parseByte(job2.get((Object) "maxFight").toString());
                        final JSONArray Option2 = (JSONArray) JSONValue.parse(job2.get((Object) "options").toString());
                        for (final Object option2 : Option2) {
                            final JSONObject job3 = (JSONObject) option2;
                            final Option op = new Option(Integer.parseInt(job3.get((Object) "id").toString()),
                                    Integer.parseInt(job3.get((Object) "param").toString()));
                            temp2.options.add(op);
                        }
                        skill.templates.add(temp2);
                    }
                    SkillData.entrys.put(skill.id, skill);
                    ++i[0];
                }
                res.close();

            });

            i[0] = 0;
            SQLManager.executeQuery("SELECT * FROM `itemsell`;", (res) -> {

                while (res.next()) {
                    final ItemSell sell = new ItemSell();
                    sell.id = Integer.parseInt(res.getString("id"));
                    sell.type = Byte.parseByte(res.getString("type"));
                    final JSONArray jar3 = (JSONArray) JSONValue.parse(res.getString("ListItem"));
                    if (jar3 != null) {
                        sell.item = new Item[jar3.size()];
                        for (byte j = 0; j < jar3.size(); ++j) {
                            final JSONObject job = (JSONObject) jar3.get((int) j);
                            final Item item2 = parseItem(jar3.get((int) j).toString());
                            item2.buyCoin = Integer.parseInt(job.get((Object) "buyCoin").toString());
                            item2.buyCoinLock = Integer.parseInt(job.get((Object) "buyCoinLock").toString());
                            item2.buyGold = Integer.parseInt(job.get((Object) "buyGold").toString());
                            sell.item[j] = item2;
                        }
                    }
                    ItemSell.entrys.put((int) sell.type, sell);
                    ++i[0];
                }
                res.close();

            });

            SQLManager.executeQuery("SELECT * from npc_daily limit 1", (red) -> {
                if (red.first()) {
                    MENU_EVENT_NPC = Mapper.converter.readValue(red.getString("features"), String[].class);
                    ID_EVENT_NPC = Short.parseShort(red.getInt("id") + "");
                    EVENT_NPC_CHAT = Mapper.converter.readValue(red.getString("npc_chat"), String[].class);
                    ID_FEATURES = Mapper.converter.readValue(red.getString("features_id"), short[].class);
                } else {
                    System.out.println("Khong tim thay NPC MENU");
                }
                red.close();
            });

        } catch (Exception e) {
            util.Debug("Error i:" + i[0]);
            e.printStackTrace();
            System.exit(0);
        }
        this.loadGame();

    }

    private void loadGame() {
        int i = 0;
        try {
            i = 0;
            SQLManager.executeQuery("SELECT * FROM `clan`;", (res) -> {

                while (res.next()) {
                    final ClanManager clan = new ClanManager();
                    clan.id = Integer.parseInt(res.getString("id"));
                    clan.name = res.getString("name");
                    clan.exp = res.getInt("exp");
                    clan.setLevel(res.getInt("level"));
                    clan.itemLevel = res.getInt("itemLevel");
                    clan.coin = res.getInt("coin");
                    clan.reg_date = res.getString("reg_date");
                    clan.log = res.getString("log");
                    clan.alert = res.getString("alert");
                    clan.use_card = res.getByte("use_card");
                    clan.openDun = res.getByte("openDun");
                    clan.debt = res.getByte("debt");
                    JSONArray jar = (JSONArray) JSONValue.parse(res.getString("members"));
                    if (jar != null) {
                        for (short j = 0; j < jar.size(); ++j) {
                            final JSONArray jar2 = (JSONArray) jar.get((int) j);
                            val mem = ClanMember.fromJSONArray(jar2);
                            clan.members.add(mem);
                        }
                    }

                    try {
                        clan.clanThanThus = Mapper.converter.readValue(res.getString("clan_than_thu"),
                                new TypeReference<List<ClanThanThu>>() {
                                });
                    } catch (Exception e) {
                        clan.clanThanThus = new ArrayList<>();
                    }
                    jar = (JSONArray) JSONValue.parse(res.getString("items"));
                    if (jar != null) {
                        for (byte k = 0; k < jar.size(); ++k) {
                            clan.items.add(parseItem(jar.get((int) k).toString()));
                        }
                    }
                    clan.week = res.getString("week");
                    try {
                        clan.setClanBattleData(
                                Mapper.converter.readValue(res.getString("clan_battle_data"), ClanBattleData.class));
                    } catch (Exception e) {
                    }
                    ClanManager.entrys.add(clan);
                }
                res.close();

            });
            ItemShinwaManager.loadFromDatabase();

            SQLManager.executeUpdate("UPDATE `ninja` SET `caveID`=-1;");
        } catch (Exception e) {
            util.Debug("Error i:" + i);
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void getPackMessage(final User p) throws IOException {
        final Message m = new Message(-28);
        m.writer().writeByte(-123);
        m.writer().writeByte(this.vsData);
        m.writer().writeByte(this.vsMap);
        m.writer().writeByte(this.vsSkill);
        m.writer().writeByte(this.vsItem);
        m.writer().write(cache[5].toByteArray());
        // m.writer().writeByte(0);
        // m.writer().writeByte(0);
        // m.writer().writeByte(0);

        m.writer().flush();
        p.sendMessage(m);
        m.cleanup();
    }

    public void sendData(final User p) throws IOException {
        final Message m = new Message(-28);
        m.writer().writeByte(-122);
        m.writer().writeByte(this.vsData);
        byte[] ab = GameScr.loadFile("res/cache/data/nj_arrow").toByteArray();
        m.writer().writeInt(ab.length);
        m.writer().write(ab);
        ab = GameScr.loadFile("res/cache/data/nj_effect").toByteArray();
        m.writer().writeInt(ab.length);
        m.writer().write(ab);
        ab = GameScr.loadFile("res/cache/data/nj_image").toByteArray();
        m.writer().writeInt(ab.length);
        m.writer().write(ab);
        ab = GameScr.loadFile("res/cache/data/nj_part").toByteArray();
        m.writer().writeInt(ab.length);
        m.writer().write(ab);
        ab = GameScr.loadFile("res/cache/data/nj_skill").toByteArray();
        m.writer().writeInt(ab.length);
        m.writer().write(ab);
        m.writer().writeByte(this.tasks.length);
        for (byte i = 0; i < this.tasks.length; ++i) {
            m.writer().writeByte(this.tasks[i].length);
            for (byte j = 0; j < this.tasks[i].length; ++j) {
                m.writer().writeByte(this.tasks[i][j]);
                m.writer().writeByte(this.maptasks[i][j]);
            }
        }
        m.writer().writeByte(Level.getEntrys().size());
        for (final Level entry : Level.getEntrys()) {
            m.writer().writeLong(entry.exps);
        }
        m.writer().writeByte(GameScr.crystals.length);
        for (byte i = 0; i < GameScr.crystals.length; ++i) {
            m.writer().writeInt(GameScr.crystals[i]);
        }
        m.writer().writeByte(GameScr.upClothe.length);
        for (byte i = 0; i < GameScr.upClothe.length; ++i) {
            m.writer().writeInt(GameScr.upClothe[i]);
        }
        m.writer().writeByte(GameScr.upAdorn.length);
        for (byte i = 0; i < GameScr.upAdorn.length; ++i) {
            m.writer().writeInt(GameScr.upAdorn[i]);
        }
        m.writer().writeByte(GameScr.upWeapon.length);
        for (byte i = 0; i < GameScr.upWeapon.length; ++i) {
            m.writer().writeInt(GameScr.upWeapon[i]);
        }
        m.writer().writeByte(GameScr.coinUpCrystals.length);
        for (byte i = 0; i < GameScr.coinUpCrystals.length; ++i) {
            m.writer().writeInt(GameScr.coinUpCrystals[i]);
        }
        m.writer().writeByte(GameScr.coinUpClothes.length);
        for (byte i = 0; i < GameScr.coinUpClothes.length; ++i) {
            m.writer().writeInt(GameScr.coinUpClothes[i]);
        }
        m.writer().writeByte(GameScr.coinUpAdorns.length);
        for (byte i = 0; i < GameScr.coinUpAdorns.length; ++i) {
            m.writer().writeInt(GameScr.coinUpAdorns[i]);
        }
        m.writer().writeByte(GameScr.coinUpWeapons.length);
        for (byte i = 0; i < GameScr.coinUpWeapons.length; ++i) {
            m.writer().writeInt(GameScr.coinUpWeapons[i]);
        }
        m.writer().writeByte(GameScr.goldUps.length);
        for (byte i = 0; i < GameScr.goldUps.length; ++i) {
            m.writer().writeInt(GameScr.goldUps[i]);
        }
        m.writer().writeByte(GameScr.maxPercents.length);
        for (byte i = 0; i < GameScr.maxPercents.length; ++i) {
            m.writer().writeInt(GameScr.maxPercents[i]);
        }
        m.writer().writeByte(EffectData.entrys.size());
        for (byte i = 0; i < EffectData.entrys.size(); ++i) {
            m.writer().writeByte(EffectData.entrys.get(i).id);
            m.writer().writeByte(EffectData.entrys.get(i).type);
            m.writer().writeUTF(EffectData.entrys.get(i).name);
            m.writer().writeShort(EffectData.entrys.get(i).iconId);
        }
        m.writer().flush();
        p.sendMessage(m);
        m.cleanup();
    }

    public static void chatKTG(final String chat) throws IOException {
        final Message m = new Message(-25);
        m.writer().writeUTF(chat);
        m.writer().flush();
        PlayerManager.getInstance().NinjaMessage(m);
        m.cleanup();

        util.Debug("KTG: " + chat);
    }

    public void Infochat(final String chat) throws IOException {
        final Message m = new Message(-24);
        m.writer().writeUTF(chat);
        m.writer().flush();
        PlayerManager.getInstance().NinjaMessage(m);
        m.cleanup();
    }

    protected void stop() {
    }

    public void chatKTG(final User p, final Message m) throws IOException {
        final String chat = m.reader().readUTF();
        m.cleanup();
        if (p.chatKTGdelay > System.currentTimeMillis()) {
            p.session.sendMessageLog("Chờ sau " + (p.chatKTGdelay - System.currentTimeMillis()) / 1000L + "s.");
            return;
        }
        p.chatKTGdelay = System.currentTimeMillis() + 5000L;
        if (p.luong < 5) {
            p.session.sendMessageLog("Bạn không có đủ lượng trên người.");
            return;
        }
        p.luongMessage(-5L);
        serverChat(p.nj.name, chat);
    }

    public static void serverChat(final String name, final String s) {
        final Message m = new Message(-21);
        try {
            m.writer().writeUTF(name);
            m.writer().writeUTF(s);
            m.writer().flush();
            PlayerManager.getInstance().NinjaMessage(m);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (m != null) {
                m.cleanup();
            }
        }
    }

    public void sendTB(final User p, final String title, final String s) throws IOException {
        final Message m = new Message(53);
        m.writer().writeUTF(title);
        m.writer().writeUTF(s);
        m.writer().flush();
        p.sendMessage(m);
        m.cleanup();
    }

    public void close() {
        for (int i = 0; i < this.rotationluck.length; ++i) {
            this.rotationluck[i].close();
            this.rotationluck[i] = null;
        }
        this.rotationluck = null;
        for (int i = 0; i < Manager.server.getMaps().length; ++i) {
            val map = Manager.server.getMaps()[i];
            if (map != null) {
                Manager.server.getMaps()[i].close();
                Manager.server.getMaps()[i] = null;
            }
        }
        Manager.server.setMaps(null);
    }

    public static ByteArrayOutputStream[] cache = new ByteArrayOutputStream[6];

    static {
        Manager.server = Server.getInstance();
        cache[0] = GameScr.loadFile("res/cache/data.bin");
        // cache[1] = GameScr.loadFile("res/cache/map");
        cache[2] = GameScr.loadFile("res/cache/skill");
        // cache[3] = GameScr.loadFile("res/cache/item");
        cache[4] = GameScr.loadFile("res/cache/skillnhanban");
        cache[5] = GameScr.loadFile("res/cache/request");
    }

    public void sendMap(final User p) throws IOException {
        if (cache[1] == null) {
            cache[1] = GameScr.loadFile("cache/map");
        }
        final Message m = new Message(-28);
        m.writer().writeByte(-121);
        m.writer().write(cache[1].toByteArray());
        m.writer().flush();
        p.sendMessage(m);
        m.cleanup();
    }

    public void sendSkill(final User p) throws IOException {
        final Message m = new Message(-28);
        m.writer().writeByte(-120);
        m.writer().write(cache[2].toByteArray());
        m.writer().flush();
        p.sendMessage(m);
        m.cleanup();

    }

    public void sendItem(final User p) throws IOException {
        if (cache[3] == null) {
            cache[3] = GameScr.loadFile("cache/item");
        }
        final Message m = new Message(-28);
        m.writer().writeByte(-119);
        m.writer().write(cache[3].toByteArray());
        m.writer().flush();
        p.sendMessage(m);
        m.cleanup();

    }
}
