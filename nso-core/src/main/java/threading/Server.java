package threading;

import java.lang.Exception;
import io.sentry.Sentry;
import io.Session;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import battle.GBattle;
import patch.Resource;
import candybattle.CandyBattleManager;
import interfaces.IBattle;
import tournament.GeninTournament;
import tournament.KageTournament;
import tournament.Tournament;
import real.*;
import server.*;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.Instant;
import java.util.stream.Collectors;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static interfaces.IBattle.*;
import java.net.InetSocketAddress;
import static threading.Manager.MOMENT_REFRESH_BATTLE;

public class Server {
    public static long TIME_SLEEP_SHINWA_THREAD;
    private static Server instance;
    private static Runnable updateBattle;
    public IBattle globalBattle;
    private ServerSocket listenSocket;
    public volatile static boolean start;
    public Manager manager;

    @NotNull
    public MenuController menu;
    public ServerController controllerManager;
    public Controller serverMessageHandler;
    private static Map[] maps;
    // public static short[] MOMENT_BOSS_REFRESH;
    public static short MAX_BOSS;
    public static short DURATION_TIME_BOSS_REFRESH;
    private static boolean isRefreshBoss;
    private static final short[] mapBossVDMQ;
    private static final short[] mapBoss45;
    private static final short[] mapBoss55;
    private static final short[] mapBoss65;
    private static final short[] mapBoss75;
    public static final short[] endMaps;

    public static Runnable notifyHourlyTips;

    public static Runnable updateRefreshBoss;
    public static ExecutorService executorService = Executors.newFixedThreadPool(5);
    public static ClanTerritoryManager clanTerritoryManager = new ClanTerritoryManager();
    public static Tournament kageTournament;
    public static Tournament geninTournament;
    public static java.util.Map<String, Resource> resource;
    public DaemonThread daemonThread;

    public static String[] HOURLY_TIPS = new String[] {
            "Khuyến mại nạp lượng 100% bằng card tự động npc Okanechan và liên hệ admin nếu nạp bằng momo/atm với tỷ lệ ưu đãi hơn.",
            "Chiến trường sẽ bắt đầu mở vào 16h và 21h hàng ngày.",
            "Nhận quà tân thủ tại NPC Vua Hùng.",
            "Cởi thú cưỡi và pet sau khi đánh Boss để nhặt rìu khi làm nhiệm vụ nhặt rìu.",
            "Kiếm lượng bằng cách làm nvhn, nvct, săn TA/TL/Boss hoặc dùng Phúc nang nhẫn giả.",
            "Tử Hạ Ma Thần sẽ xuất hiện tại khu 17 ở map cuối của các trường.",
            "Thần thú sẽ xuất hiện vào khung giờ chẵn. Hãy theo dõi để săn và nhận được nhiều vật phẩm ý nghĩa.",
            "Nếu gặp lỗi hiển thị với phiên bản hiện tại vui lòng xoá game và truy cập http://nso-ms.tk để download phiên bản mới.",
            "Nếu bị lỗi hiển thị nhiệm vụ tìm bảo vật lv34 hãy huỷ nhiệm vụ và nhận lại để tiếp tục tại map rừng kappa",
            "Mua phiếu may mắn lật hình để có cơ hội nhận đc bát bảo,rương bạch ngân,rương huyền bí và sói vĩnh viễn.",
            "Tham gia lật hình, làm nvhn, nvdv và săn boss trên KTG để có thể nhận đc mảnh Jrai/Jumito."
    };

    @NotNull
    public static CandyBattleManager candyBattleManager;

    public Server() {
        this.listenSocket = null;
        resource = new ConcurrentHashMap<>();
    }

    private void init() {
        this.manager = new Manager();
        this.menu = new MenuController();
        this.controllerManager = new RealController();
        this.serverMessageHandler = new Controller();
        this.globalBattle = new GBattle();
        Server.kageTournament = KageTournament.gi();
        Server.geninTournament = GeninTournament.gi();
        Server.candyBattleManager = new CandyBattleManager();
        updateRefreshBoss = () -> {

            synchronized (ClanManager.entrys) {
                for (int i = ClanManager.entrys.size() - 1; i >= 0; --i) {
                    final ClanManager clan = ClanManager.entrys.get(i);
                    if (util.compare_Week(Date.from(Instant.now()), util.getDate(clan.week))) {
                        clan.payfeesClan();
                    }
                }
            }

            final Calendar rightNow = Calendar.getInstance();
            final short moment = (short) rightNow.get(Manager.BOSS_WAIT_TIME_UNIT);
            if (moment % Server.DURATION_TIME_BOSS_REFRESH == 0) {
                if (!Server.isRefreshBoss) {
                    StringBuilder textchat = new StringBuilder("Thần thú đã suất hiện tại");
                    for (byte k = 0; k < util.nextInt(Server.MAX_BOSS); ++k) {
                        final Map map = Manager.getMapid(Server.mapBoss75[util.nextInt(Server.mapBoss75.length)]);
                        if (map != null) {
                            map.refreshBoss(util.debug ? 0 : util.nextInt(1, 17));
                            textchat.append(" ").append(map.template.name);
                            Server.isRefreshBoss = true;
                        }
                    }
                    for (byte k = 0; k < util.nextInt(Server.MAX_BOSS); ++k) {
                        final Map map = Manager.getMapid(Server.mapBoss65[util.nextInt(Server.mapBoss65.length)]);
                        if (map != null) {
                            map.refreshBoss(util.debug ? 0 : util.nextInt(1, 17));
                            textchat.append(", ").append(map.template.name);
                            Server.isRefreshBoss = true;
                        }
                    }
                    for (byte k = 0; k < util.nextInt(Server.MAX_BOSS); ++k) {
                        final Map map = Manager.getMapid(Server.mapBoss55[util.nextInt(Server.mapBoss55.length)]);
                        if (map != null) {
                            map.refreshBoss(util.debug ? 0 : util.nextInt(1, 17));
                            textchat.append(", ").append(map.template.name);
                            Server.isRefreshBoss = true;
                        }
                    }
                    for (byte k = 0; k < util.nextInt(Server.MAX_BOSS); ++k) {
                        final Map map = Manager.getMapid(Server.mapBoss45[util.nextInt(Server.mapBoss45.length)]);
                        if (map != null) {
                            map.refreshBoss(util.debug ? 0 : util.nextInt(1, 17));
                            textchat.append(", ").append(map.template.name);
                            Server.isRefreshBoss = true;
                        }
                    }
                    for (byte k = 0; k < Server.mapBossVDMQ.length; ++k) {
                        final Map map = Manager.getMapid(Server.mapBossVDMQ[k]);
                        if (map != null) {
                            map.refreshBoss(util.debug ? 0 : util.nextInt(1, 17));
                            textchat.append(", ").append(map.template.name);
                            Server.isRefreshBoss = true;
                        }
                    }

                    for (short i : mapBossLC) {
                        val map = Manager.getMapid(i);
                        if (map != null) {
                            map.refreshBoss(util.nextInt(1, 3));
                            textchat.append(", ").append(map.template.name);
                            Server.isRefreshBoss = true;
                        }
                    }
                    try {
                        Manager.chatKTG(textchat.toString());
                    } catch (IOException e) {
                    }
                }
            } else {
                Server.isRefreshBoss = false;
            }
        };
        updateBattle = () -> {
            final Calendar rightNow = Calendar.getInstance();
            final short moment = (short) rightNow.get(Manager.BOSS_WAIT_TIME_UNIT);
            for (int i = 0; i < MOMENT_REFRESH_BATTLE.length; i++) {

                if (MOMENT_REFRESH_BATTLE[i] == moment) {
                    if (this.globalBattle.getState() == INITIAL_STATE) {
                        this.globalBattle.reset();
                        this.globalBattle.setState(WAITING_STATE);
                    }
                    long second = Server.this.globalBattle.getTimeInSeconds();
                    if (second > 0 && this.globalBattle.getState() == WAITING_STATE) {
                        int phut = (int) (second / 60);
                        Manager.serverChat("Server", "Chiến trường sẽ bắt đầu trong " + (phut > 0 ? phut : second)
                                + (phut > 0 ? " phút" : " giây"));
                    }
                }

            }
        };

        notifyHourlyTips = () -> {
            final Calendar rightNow = Calendar.getInstance();
            final short minutes = (short) rightNow.get(12);

            if (minutes % 30 == 0) {
                Manager.serverChat("Tips", HOURLY_TIPS[util.nextInt(HOURLY_TIPS.length)]);
            }

        };

        clanTerritoryManager.start();

        initSentry();
    }

    private void initSentry() {
        Sentry.init(options -> {
            options.setDsn("https://cc4ba987408a4dba8910a8b52e98118b@o1112665.ingest.sentry.io/6142463");
            // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance
            // monitoring.
            // We recommend adjusting this value in production.
            options.setTracesSampleRate(1.0);
            // When first trying Sentry it's good to see what the SDK is doing:
            options.setDebug(util.debug);
            // set environment
            options.setEnvironment(util.debug ? "developemnt" : "production");
        });
    }

    private static final Object MUTEX = new Object();

    public static Server getInstance() {
        if (Server.instance == null) {
            synchronized (MUTEX) {
                (Server.instance = new Server()).init();
            }
            instance.daemonThread = new DaemonThread();
            BXHManager.init();
            TopEventManager.init();
            instance.daemonThread.addRunner(Server.updateRefreshBoss);
            instance.daemonThread.addRunner(Server.updateBattle);
            instance.daemonThread.addRunner(Server.notifyHourlyTips);
        }
        return Server.instance;
    }

    public static boolean threadRunning = true;
    public static Thread t;

    public static void main(final String[] args) {
        Server.start = true;
        getInstance().run();

        // Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        // threadRunning = false;
        // if (t != null) {
        // t.interrupt();
        // }
        // }));
        // t = new Thread(() -> {
        // while (threadRunning) {
        //
        // OperatingSystemMXBean osBean =
        // ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        // int pt = (int) (osBean.getProcessCpuLoad() * 100);
        // if (pt > 80) {
        // getInstance().stop();
        // getInstance().run();
        // }
        //
        // }
        // });
        // t.start();
        try {
            t.join();
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void run() {
        this.setMaps(new Map[MapTemplate.arrTemplate.length]);
        for (short i = 0; i < Server.maps.length; ++i) {
            Server.maps[i] = new Map(i, null);
        }

        this.listenSocket = null;
        try {
            this.listenSocket = new ServerSocket(this.manager.PORT);
            System.out.println("Listenning port " + this.manager.PORT);

            // try {
            // // if (!util.debug) {
            // Naming.rebind("rmi://127.0.0.1:16666/tinhtoan", new RmiRemoteImpl());
            // // }
            // System.out.println("Start rmi success");
            // } catch (Exception e) {
            // System.out.println("Start rmi fail");
            // }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("RUN HOOK");
                for (Session conn : PlayerManager.getInstance().conns) {
                    if (conn != null && conn.user != null) {
                        conn.user.flush();
                    }
                }
                for (ClanManager entry : ClanManager.entrys) {
                    entry.flush();
                }
                System.out.println("CLOSE SERVER");
                stop();

            }));

            while (Server.start) {
                List<String> blackListIps = util.ReadBlackListIps().stream().map(ip -> ip.getName()).collect(
                        Collectors.toList());

                final Socket clientSocket = this.listenSocket.accept();
                InetSocketAddress socketAddress = (InetSocketAddress) clientSocket.getRemoteSocketAddress();
                String clientIpAddress = socketAddress.getAddress().getHostAddress();

                // Close socket by ghost ip address
                if (blackListIps.contains(clientIpAddress)) {
                    clientSocket.close();
                }

                // check ghost connections
                if (PlayerManager.getInstance().checkGhostIpAddress(clientIpAddress)) {
                    if (!blackListIps.contains(clientIpAddress)) {
                        blackListIps.add(clientIpAddress);
                        util.WriteBlackListIps(clientIpAddress);
                    }
                }

                // Remove non user session
                PlayerManager.getInstance().kickGhostSessionByIds(blackListIps);

                if (PlayerManager.getInstance().check(clientIpAddress)
                        && !blackListIps.contains(clientIpAddress)) {

                    final Session conn = new Session(clientSocket, this.serverMessageHandler);
                    PlayerManager.getInstance().put(conn);
                    conn.start();
                    System.out.println("Accept socket size :" + PlayerManager.getInstance().conns_size());
                    System.out.println("Client IP address:  " + clientIpAddress + " with "
                            + PlayerManager.getInstance().conns_size(clientIpAddress) + " connections.");
                    System.out.println("Real clients: " + PlayerManager.getInstance().clients_size());
                } else {
                    clientSocket.close();
                }
            }
        } catch (BindException bindEx) {
            System.exit(0);
        } catch (SocketException genEx) {
            System.out.println("Socket Closed");
        } catch (IOException e) {
            System.out.println("IO EXCEPTION");
        }
        try {
            if (this.listenSocket != null) {
                this.listenSocket.close();
            }
            util.Debug("Close server socket");
        } catch (Exception ex) {
        }

    }

    public void stop() {
        if (Server.start) {
            Server.start = false;
            try {
                Tournament.closeAll();
            } catch (Exception e) {

            }
            try {
                Server.candyBattleManager.close();
            } catch (Exception e) {

            }
            try {
                this.listenSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            try {
                ClanManager.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                this.daemonThread.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                executorService.shutdown();
                if (executorService.awaitTermination(300, TimeUnit.MILLISECONDS)) {
                    System.out.println("CLOSE");
                }
            } catch (Exception e) {
            }

            try {
                if (executorService.awaitTermination(300, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (Exception e) {
                executorService.shutdownNow();
                e.printStackTrace();
            }

            try {
                if (executorService.isShutdown()) {
                    util.Debug("Shut down executor success");
                    executorService = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                PlayerManager.getInstance().Clear();
                PlayerManager.getInstance().close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                this.manager.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Server.clanTerritoryManager.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.manager = null;
            this.menu = null;
            this.controllerManager = null;
            this.serverMessageHandler = null;

            try {
                SQLManager.close();
            } catch (Exception e) {

            }

            System.gc();
        }
    }

    private static final short[] mapBossLC;

    static {
        Server.instance = null;
        Server.start = false;
        isRefreshBoss = false;
        mapBossVDMQ = new short[] { 141, 142, 143, 146, 147 };
        mapBoss45 = new short[] { 14, 15, 16, 34, 35, 52, 68 };
        mapBoss55 = new short[] { 44, 67 };
        mapBoss65 = new short[] { 24, 41, 45, 59 };
        mapBoss75 = new short[] { 18, 36, 54 };
        mapBossLC = new short[] { 134, 135, 136, 137 };
        endMaps = new short[] { 37, 55, 58 };
    }

    public Map[] getMaps() {
        return Server.maps;
    }

    public void setMaps(Map[] maps) {
        Server.maps = maps;
    }

    public static Map getMapById(int i) {
        return maps[i];
    }

    public static boolean checkAllBossVIPIsDie() {
        for (int i = 0; i < Server.endMaps.length; i++) {
            Map map = Server.getMapById(Server.endMaps[i]);
            if (map.hasBossVIPIsLive()) {
                return false;
            }
        }

        return true;
    }

    public static void refreshAllBossVIPTimeout() {
        for (int i = 0; i < Server.endMaps.length; i++) {
            Map map = Server.getMapById(Server.endMaps[i]);
            map.refreshBossVIPTimeout();
        }
    }
}
