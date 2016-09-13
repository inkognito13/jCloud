package su.orange.jcloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Example to watch a directory (or tree) for changes to files.
 */

@Component
public class MonitorService {
    
    @Autowired
    SystemEventHandler systemEventHandler;
    private WatchService watcher;
    private Map<WatchKey, Path> keys;
    private boolean trace = false;
    @Value("${folder.local.base}")
    String baseLocalFolder;
    @Value("${recursive}")
    Boolean recursive;
    
    

    private Logger logger = LoggerFactory.getLogger(MonitorService.class);
    /**
     * Creates a WatchService and registers the given directory
     */
    @PostConstruct
    void init() throws IOException{
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey, Path>();
        Path base = Paths.get(baseLocalFolder);

        if (recursive) {
            logger.debug("Scanning {} ", baseLocalFolder);
            registerAll(base);
            logger.debug("Done scanning {}", baseLocalFolder);
        } else {
            register(base);
        }

        // enable trace after initial registration
        this.trace = true;
    } 

    /**
     * Process all events for keys queued to the watcher
     */
    @Scheduled(initialDelay = 1000,fixedRate = 1000)
    void processEvents() {
        List<Event> events = getEvents();
        if (events!=null){
            submitEvents(events);
        }
    }
    
    private List<Event> getEvents(){
        List<Event> events = null;
        // wait for key to be signalled
        WatchKey key = watcher.poll();
        if (key==null){
            return null;
        }
        

        Path dir = keys.get(key);
        if (dir == null) {
            logger.error("WatchKey not recognized!!");
            return null;
        }

        for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind kind = event.kind();

            // TBD - provide example of how OVERFLOW event is handled
            if (kind == OVERFLOW) {
                continue;
            }

            // Context for directory entry event is the file name of entry
            WatchEvent<Path> ev = cast(event);
            Path name = ev.context();
            Path child = dir.resolve(name);

            // print out event
            logger.debug("{}: {}", event.kind().name(), child);

            // if directory is created, and watching recursively, then
            // register it and its sub-directories
            if (recursive && (kind == ENTRY_CREATE)) {
                try {
                    if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                        registerAll(child);
                    }
                } catch (IOException x) {
                    // ignore to keep sample readbale
                }
            }
            
            if (events==null){
                events = new ArrayList<>();
            }
            events.add(new Event(child, kind));
        }

        // reset key and remove from set if directory no longer accessible
        boolean valid = key.reset();
        if (!valid) {

            keys.remove(key);

            if (dir.toFile().exists()){
                try {
                    registerAll(dir);
                }catch (IOException x){

                }
            }

            // all directories are inaccessible
            if (keys.isEmpty()) {
                return null;
            }
        }
        return events;
    }
    
    private void submitEvents(List<Event> events){
        for (Event event:events){
            systemEventHandler.handleSystemChangeEvent(event);
        }
    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        logger.debug("register: {}", dir);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                logger.debug("register {} as new folder", dir);
            } else {
                if (!dir.equals(prev)) {
                    logger.debug("update: {} -> {}", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    public Map<WatchKey, Path> getKeys(){
        return keys;    
    }

}
