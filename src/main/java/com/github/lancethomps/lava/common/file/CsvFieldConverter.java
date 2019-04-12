package com.github.lancethomps.lava.common.file;

@FunctionalInterface
public interface CsvFieldConverter {

  Object convertObject(Object orig, String description) throws Exception;

}
