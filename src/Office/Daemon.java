package Office;
import java.util.*;

public class Daemon {
    private int minInstances;
    private int maxInstances;
    private ArrayList<Instance> ready = new ArrayList<>();
    public Set<Instance> instances = new HashSet<>();
    long spikeTimer = 0;
    int spikeCounter = 0;

    public Daemon(int minInstances, int maxInstances) {
        this.minInstances = minInstances;
        this.maxInstances = maxInstances;
        (new Thread(this::run)).start();
    }

    private void run () {
        while (true) {
            try {
                _run();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void _run () {
        ArrayList<Instance> _ready = new ArrayList<>();
        Iterator<Instance> iterator = instances.iterator();

        while (iterator.hasNext()) {
            Instance instance = iterator.next();

            switch (instance.getState()) {
                case DRAFT:
                    instance.start();
                    break;
                case IDLE:
                    instance.connect();
                    break;
                case READY:
                    _ready.add(instance);
                    break;
                case TRASH:
                    if (instance.terminate())
                        iterator.remove();
                    break;
            }
        }

        // consider each instance could handle 10 tasks in 30s
        int targetInstances = Math.min(
                maxInstances,
                Math.max(
                        minInstances,
                        (int) spikeCounter / 5
                )
        );

        if (targetInstances > instances.size())
            instances.add(new Instance());

        _ready.sort(Comparator.comparing(Instance::getHealth));
        ready = _ready;

        long now = System.currentTimeMillis();
        if (spikeTimer + 30000 < now) {
            spikeTimer = now;
            spikeCounter = 0;
        }
    }

    public Instance getInstance () {
        spikeCounter++;
        try {
            Instance instance = ready.get(0);
            instance.attach();
            return instance;
        } catch (Throwable e) {
            return null;
        }
    }
}
