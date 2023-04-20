package Office;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

// should keep a queue of tasks to execute
// when a client requests conversion, push the task to a queue
// a loop should go through
public class Daemon {
    final int TIMEOUT = 60000;
    int minInstances = 0;
    ArrayList<Instance> ready = new ArrayList<>();
    public Set<Instance> instances = new HashSet<>();
    long lastStart = 0;

    public Daemon (int minInstances) {
        this.minInstances = minInstances;
        Thread loop = new Thread(this::run);
        loop.start();
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
        long now = System.currentTimeMillis();
        ArrayList<Instance> _ready = new ArrayList<>();

        if (instances.size() < minInstances) {
            int missing = minInstances - instances.size();
            for (int i = 0; i < missing; i++) {
                instances.add(new Instance());
            }
        }

        Iterator<Instance> iterator = instances.iterator();
        int notBusy = 0;

        while (iterator.hasNext()) {
            Instance instance = iterator.next();

            if (instance.state != State.STOPPED && (now - instance.lastActivity) > TIMEOUT) {
                instance.kill();
                ready.remove(instance);
                iterator.remove();
                System.err.println("Killed - RUDY"); // R U DEAD YET? NOT RESPONDING
                continue;
            }

            if (instance.state == State.DEAD) {
                instance.kill();
                iterator.remove();
                continue;
            }

            if (instance.state == State.READY) {
                notBusy++;
                _ready.add(instance);
                continue;
            }

            if (instance.state == State.STOPPED) {
                notBusy++;
                // AVOIDING CONCURRENT SPAWNS
                if (now - lastStart > 5000) {
                    lastStart = now;
                    instance.start();
                }
                continue;
            }

            if (instance.state == State.STARTED) {
                notBusy++;
                if (now - instance.lastActivity > 1000) {
                    instance.connect();
                }
            }
        }

        // SHIT WILL HAPPEN IF NOTHING HAPPENS ABOVE
        // AND THIS IS EXECUTED REPEATEDLY
        if (notBusy < (minInstances / 2) && instances.size() < minInstances * 2) {
            Instance tempInstance = new Instance();
            instances.add(tempInstance);
            System.out.println("ADDING 1 INSTANCE");
        }

        ready = _ready;
    }
    public Instance getInstance () {
        try {
            Instance instance = ready.remove(0);
            instance.lock();
            return instance;
        } catch (Throwable e) {
            return null;
        }
    }
}
