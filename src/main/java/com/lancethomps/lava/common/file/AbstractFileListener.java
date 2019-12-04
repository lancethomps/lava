package com.lancethomps.lava.common.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.lancethomps.lava.common.Checks;
import com.lancethomps.lava.common.lambda.ThrowingConsumer;
import com.lancethomps.lava.common.logging.Logs;

public abstract class AbstractFileListener implements Listener {

  private static final Logger LOG = Logger.getLogger(AbstractFileListener.class);

  private String baseDir;

  private List<ThrowingConsumer<File>> fileLoadCallbacks = new ArrayList<>();

  private ListenerConfiguration listenerConfiguration;

  public void addFileLoadCallback(final ThrowingConsumer<File> callback) {
    synchronized (fileLoadCallbacks) {
      fileLoadCallbacks.add(callback);
    }
  }

  public void afterBaseDirSet() throws Exception {

  }

  public String getBaseDir() {
    return baseDir;
  }

  public void setBaseDir(String baseDir) {
    this.baseDir = baseDir;
  }

  @Override
  public List<ThrowingConsumer<File>> getFileLoadCallbacks() {
    return fileLoadCallbacks;
  }

  public ListenerConfiguration getListenerConfiguration() {
    return listenerConfiguration;
  }

  public void setListenerConfiguration(ListenerConfiguration listenerConfiguration) {
    this.listenerConfiguration = listenerConfiguration;
  }

  @Override
  public void handleFileDelete(File file) {
  }

  @Override
  public void handleFileLoad(File file) {
    Logs.logError(
      LOG,
      new Exception(),
      "Listeners should overwrite the handleFileLoad method! The file [%s] was not loaded due to no available method.",
      file
    );
  }

  public void loadAllFilesInDir(File dir) {
    if ((dir != null) && dir.exists() && dir.isDirectory()) {
      File[] files = dir.listFiles();
      if ((files != null) && (files.length > 0)) {
        Stream
          .of(files)
          .filter(File::isFile)
          .peek(file -> Logs.logWarn(LOG, "Loading file [%s] with listener [%s]", getRelativePathForFile(file), getClass().getSimpleName()))
          .forEach(
            this::handleFileLoad
          );
        if (listenerConfiguration.isIncludeSubDirs()) {
          Stream.of(files).filter(File::isDirectory).forEach(this::loadAllFilesInDir);
        }
      }
    }
  }

  private String getRelativePathForFile(File file) {
    if (Checks.isNotBlank(baseDir)) {
      return StringUtils.removeStart(FileUtil.fullPath(file), baseDir + File.separatorChar);
    }
    return file.getName();
  }

}
