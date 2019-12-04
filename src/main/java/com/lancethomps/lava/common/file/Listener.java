package com.lancethomps.lava.common.file;

import java.io.File;
import java.util.List;

import com.lancethomps.lava.common.lambda.ThrowingConsumer;

public interface Listener {

  default List<ThrowingConsumer<File>> getFileLoadCallbacks() {
    return null;
  }

  void handleFileDelete(File file);

  void handleFileLoad(File file);

}
