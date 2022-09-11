package tournament;

import com.fasterxml.jackson.core.type.TypeReference;
import org.jetbrains.annotations.NotNull;
import patch.Mapper;
import real.Ninja;
import threading.Manager;
import threading.Server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class KageTournament extends Tournament {

    private static volatile Tournament instance;
    public int[][] KAGE_REWARDS;

    protected KageTournament() {
        super(100, 2);
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(Paths.get("application.properties"))) {
            properties.load(inputStream);
            KAGE_REWARDS = Mapper.converter.readValue(properties.getProperty("reward-thien-bang"), new TypeReference<int[][]>() {
            });
        } catch (Exception e) {
            Server.getInstance().stop();
            System.out.println("TOURNAMENT ERR");
        }
    }

    public static Tournament gi() {
        if (instance == null) {
            synchronized (KageTournament.class) {
                if (instance == null) {
                    instance = new KageTournament();
                }
            }
        }
        return instance;
    }

    @Override
    public @NotNull Ninja findNinjaById(int ninjaId) {
        return this.participants.stream().filter(p-> p.nj.id == ninjaId).map(p-> p.nj).findFirst().orElse(null);
    }

    @Override
    public int[][] getRewardItems() {
        return KAGE_REWARDS;
    }

    @Override
    public void notifyKTG(String name) throws IOException {
        Manager.chatKTG("Người chơi " + name + " đã chiến thắng và giành hạng 1 thiên bảng.");
    }
}
