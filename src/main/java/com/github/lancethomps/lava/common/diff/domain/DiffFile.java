package com.github.lancethomps.lava.common.diff.domain;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.lancethomps.lava.common.ser.ExternalizableBean;
import com.google.common.collect.Lists;

public class DiffFile extends ExternalizableBean {

  private AtomicInteger addedLines = new AtomicInteger(0);

  private List<DiffBlock> blocks = Lists.newArrayList();

  private String changedPercentage;

  private String checksumAfter;

  private String checksumBefore;

  private boolean combined;

  private Boolean copy;

  private Boolean deleted;

  private String deletedFileMode;

  private AtomicInteger deletedLines = new AtomicInteger(0);

  private String language;

  private String mode;

  private Boolean newFile;

  private String newFileMode;

  private String newMode;

  private String newName;

  private String oldMode;

  private String oldName;

  private Boolean rename;

  @JsonIgnore
  private Map<String, Object> templateData = new HashMap<>();

  private String unchangedPercentage;

  public AtomicInteger getAddedLines() {
    return addedLines;
  }

  public void setAddedLines(AtomicInteger addedLines) {
    this.addedLines = addedLines;
  }

  public List<DiffBlock> getBlocks() {
    return blocks;
  }

  public void setBlocks(List<DiffBlock> blocks) {
    this.blocks = blocks;
  }

  public String getChangedPercentage() {
    return changedPercentage;
  }

  public void setChangedPercentage(String changedPercentage) {
    this.changedPercentage = changedPercentage;
  }

  public String getChecksumAfter() {
    return checksumAfter;
  }

  public void setChecksumAfter(String checksumAfter) {
    this.checksumAfter = checksumAfter;
  }

  public String getChecksumBefore() {
    return checksumBefore;
  }

  public void setChecksumBefore(String checksumBefore) {
    this.checksumBefore = checksumBefore;
  }

  public Boolean getCopy() {
    return copy;
  }

  public void setCopy(Boolean copy) {
    this.copy = copy;
  }

  public Boolean getDeleted() {
    return deleted;
  }

  public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
  }

  public String getDeletedFileMode() {
    return deletedFileMode;
  }

  public void setDeletedFileMode(String deletedFileMode) {
    this.deletedFileMode = deletedFileMode;
  }

  public AtomicInteger getDeletedLines() {
    return deletedLines;
  }

  public void setDeletedLines(AtomicInteger deletedLines) {
    this.deletedLines = deletedLines;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public Boolean getNewFile() {
    return newFile;
  }

  public void setNewFile(Boolean newFile) {
    this.newFile = newFile;
  }

  public String getNewFileMode() {
    return newFileMode;
  }

  public void setNewFileMode(String newFileMode) {
    this.newFileMode = newFileMode;
  }

  public String getNewMode() {
    return newMode;
  }

  public void setNewMode(String newMode) {
    this.newMode = newMode;
  }

  public String getNewName() {
    return newName;
  }

  public void setNewName(String newName) {
    this.newName = newName;
  }

  public String getOldMode() {
    return oldMode;
  }

  public void setOldMode(String oldMode) {
    this.oldMode = oldMode;
  }

  public String getOldName() {
    return oldName;
  }

  public void setOldName(String oldName) {
    this.oldName = oldName;
  }

  public Boolean getRename() {
    return rename;
  }

  public void setRename(Boolean rename) {
    this.rename = rename;
  }

  public Map<String, Object> getTemplateData() {
    return templateData;
  }

  public void setTemplateData(Map<String, Object> templateData) {
    this.templateData = templateData;
  }

  public String getUnchangedPercentage() {
    return unchangedPercentage;
  }

  public void setUnchangedPercentage(String unchangedPercentage) {
    this.unchangedPercentage = unchangedPercentage;
  }

  public boolean isCombined() {
    return combined;
  }

  public void setCombined(boolean combined) {
    this.combined = combined;
  }

  public boolean testCopy() {
    return toBoolean(copy);
  }

  public boolean testDeleted() {
    return toBoolean(deleted);
  }

  public boolean testNewFile() {
    return toBoolean(newFile);
  }

  public boolean testRename() {
    return toBoolean(rename);
  }

}
