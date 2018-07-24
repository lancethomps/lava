package com.github.lancethomps.lava.common.diff.domain;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.lancethomps.lava.common.ser.ExternalizableBean;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

/**
 * The Class DiffedFile.
 */
public class DiffFile extends ExternalizableBean {

	/** The added lines. */
	private AtomicInteger addedLines = new AtomicInteger(0);

	/** The blocks. */
	private List<DiffBlock> blocks = Lists.newArrayList();

	/** The changed percentage. */
	private String changedPercentage;

	/** The checksum after. */
	private String checksumAfter;

	/** The checksum before. */
	private String checksumBefore;

	/** The combined. */
	private boolean combined;

	/** The copy. */
	private Boolean copy;

	/** The deleted. */
	private Boolean deleted;

	/** The deleted file mode. */
	private String deletedFileMode;

	/** The deleted lines. */
	private AtomicInteger deletedLines = new AtomicInteger(0);

	/** The language. */
	private String language;

	/** The mode. */
	private String mode;

	/** The new file. */
	private Boolean newFile;

	/** The new file mode. */
	private String newFileMode;

	/** The new mode. */
	private String newMode;

	/** The new name. */
	private String newName;

	/** The old mode. */
	private String oldMode;

	/** The old name. */
	private String oldName;

	/** The rename. */
	private Boolean rename;

	/** The template data. */
	@JsonIgnore
	private Map<String, Object> templateData = new HashMap<>();

	/** The unchanged percentage. */
	private String unchangedPercentage;

	/**
	 * Gets the added lines.
	 *
	 * @return the addedLines
	 */
	public AtomicInteger getAddedLines() {
		return addedLines;
	}

	/**
	 * Gets the blocks.
	 *
	 * @return the blocks
	 */
	public List<DiffBlock> getBlocks() {
		return blocks;
	}

	/**
	 * Gets the changed percentage.
	 *
	 * @return the changedPercentage
	 */
	public String getChangedPercentage() {
		return changedPercentage;
	}

	/**
	 * Gets the checksum after.
	 *
	 * @return the checksumAfter
	 */
	public String getChecksumAfter() {
		return checksumAfter;
	}

	/**
	 * Gets the checksum before.
	 *
	 * @return the checksumBefore
	 */
	public String getChecksumBefore() {
		return checksumBefore;
	}

	/**
	 * Gets the copy.
	 *
	 * @return the copy
	 */
	public Boolean getCopy() {
		return copy;
	}

	/**
	 * Gets the deleted.
	 *
	 * @return the deleted
	 */
	public Boolean getDeleted() {
		return deleted;
	}

	/**
	 * Gets the deleted file mode.
	 *
	 * @return the deletedFileMode
	 */
	public String getDeletedFileMode() {
		return deletedFileMode;
	}

	/**
	 * Gets the deleted lines.
	 *
	 * @return the deletedLines
	 */
	public AtomicInteger getDeletedLines() {
		return deletedLines;
	}

	/**
	 * Gets the language.
	 *
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Gets the mode.
	 *
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * Gets the new file.
	 *
	 * @return the newFile
	 */
	public Boolean getNewFile() {
		return newFile;
	}

	/**
	 * Gets the new file mode.
	 *
	 * @return the newFileMode
	 */
	public String getNewFileMode() {
		return newFileMode;
	}

	/**
	 * Gets the new mode.
	 *
	 * @return the newMode
	 */
	public String getNewMode() {
		return newMode;
	}

	/**
	 * Gets the new name.
	 *
	 * @return the newName
	 */
	public String getNewName() {
		return newName;
	}

	/**
	 * Gets the old mode.
	 *
	 * @return the oldMode
	 */
	public String getOldMode() {
		return oldMode;
	}

	/**
	 * Gets the old name.
	 *
	 * @return the oldName
	 */
	public String getOldName() {
		return oldName;
	}

	/**
	 * Gets the rename.
	 *
	 * @return the rename
	 */
	public Boolean getRename() {
		return rename;
	}

	/**
	 * @return the templateData
	 */
	public Map<String, Object> getTemplateData() {
		return templateData;
	}

	/**
	 * Gets the unchanged percentage.
	 *
	 * @return the unchangedPercentage
	 */
	public String getUnchangedPercentage() {
		return unchangedPercentage;
	}

	/**
	 * Checks if is combined.
	 *
	 * @return the combined
	 */
	public boolean isCombined() {
		return combined;
	}

	/**
	 * Sets the added lines.
	 *
	 * @param addedLines the addedLines to set
	 */
	public void setAddedLines(AtomicInteger addedLines) {
		this.addedLines = addedLines;
	}

	/**
	 * Sets the blocks.
	 *
	 * @param blocks the blocks to set
	 */
	public void setBlocks(List<DiffBlock> blocks) {
		this.blocks = blocks;
	}

	/**
	 * Sets the changed percentage.
	 *
	 * @param changedPercentage the changedPercentage to set
	 */
	public void setChangedPercentage(String changedPercentage) {
		this.changedPercentage = changedPercentage;
	}

	/**
	 * Sets the checksum after.
	 *
	 * @param checksumAfter the checksumAfter to set
	 */
	public void setChecksumAfter(String checksumAfter) {
		this.checksumAfter = checksumAfter;
	}

	/**
	 * Sets the checksum before.
	 *
	 * @param checksumBefore the checksumBefore to set
	 */
	public void setChecksumBefore(String checksumBefore) {
		this.checksumBefore = checksumBefore;
	}

	/**
	 * Sets the combined.
	 *
	 * @param combined the combined to set
	 */
	public void setCombined(boolean combined) {
		this.combined = combined;
	}

	/**
	 * Sets the copy.
	 *
	 * @param copy the copy to set
	 */
	public void setCopy(Boolean copy) {
		this.copy = copy;
	}

	/**
	 * Sets the deleted.
	 *
	 * @param deleted the deleted to set
	 */
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	/**
	 * Sets the deleted file mode.
	 *
	 * @param deletedFileMode the deletedFileMode to set
	 */
	public void setDeletedFileMode(String deletedFileMode) {
		this.deletedFileMode = deletedFileMode;
	}

	/**
	 * Sets the deleted lines.
	 *
	 * @param deletedLines the deletedLines to set
	 */
	public void setDeletedLines(AtomicInteger deletedLines) {
		this.deletedLines = deletedLines;
	}

	/**
	 * Sets the language.
	 *
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * Sets the mode.
	 *
	 * @param mode the mode to set
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * Sets the new file.
	 *
	 * @param newFile the newFile to set
	 */
	public void setNewFile(Boolean newFile) {
		this.newFile = newFile;
	}

	/**
	 * Sets the new file mode.
	 *
	 * @param newFileMode the newFileMode to set
	 */
	public void setNewFileMode(String newFileMode) {
		this.newFileMode = newFileMode;
	}

	/**
	 * Sets the new mode.
	 *
	 * @param newMode the newMode to set
	 */
	public void setNewMode(String newMode) {
		this.newMode = newMode;
	}

	/**
	 * Sets the new name.
	 *
	 * @param newName the newName to set
	 */
	public void setNewName(String newName) {
		this.newName = newName;
	}

	/**
	 * Sets the old mode.
	 *
	 * @param oldMode the oldMode to set
	 */
	public void setOldMode(String oldMode) {
		this.oldMode = oldMode;
	}

	/**
	 * Sets the old name.
	 *
	 * @param oldName the oldName to set
	 */
	public void setOldName(String oldName) {
		this.oldName = oldName;
	}

	/**
	 * Sets the rename.
	 *
	 * @param rename the rename to set
	 */
	public void setRename(Boolean rename) {
		this.rename = rename;
	}

	/**
	 * @param templateData the templateData to set
	 */
	public void setTemplateData(Map<String, Object> templateData) {
		this.templateData = templateData;
	}

	/**
	 * Sets the unchanged percentage.
	 *
	 * @param unchangedPercentage the unchangedPercentage to set
	 */
	public void setUnchangedPercentage(String unchangedPercentage) {
		this.unchangedPercentage = unchangedPercentage;
	}

	/**
	 * Test copy.
	 *
	 * @return true, if successful
	 */
	public boolean testCopy() {
		return toBoolean(copy);
	}

	/**
	 * Test deleted.
	 *
	 * @return true, if successful
	 */
	public boolean testDeleted() {
		return toBoolean(deleted);
	}

	/**
	 * Test new file.
	 *
	 * @return true, if successful
	 */
	public boolean testNewFile() {
		return toBoolean(newFile);
	}

	/**
	 * Test rename.
	 *
	 * @return true, if successful
	 */
	public boolean testRename() {
		return toBoolean(rename);
	}

}
