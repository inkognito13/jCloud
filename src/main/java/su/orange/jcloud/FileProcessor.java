package su.orange.jcloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

/**
 * @author Dmitry Tarasov
 *         Date: 03/22/2016
 *         Time: 11:06
 */
@Component
public class FileProcessor {
    
    @Autowired
    private ThumbMaker thumbMaker;
    @Autowired
    private RemoteDriveManager remoteDriveManager;
    @Value("${folder.local.base}")
    String baseLocalFolder;
    @Value("${folder.remote.base}")
    String baseRemoteFolder;
    @Value("${folder.local.thumb}")
    String thumbLocalFolder;
    @Value("${folder.remote.backup}")
    String backupRemoteFolder;
    @Value("${extension.raw}")
    String rawExtension;
    @Value("${extension.thumb}")
    String thumbExtension;

    private Logger logger = LoggerFactory.getLogger(FileProcessor.class);

    public FileProcessor() {
    }

    public String createThumb(String rawFileName) {
        String thumbFileName = convert(rawFileName);
        if (thumbFileName != null) {
            logger.debug("Thumb {} is created", thumbFileName);
        }
        return thumbFileName;
    }
    
    public void uploadToPhotoFolder(String localFileName) {
        uploadToPhotoFolder(localFileName,null);
    }
    
    public void uploadToPhotoFolder(String localFileName, String excludeEnding) {
        String remoteDir = getFolder(localFileName).replace(baseLocalFolder, baseRemoteFolder)
                .replace(thumbLocalFolder, baseRemoteFolder);
        logger.debug("Uploading file {} to remote drive PHOTO folder {}", localFileName, remoteDir);
        boolean fileUploaded = remoteDriveManager.uploadFile(localFileName, remoteDir, excludeEnding);
        if (fileUploaded) {
            logger.debug("File {} successfully uploaded to remote drive PHOTO folder {}", localFileName, remoteDir);
        } else {
            logger.error("Thumb {} NOT uploaded to remote drive PHOTO folder {}", localFileName, remoteDir);
        }
    }

    public void uploadToRawFolder(String localFileName) {
        uploadToRawFolder(localFileName,null);
    }
    
    public void uploadToRawFolder(String localFileName, String excludeEnding) {
        String remoteDir = getFolder(localFileName).replace(baseLocalFolder, backupRemoteFolder);
        boolean fileUploaded = remoteDriveManager.uploadFile(localFileName, remoteDir, excludeEnding);
        if (fileUploaded) {
            logger.debug("File {} successfully uploaded to remote drive RAW folder {}", localFileName, remoteDir);
        } else {
            logger.error("Regular file {} NOT uploaded to remote drive RAW folder {}", localFileName, remoteDir);
        }
    }

    private String convert(String rawFileName) {
        String thumbFileName = rawFileName.replace(baseLocalFolder, thumbLocalFolder)
                .replace(rawExtension, thumbExtension)
                .replace(rawExtension.toLowerCase(), thumbExtension);
        boolean thumbCreated = thumbMaker.createThumb(rawFileName, thumbFileName);
        if (thumbCreated) {
            return thumbFileName;
        } else {
            return null;
        }
    }

    public void createRemoteDirectories(String localDir) {
        String remoteRawDir = localDir.replace(baseLocalFolder, backupRemoteFolder);
        boolean success = remoteDriveManager.createDir(remoteRawDir);
        if (success) {
            logger.debug("Remote RAW directory {} is created", remoteRawDir);
        } else {
            logger.error("Remote RAW directory is NOT created");
            return;
        }

        String remotePhotosDir = localDir.replace(baseLocalFolder, baseRemoteFolder);
        success = remoteDriveManager.createDir(remotePhotosDir);
        if (success) {
            logger.debug("Remote Photos directory {} is created", remoteRawDir);
        } else {
            logger.error("Remote Photos directory is NOT created");
        }
    }

    private String getFolder(String fileName) {
        return Paths.get(fileName).getParent().toString();
    }
}
