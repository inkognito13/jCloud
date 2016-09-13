package su.orange.jcloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

/**
 * @author Dmitry Tarasov
 *         Date: 03/05/2016
 *         Time: 00:16
 */
@Component
public class SystemEventHandler {
    
    @Autowired
    private FileProcessor fileProcessor;
    @Value("${extension.raw}")
    String rawExtension;

    private Logger logger = LoggerFactory.getLogger(SystemEventHandler.class);
    
    @Async
    public void handleSystemChangeEvent(Event event) {
        if (event.getKind().equals(ENTRY_CREATE)) {
            entryCreate(event.getPath());
        } else if (event.getKind().equals(ENTRY_DELETE)) {

        }
    }

    private void entryCreate(Path file) {
        if (FileExtensionUtil.isFileRaw(file, rawExtension)) {
            logger.debug("File {} is Raw Photo file. Converting and uploading", file);
            String thumb = fileProcessor.createThumb(file.toString());
            fileProcessor.uploadToPhotoFolder(thumb);
            fileProcessor.uploadToRawFolder(file.toString());
        } else if (Files.isDirectory(file, NOFOLLOW_LINKS)) {
            logger.debug("File {} is directory. Creating new dir on remote drive", file);
            fileProcessor.createRemoteDirectories(file.toString());
        } else {
            logger.debug("File {} is regular file. Uploading to remote drive", file);
            fileProcessor.uploadToPhotoFolder(file.toString());
            fileProcessor.uploadToRawFolder(file.toString());
        }
    }
}
