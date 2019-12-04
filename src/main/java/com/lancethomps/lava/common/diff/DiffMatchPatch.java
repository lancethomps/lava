package com.lancethomps.lava.common.diff;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiffMatchPatch {

  public enum DiffMode {

    LINE_LEVEL,

    WORD_LEVEL
  }

  public enum DiffOperation {

    DELETE("del"),

    EQUAL,

    INSERT("ins");

    private final String htmlTag;

    DiffOperation() {
      this(null);
    }

    DiffOperation(String htmlTag) {
      this.htmlTag = htmlTag;
    }

    public String getHtmlTag() {
      return htmlTag;
    }
  }

  private static final Pattern BLANK_LINE_END = Pattern.compile("\\n\\r?\\n\\Z", Pattern.DOTALL);
  private static final Pattern BLANK_LINE_START = Pattern.compile("\\A\\r?\\n\\r?\\n", Pattern.DOTALL);
  private static final String TAG_REGEX = "<(\"[^\"]*\"|'[^']*'|[^'\">])*>";
  private static final String UTF_8 = "UTF-8";
  private static final String WORD_SEPARATOR_REGEX = "[ :;?!]|" + TAG_REGEX;
  private static final Pattern WORD_SEPARATOR_PATTERN = Pattern.compile(WORD_SEPARATOR_REGEX);
  private short diffEditCost = 4;
  private DiffMode diffMode = DiffMode.LINE_LEVEL;
  private float diffTimeout = 1.0f;
  private int matchDistance = 1000;
  private short matchMaxBits = 32;
  private float matchThreshold = 0.5f;
  private float patchDeleteThreshold = 0.5f;
  private short patchMargin = 4;

  public static String unescapeForEncodeUriCompatability(final String str) {
    return str
      .replace("%21", "!")
      .replace("%7E", "~")
      .replace("%27", "'")
      .replace("%28", "(")
      .replace("%29", ")")
      .replace("%3B", ";")
      .replace("%2F", "/")
      .replace("%3F", "?")
      .replace(
        "%3A",
        ":"
      )
      .replace("%40", "@")
      .replace("%26", "&")
      .replace("%3D", "=")
      .replace("%2B", "+")
      .replace("%24", "$")
      .replace("%2C", ",")
      .replace("%23", "#");
  }

  private static boolean isWordSepartor(final String value) {
    return WORD_SEPARATOR_PATTERN.matcher(value).matches();
  }

  public void diffCleanupEfficiency(final List<Diff> diffs) {
    if (diffs.isEmpty()) {
      return;
    }
    boolean changes = false;
    Stack<Diff> equalities = new Stack<Diff>();
    String lastequality = null;
    ListIterator<Diff> pointer = diffs.listIterator();

    boolean preIns = false;

    boolean preDel = false;

    boolean postIns = false;

    boolean postDel = false;
    Diff thisDiff = pointer.next();
    Diff safeDiff = thisDiff;
    while (thisDiff != null) {
      if (thisDiff.getOperation() == DiffOperation.EQUAL) {

        if ((thisDiff.getText().length() < diffEditCost) && (postIns || postDel)) {

          equalities.push(thisDiff);
          preIns = postIns;
          preDel = postDel;
          lastequality = thisDiff.getText();
        } else {

          equalities.clear();
          lastequality = null;
          safeDiff = thisDiff;
        }
        postDel = false;
        postIns = false;
      } else {

        if (thisDiff.getOperation() == DiffOperation.DELETE) {
          postDel = true;
        } else {
          postIns = true;
        }

        if ((lastequality != null) && ((preIns && preDel && postIns && postDel) ||
          ((lastequality.length() < (diffEditCost / 2)) && (((preIns ? 1 : 0) + (preDel ? 1 : 0) + (postIns ? 1
            : 0) + (postDel ? 1 : 0)) == 3)))) {

          while (thisDiff != equalities.lastElement()) {
            thisDiff = pointer.previous();
          }
          pointer.next();

          pointer.set(new Diff(DiffOperation.DELETE, lastequality));

          thisDiff = new Diff(DiffOperation.INSERT, lastequality);
          pointer.add(thisDiff);

          equalities.pop();
          lastequality = null;
          if (preIns && preDel) {

            postDel = true;
            postIns = true;
            equalities.clear();
            safeDiff = thisDiff;
          } else {
            if (!equalities.empty()) {

              equalities.pop();
            }
            if (equalities.empty()) {

              thisDiff = safeDiff;
            } else {

              thisDiff = equalities.lastElement();
            }

            boolean different = thisDiff != pointer.previous();
            while (different) {
              different = thisDiff != pointer.previous();
            }

            postDel = false;
            postIns = false;
          }

          changes = true;
        }
      }
      thisDiff = pointer.hasNext() ? pointer.next() : null;
    }

    if (changes) {
      diffCleanupMerge(diffs);
    }
  }

  public void diffCleanupMerge(final List<Diff> diffs) {
    diffs.add(new Diff(DiffOperation.EQUAL, ""));
    ListIterator<Diff> pointer = diffs.listIterator();
    int countDelete = 0;
    int countInsert = 0;
    String textDelete = "";
    String textInsert = "";
    Diff thisDiff = pointer.next();
    Diff prevEqual = null;
    int commonlength;
    while (thisDiff != null) {
      switch (thisDiff.getOperation()) {
        case INSERT:
          countInsert++;
          textInsert += thisDiff.getText();
          prevEqual = null;
          break;
        case DELETE:
          countDelete++;
          textDelete += thisDiff.getText();
          prevEqual = null;
          break;
        case EQUAL:
          if ((countDelete + countInsert) > 1) {
            boolean bothTypes = (countDelete != 0) && (countInsert != 0);

            pointer.previous();
            while (countDelete-- > 0) {
              pointer.previous();
              pointer.remove();
            }
            while (countInsert-- > 0) {
              pointer.previous();
              pointer.remove();
            }
            if (bothTypes) {

              commonlength = diffCommonPrefix(textInsert, textDelete);
              if (commonlength != 0) {
                if (pointer.hasPrevious()) {
                  thisDiff = pointer.previous();
                  assert thisDiff.getOperation() == DiffOperation.EQUAL : "Previous diff should have been an equality.";
                  thisDiff.setText(thisDiff.getText() + textInsert.substring(0, commonlength));
                  pointer.next();
                } else {
                  pointer.add(new Diff(DiffOperation.EQUAL, textInsert.substring(0, commonlength)));
                }
                textInsert = textInsert.substring(commonlength);
                textDelete = textDelete.substring(commonlength);
              }

              commonlength = diffCommonSuffix(textInsert, textDelete);
              if (commonlength != 0) {
                thisDiff = pointer.next();
                thisDiff.setText(textInsert.substring(textInsert.length() - commonlength) + thisDiff.getText());
                textInsert = textInsert.substring(0, textInsert.length() - commonlength);
                textDelete = textDelete.substring(0, textDelete.length() - commonlength);
                pointer.previous();
              }
            }

            if (textDelete.length() != 0) {
              pointer.add(new Diff(DiffOperation.DELETE, textDelete));
            }
            if (textInsert.length() != 0) {
              pointer.add(new Diff(DiffOperation.INSERT, textInsert));
            }

            thisDiff = pointer.hasNext() ? pointer.next() : null;
          } else if (prevEqual != null) {

            prevEqual.setText(prevEqual.getText() + thisDiff.getText());
            pointer.remove();
            thisDiff = pointer.previous();
            pointer.next();
          }
          countInsert = 0;
          countDelete = 0;
          textDelete = "";
          textInsert = "";
          prevEqual = thisDiff;
          break;
        default:
          break;
      }
      thisDiff = pointer.hasNext() ? pointer.next() : null;
    }
    if (diffs.get(diffs.size() - 1).getText().length() == 0) {
      diffs.remove(diffs.size() - 1);
    }

    boolean changes = lookForSingleEditsOnBothSides(diffs);

    if (changes) {
      diffCleanupMerge(diffs);
    }
  }

  public void diffCleanupSemantic(final List<Diff> diffs) {
    if (diffs.isEmpty()) {
      return;
    }
    boolean changes = false;
    Stack<Diff> equalities = new Stack<Diff>();
    String lastequality = null;
    ListIterator<Diff> pointer = diffs.listIterator();

    int lengthInsertions1 = 0;
    int lengthDeletions1 = 0;

    int lengthInsertions2 = 0;
    int lengthDeletions2 = 0;
    Diff thisDiff = pointer.next();
    while (thisDiff != null) {
      if (thisDiff.getOperation() == DiffOperation.EQUAL) {

        equalities.push(thisDiff);
        lengthInsertions1 = lengthInsertions2;
        lengthDeletions1 = lengthDeletions2;
        lengthInsertions2 = 0;
        lengthDeletions2 = 0;
        lastequality = thisDiff.getText();
      } else {

        if (thisDiff.getOperation() == DiffOperation.INSERT) {
          lengthInsertions2 += thisDiff.getText().length();
        } else {
          lengthDeletions2 += thisDiff.getText().length();
        }

        if ((lastequality != null) && (lastequality.length() <= Math.max(lengthInsertions1, lengthDeletions1)) && (lastequality.length() <= Math.max(
          lengthInsertions2,
          lengthDeletions2
        ))) {

          while (thisDiff != equalities.lastElement()) {
            thisDiff = pointer.previous();
          }
          pointer.next();

          pointer.set(new Diff(DiffOperation.DELETE, lastequality));

          pointer.add(new Diff(DiffOperation.INSERT, lastequality));

          equalities.pop();
          if (!equalities.empty()) {

            equalities.pop();
          }
          if (equalities.empty()) {

            while (pointer.hasPrevious()) {
              pointer.previous();
            }
          } else {

            thisDiff = equalities.lastElement();

            boolean different = thisDiff != pointer.previous();
            while (different) {
              different = thisDiff != pointer.previous();
            }

          }

          lengthInsertions1 = 0;
          lengthInsertions2 = 0;
          lengthDeletions1 = 0;
          lengthDeletions2 = 0;
          lastequality = null;
          changes = true;
        }
      }
      thisDiff = pointer.hasNext() ? pointer.next() : null;
    }

    normalizeDiff(diffs, changes);

    findOverlapsBetweenDeletionAndInsertion(diffs);
  }

  public void diffCleanupSemanticLossless(final List<Diff> diffs) {
    String equality1, edit, equality2;
    String commonString;
    int commonOffset;
    int score, bestScore;
    String bestEquality1, bestEdit, bestEquality2;

    ListIterator<Diff> pointer = diffs.listIterator();
    Diff prevDiff = pointer.hasNext() ? pointer.next() : null;
    Diff thisDiff = pointer.hasNext() ? pointer.next() : null;
    Diff nextDiff = pointer.hasNext() ? pointer.next() : null;

    while (nextDiff != null) {
      if ((prevDiff.getOperation() == DiffOperation.EQUAL) && (nextDiff.getOperation() == DiffOperation.EQUAL)) {

        equality1 = prevDiff.getText();
        edit = thisDiff.getText();
        equality2 = nextDiff.getText();

        commonOffset = diffCommonSuffix(equality1, edit);
        if (commonOffset != 0) {
          commonString = edit.substring(edit.length() - commonOffset);
          equality1 = equality1.substring(0, equality1.length() - commonOffset);
          edit = commonString + edit.substring(0, edit.length() - commonOffset);
          equality2 = commonString + equality2;
        }

        bestEquality1 = equality1;
        bestEdit = edit;
        bestEquality2 = equality2;
        bestScore = diffCleanupSemanticScore(equality1, edit) + diffCleanupSemanticScore(edit, equality2);
        while ((edit.length() != 0) && (equality2.length() != 0) && (edit.charAt(0) == equality2.charAt(0))) {
          equality1 += edit.charAt(0);
          edit = edit.substring(1) + equality2.charAt(0);
          equality2 = equality2.substring(1);
          score = diffCleanupSemanticScore(equality1, edit) + diffCleanupSemanticScore(edit, equality2);

          if (score >= bestScore) {
            bestScore = score;
            bestEquality1 = equality1;
            bestEdit = edit;
            bestEquality2 = equality2;
          }
        }

        if (!prevDiff.getText().equals(bestEquality1)) {

          if (bestEquality1.length() != 0) {
            prevDiff.setText(bestEquality1);
          } else {
            pointer.previous();
            pointer.previous();
            pointer.previous();
            pointer.remove();
            pointer.next();
            pointer.next();
          }
          thisDiff.setText(bestEdit);
          if (bestEquality2.length() != 0) {
            nextDiff.setText(bestEquality2);
          } else {
            pointer.remove();
            nextDiff = thisDiff;
            thisDiff = prevDiff;
          }
        }
      }
      prevDiff = thisDiff;
      thisDiff = nextDiff;
      nextDiff = pointer.hasNext() ? pointer.next() : null;
    }
  }

  public int diffCommonPrefix(final String text1, final String text2) {

    int n = Math.min(text1.length(), text2.length());
    for (int i = 0; i < n; i++) {
      if (text1.charAt(i) != text2.charAt(i)) {
        return i;
      }
    }
    return n;
  }

  public int diffCommonSuffix(final String text1, final String text2) {

    int text1Length = text1.length();
    int text2Length = text2.length();
    int n = Math.min(text1Length, text2Length);
    for (int i = 1; i <= n; i++) {
      if (text1.charAt(text1Length - i) != text2.charAt(text2Length - i)) {
        return i - 1;
      }
    }
    return n;
  }

  public List<Diff> diffFromDelta(final String text1, final String delta) {
    List<Diff> diffs = new LinkedList<Diff>();
    int pointer = 0;
    String[] tokens = delta.split("\t");
    for (String token : tokens) {
      if (token.length() == 0) {

        continue;
      }

      String param = token.substring(1);
      switch (token.charAt(0)) {
        case '+':

          param = param.replace("+", "%2B");
          try {
            param = URLDecoder.decode(param, UTF_8);
          } catch (UnsupportedEncodingException e) {

          } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException("Illegal escape in diffFromDelta: " + param, e);
          }
          diffs.add(new Diff(DiffOperation.INSERT, param));
          break;
        case '-':

        case '=':
          int n;
          try {
            n = Integer.parseInt(param);
          } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number in diff_fromDelta: " + param, e);
          }
          if (n < 0) {
            throw new IllegalArgumentException("Negative number in diff_fromDelta: " + param);
          }
          String text;
          try {
            text = text1.substring(pointer, pointer + n);
            pointer += n;
          } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Delta length (" + pointer + ") larger than source text length (" + text1.length() + ").", e);
          }
          if (token.charAt(0) == '=') {
            diffs.add(new Diff(DiffOperation.EQUAL, text));
          } else {
            diffs.add(new Diff(DiffOperation.DELETE, text));
          }
          break;
        default:

          throw new IllegalArgumentException("Invalid diff operation in diff_fromDelta: " + token.charAt(0));
      }
    }
    if (pointer != text1.length()) {
      throw new IllegalArgumentException("Delta length (" + pointer + ") smaller than source text length (" + text1.length() + ").");
    }
    return diffs;
  }

  public int diffLevenshtein(final List<Diff> diffs) {
    int levenshtein = 0;
    int insertions = 0;
    int deletions = 0;
    for (Diff aDiff : diffs) {
      switch (aDiff.getOperation()) {
        case INSERT:
          insertions += aDiff.getText().length();
          break;
        case DELETE:
          deletions += aDiff.getText().length();
          break;
        case EQUAL:

          levenshtein += Math.max(insertions, deletions);
          insertions = 0;
          deletions = 0;
          break;
        default:
          break;
      }
    }
    levenshtein += Math.max(insertions, deletions);
    return levenshtein;
  }

  public List<Diff> diffMain(final String text1, final String text2) {
    return diffMain(text1, text2, true);
  }

  public List<Diff> diffMain(final String text1, final String text2, final boolean checklines) {

    long deadline;
    if (diffTimeout <= 0) {
      deadline = Long.MAX_VALUE;
    } else {
      deadline = System.currentTimeMillis() + (long) (diffTimeout * 1000);
    }
    return diffMain(text1, text2, checklines, deadline);
  }

  public List<Diff> diffMainAtLineLevel(final String text1, final String text2) {
    diffMode = DiffMode.LINE_LEVEL;

    LinesOrWordsToCharsResult r = diffLinesOrWordsToChars(text1, text2);
    List<Diff> diffs = diffMain(r.getChars1(), r.getChars2(), false);
    diffCharsToLines(diffs, r.getLineArray());
    return diffs;
  }

  public List<Diff> diffMainAtWordLevel(final String text1, final String text2) {
    diffMode = DiffMode.WORD_LEVEL;

    LinesOrWordsToCharsResult r = diffLinesOrWordsToChars(text1, text2);
    List<Diff> diffs = diffMain(r.getChars1(), r.getChars2(), false);
    diffCharsToLines(diffs, r.getLineArray());
    diffCleanupSemanticLossless(diffs);
    return diffs;
  }

  public String diffPrettyHtml(final List<Diff> diffs) {
    StringBuilder html = new StringBuilder();
    for (Diff aDiff : diffs) {
      String text = aDiff.getText().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "&para;<br>");
      switch (aDiff.getOperation()) {
        case INSERT:
          html.append("<ins style=\"background:#e6ffe6;\">").append(text).append("</ins>");
          break;
        case DELETE:
          html.append("<del style=\"background:#ffe6e6;\">").append(text).append("</del>");
          break;
        case EQUAL:
          html.append("<span>").append(text).append("</span>");
          break;
        default:
          break;
      }
    }
    return html.toString();
  }

  public String diffText1(final List<Diff> diffs) {
    StringBuilder text = new StringBuilder();
    for (Diff aDiff : diffs) {
      if (aDiff.getOperation() != DiffOperation.INSERT) {
        text.append(aDiff.getText());
      }
    }
    return text.toString();
  }

  public String diffText2(final List<Diff> diffs) {
    StringBuilder text = new StringBuilder();
    for (Diff aDiff : diffs) {
      if (aDiff.getOperation() != DiffOperation.DELETE) {
        text.append(aDiff.getText());
      }
    }
    return text.toString();
  }
  // CHECKSTYLE.ON: MethodNameCase

  public String diffToDelta(final List<Diff> diffs) {
    StringBuilder text = new StringBuilder();
    for (Diff aDiff : diffs) {
      switch (aDiff.getOperation()) {
        case INSERT:
          try {
            text.append("+").append(URLEncoder.encode(aDiff.getText(), UTF_8).replace('+', ' ')).append("\t");
          } catch (UnsupportedEncodingException e) {

          }
          break;
        case DELETE:
          text.append("-").append(aDiff.getText().length()).append("\t");
          break;
        case EQUAL:
          text.append("=").append(aDiff.getText().length()).append("\t");
          break;
        default:
          break;
      }
    }
    String delta = text.toString();
    if (delta.length() != 0) {

      delta = delta.substring(0, delta.length() - 1);
      delta = unescapeForEncodeUriCompatability(delta);
    }
    return delta;
  }

  // CHECKSTYLE.OFF: MethodNameCase
  public int diffXIndex(final List<Diff> diffs, final int loc) {
    int chars1 = 0;
    int chars2 = 0;
    int lastChars1 = 0;
    int lastChars2 = 0;
    Diff lastDiff = null;
    for (Diff aDiff : diffs) {
      if (aDiff.getOperation() != DiffOperation.INSERT) {

        chars1 += aDiff.getText().length();
      }
      if (aDiff.getOperation() != DiffOperation.DELETE) {

        chars2 += aDiff.getText().length();
      }
      if (chars1 > loc) {

        lastDiff = aDiff;
        break;
      }
      lastChars1 = chars1;
      lastChars2 = chars2;
    }
    if ((lastDiff != null) && (lastDiff.getOperation() == DiffOperation.DELETE)) {

      return lastChars2;
    }

    return (lastChars2 + loc) - lastChars1;
  }

  public short getDiffEditCost() {
    return diffEditCost;
  }

  public void setDiffEditCost(final short diffEditCost) {
    this.diffEditCost = diffEditCost;
  }

  public float getDiffTimeout() {
    return diffTimeout;
  }

  public void setDiffTimeout(final float diffTimeout) {
    this.diffTimeout = diffTimeout;
  }

  public int getMatchDistance() {
    return matchDistance;
  }

  public void setMatchDistance(final int matchDistance) {
    this.matchDistance = matchDistance;
  }

  public short getMatchMaxBits() {
    return matchMaxBits;
  }

  public void setMatchMaxBits(final short matchMaxBits) {
    this.matchMaxBits = matchMaxBits;
  }

  public float getMatchThreshold() {
    return matchThreshold;
  }

  public void setMatchThreshold(final float matchThreshold) {
    this.matchThreshold = matchThreshold;
  }

  public float getPatchDeleteThreshold() {
    return patchDeleteThreshold;
  }

  public void setPatchDeleteThreshold(final float patchDeleteThreshold) {
    this.patchDeleteThreshold = patchDeleteThreshold;
  }

  public short getPatchMargin() {
    return patchMargin;
  }

  public void setPatchMargin(final short patchMargin) {
    this.patchMargin = patchMargin;
  }

  public int matchMain(final String text, final String pattern, final int loc) {
    int resultLoc = loc;

    if ((text == null) || (pattern == null)) {
      throw new IllegalArgumentException("Null inputs. (match_main)");
    }

    resultLoc = Math.max(0, Math.min(resultLoc, text.length()));
    if (text.equals(pattern)) {

      return 0;
    } else if (text.length() == 0) {

      return -1;
    } else if (((resultLoc + pattern.length()) <= text.length()) && text.substring(resultLoc, resultLoc + pattern.length()).equals(pattern)) {

      return resultLoc;
    } else {

      return matchBitap(text, pattern, resultLoc);
    }
  }

  public String patchAddPadding(final List<Patch> patches) {
    short paddingLength = patchMargin;
    StringBuilder sb = new StringBuilder(paddingLength);
    for (short x = 1; x <= paddingLength; x++) {
      sb.append((char) x);
    }
    String nullPadding = sb.toString();

    for (Patch aPatch : patches) {
      aPatch.setStart1(aPatch.getStart1() + paddingLength);
      aPatch.setStart2(aPatch.getStart2() + paddingLength);
    }

    Patch patch = patches.get(0);
    LinkedList<Diff> patchDiffs = (LinkedList<Diff>) patch.getDiffs();
    if (patchDiffs.isEmpty() || (patchDiffs.getFirst().getOperation() != DiffOperation.EQUAL)) {

      patchDiffs.addFirst(new Diff(DiffOperation.EQUAL, nullPadding));
      patch.setStart1(patch.getStart1() - paddingLength);
      patch.setStart2(patch.getStart2() - paddingLength);
      patch.setLength1(patch.getLength1() + paddingLength);
      patch.setLength2(patch.getLength2() + paddingLength);
    } else if (paddingLength > patchDiffs.getFirst().getText().length()) {

      Diff firstDiff = patchDiffs.getFirst();
      int extraLength = paddingLength - firstDiff.getText().length();
      firstDiff.setText(nullPadding.substring(firstDiff.getText().length()) + firstDiff.getText());
      patch.setStart1(patch.getStart1() - extraLength);
      patch.setStart2(patch.getStart2() - extraLength);
      patch.setLength1(patch.getLength1() + extraLength);
      patch.setLength2(patch.getLength2() + extraLength);
    }

    patch = patches.get(patches.size() - 1);
    patchDiffs = (LinkedList<Diff>) patch.getDiffs();
    if (patchDiffs.isEmpty() || (patchDiffs.getLast().getOperation() != DiffOperation.EQUAL)) {

      patchDiffs.addLast(new Diff(DiffOperation.EQUAL, nullPadding));
      patch.setLength1(patch.getLength1() + paddingLength);
      patch.setLength2(patch.getLength2() + paddingLength);
    } else if (paddingLength > patchDiffs.getLast().getText().length()) {

      Diff lastDiff = patchDiffs.getLast();
      int extraLength = paddingLength - lastDiff.getText().length();
      lastDiff.setText(lastDiff.getText() + nullPadding.substring(0, extraLength));
      patch.setLength1(patch.getLength1() + extraLength);
      patch.setLength2(patch.getLength2() + extraLength);
    }

    return nullPadding;
  }

  public Object[] patchApply(final List<Patch> patches, final String text) {
    if (patches.isEmpty()) {
      return new Object[]{text, new boolean[0]};
    }

    List<Patch> resultPatches = patchDeepCopy(patches);

    String nullPadding = patchAddPadding(resultPatches);
    String resultText = nullPadding + text + nullPadding;
    patchSplitMax(resultPatches);

    int x = 0;

    int delta = 0;
    boolean[] results = new boolean[resultPatches.size()];
    for (Patch aPatch : resultPatches) {
      int expectedLoc = aPatch.getStart2() + delta;
      String text1 = diffText1(aPatch.getDiffs());
      int startLoc;
      int endLoc = -1;
      if (text1.length() > matchMaxBits) {

        startLoc = matchMain(resultText, text1.substring(0, matchMaxBits), expectedLoc);
        if (startLoc != -1) {
          endLoc = matchMain(resultText, text1.substring(text1.length() - matchMaxBits), (expectedLoc + text1.length()) - matchMaxBits);
          if ((endLoc == -1) || (startLoc >= endLoc)) {

            startLoc = -1;
          }
        }
      } else {
        startLoc = matchMain(resultText, text1, expectedLoc);
      }
      if (startLoc == -1) {

        results[x] = false;

        delta -= aPatch.getLength2() - aPatch.getLength1();
      } else {

        results[x] = true;
        delta = startLoc - expectedLoc;
        String text2;
        if (endLoc == -1) {
          text2 = resultText.substring(startLoc, Math.min(startLoc + text1.length(), resultText.length()));
        } else {
          text2 = resultText.substring(startLoc, Math.min(endLoc + matchMaxBits, resultText.length()));
        }
        if (text1.equals(text2)) {

          resultText = resultText.substring(0, startLoc) + diffText2(aPatch.getDiffs()) + resultText.substring(startLoc + text1.length());
        } else {

          List<Diff> diffs = diffMain(text1, text2, false);
          if ((text1.length() > matchMaxBits) && ((diffLevenshtein(diffs) / (float) text1.length()) > patchDeleteThreshold)) {

            results[x] = false;
          } else {
            diffCleanupSemanticLossless(diffs);
            int index1 = 0;
            for (Diff aDiff : aPatch.getDiffs()) {
              if (aDiff.getOperation() != DiffOperation.EQUAL) {
                int index2 = diffXIndex(diffs, index1);
                if (aDiff.getOperation() == DiffOperation.INSERT) {

                  resultText = resultText.substring(0, startLoc + index2) + aDiff.getText() + resultText.substring(startLoc + index2);
                } else if (aDiff.getOperation() == DiffOperation.DELETE) {

                  resultText = resultText.substring(0, startLoc + index2) +
                    resultText.substring(startLoc + diffXIndex(diffs, index1 + aDiff.getText().length()));
                }
              }
              if (aDiff.getOperation() != DiffOperation.DELETE) {
                index1 += aDiff.getText().length();
              }
            }
          }
        }
      }
      x++;
    }

    resultText = resultText.substring(nullPadding.length(), resultText.length() - nullPadding.length());
    return new Object[]{resultText, results};
  }

  public List<Patch> patchDeepCopy(final List<Patch> patches) {
    List<Patch> patchesCopy = new LinkedList<Patch>();
    for (Patch aPatch : patches) {
      Patch patchCopy = new Patch();
      for (Diff aDiff : aPatch.getDiffs()) {
        Diff diffCopy = new Diff(aDiff.getOperation(), aDiff.getText());
        patchCopy.getDiffs().add(diffCopy);
      }
      patchCopy.setStart1(aPatch.getStart1());
      patchCopy.setStart2(aPatch.getStart2());
      patchCopy.setLength1(aPatch.getLength1());
      patchCopy.setLength2(aPatch.getLength2());
      patchesCopy.add(patchCopy);
    }
    return patchesCopy;
  }

  public List<Patch> patchFromText(final String textline) {
    List<Patch> patches = new LinkedList<Patch>();
    if (textline.length() == 0) {
      return patches;
    }
    List<String> textList = Arrays.asList(textline.split("\n"));
    LinkedList<String> text = new LinkedList<String>(textList);
    Patch patch;
    Pattern patchHeader = Pattern.compile("^@@ -(\\d+),?(\\d*) \\+(\\d+),?(\\d*) @@$");
    Matcher m;
    char sign;
    String line;
    while (!text.isEmpty()) {
      m = patchHeader.matcher(text.getFirst());
      if (!m.matches()) {
        throw new IllegalArgumentException("Invalid patch string: " + text.getFirst());
      }
      patch = new Patch();
      patches.add(patch);
      patch.setStart1(Integer.parseInt(m.group(1)));
      if (m.group(2).length() == 0) {
        patch.setStart1(patch.getStart1() - 1);
        patch.setLength1(1);
      } else if (m.group(2).equals("0")) {
        patch.setLength1(0);
      } else {
        patch.setStart1(patch.getStart1() - 1);
        patch.setLength1(Integer.parseInt(m.group(2)));
      }

      patch.setStart2(Integer.parseInt(m.group(3)));
      if (m.group(4).length() == 0) {
        patch.setStart2(patch.getStart2() - 1);
        patch.setLength2(1);
      } else if (m.group(4).equals("0")) {
        patch.setLength2(0);
      } else {
        patch.setStart2(patch.getStart2() - 1);
        patch.setLength2(Integer.parseInt(m.group(4)));
      }
      text.removeFirst();

      while (!text.isEmpty()) {
        try {
          sign = text.getFirst().charAt(0);
        } catch (IndexOutOfBoundsException e) {

          text.removeFirst();
          continue;
        }
        line = text.getFirst().substring(1);
        line = line.replace("+", "%2B");
        try {
          line = URLDecoder.decode(line, UTF_8);
        } catch (UnsupportedEncodingException e) {

        } catch (IllegalArgumentException e) {

          throw new IllegalArgumentException("Illegal escape in patchFromText: " + line, e);
        }
        if (sign == '-') {

          patch.getDiffs().add(new Diff(DiffOperation.DELETE, line));
        } else if (sign == '+') {

          patch.getDiffs().add(new Diff(DiffOperation.INSERT, line));
        } else if (sign == ' ') {

          patch.getDiffs().add(new Diff(DiffOperation.EQUAL, line));
        } else if (sign == '@') {

          break;
        } else {

          throw new IllegalArgumentException("Invalid patch mode '" + sign + "' in: " + line);
        }
        text.removeFirst();
      }
    }
    return patches;
  }

  public List<Patch> patchMake(final List<Diff> diffs) {
    if (diffs == null) {
      throw new IllegalArgumentException("Null inputs. (patchMake)");
    }

    String text1 = diffText1(diffs);
    return patchMake(text1, diffs);
  }

  public List<Patch> patchMake(final String text1, final List<Diff> diffs) {
    if ((text1 == null) || (diffs == null)) {
      throw new IllegalArgumentException("Null inputs. (patchMake)");
    }

    List<Patch> patches = new LinkedList<Patch>();
    if (diffs.isEmpty()) {
      return patches;
    }
    Patch patch = new Patch();
    int charCount1 = 0;
    int charCount2 = 0;

    String prepatchText = text1;
    String postpatchText = text1;
    for (Diff aDiff : diffs) {
      if (patch.getDiffs().isEmpty() && (aDiff.getOperation() != DiffOperation.EQUAL)) {

        patch.setStart1(charCount1);
        patch.setStart2(charCount2);
      }

      switch (aDiff.getOperation()) {
        case INSERT:
          patch.getDiffs().add(aDiff);
          patch.setLength2(patch.getLength2() + aDiff.getText().length());
          postpatchText = postpatchText.substring(0, charCount2) + aDiff.getText() + postpatchText.substring(charCount2);
          break;
        case DELETE:
          patch.setLength1(patch.getLength1() + aDiff.getText().length());
          patch.getDiffs().add(aDiff);
          postpatchText = postpatchText.substring(0, charCount2) + postpatchText.substring(charCount2 + aDiff.getText().length());
          break;
        case EQUAL:
          if ((aDiff.getText().length() <= (2 * patchMargin)) && !patch.getDiffs().isEmpty() && (aDiff != diffs.get(diffs.size() - 1))) {

            patch.getDiffs().add(aDiff);
            patch.setLength1(patch.getLength1() + aDiff.getText().length());
            patch.setLength2(patch.getLength2() + aDiff.getText().length());
          }

          if ((aDiff.getText().length() >= (2 * patchMargin)) && !patch.getDiffs().isEmpty()) {

            patchAddContext(patch, prepatchText);
            patches.add(patch);
            patch = new Patch();

            prepatchText = postpatchText;
            charCount1 = charCount2;
          }
          break;
        default:
          break;
      }

      if (aDiff.getOperation() != DiffOperation.INSERT) {
        charCount1 += aDiff.getText().length();
      }
      if (aDiff.getOperation() != DiffOperation.DELETE) {
        charCount2 += aDiff.getText().length();
      }
    }

    if (!patch.getDiffs().isEmpty()) {
      patchAddContext(patch, prepatchText);
      patches.add(patch);
    }

    return patches;
  }

  public List<Patch> patchMake(final String text1, final String text2) {
    if ((text1 == null) || (text2 == null)) {
      throw new IllegalArgumentException("Null inputs. (patchMake)");
    }

    List<Diff> diffs = diffMain(text1, text2, true);
    if (diffs.size() > 2) {
      diffCleanupSemantic(diffs);
      diffCleanupEfficiency(diffs);
    }
    return patchMake(text1, diffs);
  }

  @Deprecated
  public List<Patch> patchMake(final String text1, final String text2, final List<Diff> diffs) {
    return patchMake(text1, diffs);
  }

  public void patchSplitMax(final List<Patch> patches) {
    short patchSize = matchMaxBits;
    String precontext, postcontext;
    Patch patch;
    int start1, start2;
    boolean empty;
    DiffOperation diffType;
    String diffText;
    ListIterator<Patch> pointer = patches.listIterator();
    Patch bigpatch = pointer.hasNext() ? pointer.next() : null;
    while (bigpatch != null) {
      if (bigpatch.getLength1() <= matchMaxBits) {
        bigpatch = pointer.hasNext() ? pointer.next() : null;
        continue;
      }

      pointer.remove();
      start1 = bigpatch.getStart1();
      start2 = bigpatch.getStart2();
      precontext = "";
      LinkedList<Diff> bigPatchDiffs = (LinkedList<Diff>) bigpatch.getDiffs();
      while (!bigPatchDiffs.isEmpty()) {

        patch = new Patch();
        empty = true;
        patch.setStart1(start1 - precontext.length());
        patch.setStart2(start2 - precontext.length());
        LinkedList<Diff> patchDiffs = (LinkedList<Diff>) patch.getDiffs();
        if (precontext.length() != 0) {
          patch.setLength1(precontext.length());
          patch.setLength2(precontext.length());

          patchDiffs.add(new Diff(DiffOperation.EQUAL, precontext));
        }
        while (!bigPatchDiffs.isEmpty() && (patch.getLength1() < (patchSize - patchMargin))) {
          LinkedList<Diff> diffs = bigPatchDiffs;
          diffType = diffs.getFirst().getOperation();
          diffText = diffs.getFirst().getText();
          if (diffType == DiffOperation.INSERT) {

            patch.setLength2(patch.getLength2() + diffText.length());
            start2 += diffText.length();
            patchDiffs.addLast(diffs.removeFirst());
            empty = false;
          } else if ((diffType == DiffOperation.DELETE) && (patchDiffs.size() == 1) &&
            (patchDiffs.getFirst().getOperation() == DiffOperation.EQUAL) && (diffText.length() > (2
            * patchSize))) {

            patch.setLength1(patch.getLength1() + diffText.length());
            start1 += diffText.length();
            empty = false;
            patchDiffs.add(new Diff(diffType, diffText));
            bigPatchDiffs.removeFirst();
          } else {

            diffText = diffText.substring(0, Math.min(diffText.length(), patchSize - patch.getLength1() - patchMargin));
            patch.setLength1(patch.getLength1() + diffText.length());
            start1 += diffText.length();
            if (diffType == DiffOperation.EQUAL) {
              patch.setLength2(patch.getLength2() + diffText.length());
              start2 += diffText.length();
            } else {
              empty = false;
            }
            patchDiffs.add(new Diff(diffType, diffText));
            if (diffText.equals(bigPatchDiffs.getFirst().getText())) {
              bigPatchDiffs.removeFirst();
            } else {
              bigPatchDiffs.getFirst().setText(bigPatchDiffs.getFirst().getText().substring(diffText.length()));
            }
          }
        }

        precontext = diffText2(patchDiffs);
        precontext = precontext.substring(Math.max(0, precontext.length() - patchMargin));

        if (diffText1(bigPatchDiffs).length() > patchMargin) {
          postcontext = diffText1(bigPatchDiffs).substring(0, patchMargin);
        } else {
          postcontext = diffText1(bigPatchDiffs);
        }
        if (postcontext.length() != 0) {
          patch.setLength1(patch.getLength1() + postcontext.length());
          patch.setLength2(patch.getLength2() + postcontext.length());

          if (!patchDiffs.isEmpty() && (patchDiffs.getLast().getOperation() == DiffOperation.EQUAL)) {
            patchDiffs.getLast().setText(patchDiffs.getLast().getText() + postcontext);
          } else {
            patchDiffs.add(new Diff(DiffOperation.EQUAL, postcontext));
          }
        }
        if (!empty) {
          pointer.add(patch);
        }
      }
      bigpatch = pointer.hasNext() ? pointer.next() : null;
    }
  }

  public String patchToText(final List<Patch> patches) {
    StringBuilder text = new StringBuilder();
    for (Patch aPatch : patches) {
      text.append(aPatch);
    }
    return text.toString();
  }

  protected List<Diff> diffBisect(final String text1, final String text2, final long deadline) {

    int text1Length = text1.length();
    int text2Length = text2.length();
    int maxD = (text1Length + text2Length + 1) / 2;
    int vOffset = maxD;
    int vLength = 2 * maxD;
    int[] v1 = new int[vLength];
    int[] v2 = new int[vLength];
    for (int x = 0; x < vLength; x++) {
      v1[x] = -1;
      v2[x] = -1;
    }
    v1[vOffset + 1] = 0;
    v2[vOffset + 1] = 0;
    int delta = text1Length - text2Length;

    boolean front = (delta % 2) != 0;

    int k1start = 0;
    int k1end = 0;
    int k2start = 0;
    int k2end = 0;
    for (int d = 0; d < maxD; d++) {

      if (System.currentTimeMillis() > deadline) {
        break;
      }

      for (int k1 = -d + k1start; k1 <= (d - k1end); k1 += 2) {
        int k1Offset = vOffset + k1;
        int x1;
        if ((k1 == -d) || ((k1 != d) && (v1[k1Offset - 1] < v1[k1Offset + 1]))) {
          x1 = v1[k1Offset + 1];
        } else {
          x1 = v1[k1Offset - 1] + 1;
        }
        int y1 = x1 - k1;
        while ((x1 < text1Length) && (y1 < text2Length) && (text1.charAt(x1) == text2.charAt(y1))) {
          x1++;
          y1++;
        }
        v1[k1Offset] = x1;
        if (x1 > text1Length) {

          k1end += 2;
        } else if (y1 > text2Length) {

          k1start += 2;
        } else if (front) {
          int k2Offset = (vOffset + delta) - k1;
          if ((k2Offset >= 0) && (k2Offset < vLength) && (v2[k2Offset] != -1)) {

            int x2 = text1Length - v2[k2Offset];
            if (x1 >= x2) {

              return diffBisectSplit(text1, text2, x1, y1, deadline);
            }
          }
        }
      }

      for (int k2 = -d + k2start; k2 <= (d - k2end); k2 += 2) {
        int k2Offset = vOffset + k2;
        int x2;
        if ((k2 == -d) || ((k2 != d) && (v2[k2Offset - 1] < v2[k2Offset + 1]))) {
          x2 = v2[k2Offset + 1];
        } else {
          x2 = v2[k2Offset - 1] + 1;
        }
        int y2 = x2 - k2;
        while ((x2 < text1Length) && (y2 < text2Length) && (text1.charAt(text1Length - x2 - 1) == text2.charAt(text2Length - y2 - 1))) {
          x2++;
          y2++;
        }
        v2[k2Offset] = x2;
        if (x2 > text1Length) {

          k2end += 2;
        } else if (y2 > text2Length) {

          k2start += 2;
        } else if (!front) {
          int k1Offset = (vOffset + delta) - k2;
          if ((k1Offset >= 0) && (k1Offset < vLength) && (v1[k1Offset] != -1)) {
            int x1 = v1[k1Offset];
            int y1 = (vOffset + x1) - k1Offset;

            x2 = text1Length - x2;
            if (x1 >= x2) {

              return diffBisectSplit(text1, text2, x1, y1, deadline);
            }
          }
        }
      }
    }

    List<Diff> diffs = new LinkedList<Diff>();
    diffs.add(new Diff(DiffOperation.DELETE, text1));
    diffs.add(new Diff(DiffOperation.INSERT, text2));
    return diffs;
  }

  protected void diffCharsToLines(final List<Diff> diffs, final List<String> lineArray) {
    StringBuilder text;
    for (Diff diff : diffs) {
      text = new StringBuilder();
      for (int y = 0; y < diff.getText().length(); y++) {
        text.append(lineArray.get(diff.getText().charAt(y)));
      }
      diff.setText(text.toString());
    }
  }

  protected int diffCommonOverlap(final String text1, final String text2) {

    int text1Length = text1.length();
    int text2Length = text2.length();

    String resultText1 = text1;
    String resultText2 = text2;

    if ((text1Length == 0) || (text2Length == 0)) {
      return 0;
    }

    if (text1Length > text2Length) {
      resultText1 = resultText1.substring(text1Length - text2Length);
    } else if (text1Length < text2Length) {
      resultText2 = resultText2.substring(0, text1Length);
    }
    int textLength = Math.min(text1Length, text2Length);

    if (resultText1.equals(resultText2)) {
      return textLength;
    }

    int best = 0;
    int length = 1;
    while (true) {
      String pattern = resultText1.substring(textLength - length);
      int found = resultText2.indexOf(pattern);
      if (found == -1) {
        return best;
      }
      length += found;
      if ((found == 0) || resultText1.substring(textLength - length).equals(resultText2.substring(0, length))) {
        best = length;
        length++;
      }
    }
  }

  protected String[] diffHalfMatch(final String text1, final String text2) {
    if (diffTimeout <= 0) {

      return new String[0];
    }
    String longtext = text1.length() > text2.length() ? text1 : text2;
    String shorttext = text1.length() > text2.length() ? text2 : text1;
    if ((longtext.length() < 4) || ((shorttext.length() * 2) < longtext.length())) {
      return new String[0];
    }

    String[] hm1 = diffHalfMatchI(longtext, shorttext, (longtext.length() + 3) / 4);

    String[] hm2 = diffHalfMatchI(longtext, shorttext, (longtext.length() + 1) / 2);
    String[] hm;
    if ((hm1.length == 0) && (hm2.length == 0)) {
      return new String[0];
    } else if (hm2.length == 0) {
      hm = hm1;
    } else if (hm1.length == 0) {
      hm = hm2;
    } else {

      hm = hm1[4].length() > hm2[4].length() ? hm1 : hm2;
    }

    if (text1.length() > text2.length()) {
      return hm;

    }
    return new String[]{hm[2], hm[3], hm[0], hm[1], hm[4]};
  }

  protected LinesOrWordsToCharsResult diffLinesOrWordsToChars(final String text1, final String text2) {
    List<String> lineArray = new ArrayList<String>();
    Map<String, Integer> lineHash = new HashMap<String, Integer>();

    lineArray.add("");

    String chars1 = diffLinesOrWordsToCharsMunge(text1, lineArray, lineHash);
    String chars2 = diffLinesOrWordsToCharsMunge(text2, lineArray, lineHash);
    return new LinesOrWordsToCharsResult(chars1, chars2, lineArray);
  }

  protected Map<Character, Integer> matchAlphabet(final String pattern) {
    Map<Character, Integer> s = new HashMap<Character, Integer>();
    char[] charPattern = pattern.toCharArray();
    for (char c : charPattern) {
      s.put(c, 0);
    }
    int i = 0;
    for (char c : charPattern) {
      s.put(c, s.get(c) | (1 << (pattern.length() - i - 1)));
      i++;
    }
    return s;
  }

  protected int matchBitap(final String text, final String pattern, final int loc) {
    assert (matchMaxBits == 0) || (pattern.length() <= matchMaxBits) : "Pattern too long for this application.";

    Map<Character, Integer> s = matchAlphabet(pattern);

    double scoreThreshold = matchThreshold;

    int bestLoc = text.indexOf(pattern, loc);
    if (bestLoc != -1) {
      scoreThreshold = Math.min(matchBitapScore(0, bestLoc, loc, pattern), scoreThreshold);

      bestLoc = text.lastIndexOf(pattern, loc + pattern.length());
      if (bestLoc != -1) {
        scoreThreshold = Math.min(matchBitapScore(0, bestLoc, loc, pattern), scoreThreshold);
      }
    }

    int matchmask = 1 << (pattern.length() - 1);
    bestLoc = -1;

    int binMin, binMid;
    int binMax = pattern.length() + text.length();

    int[] lastRd = new int[0];
    for (int d = 0; d < pattern.length(); d++) {

      binMin = 0;
      binMid = binMax;
      while (binMin < binMid) {
        if (matchBitapScore(d, loc + binMid, loc, pattern) <= scoreThreshold) {
          binMin = binMid;
        } else {
          binMax = binMid;
        }
        binMid = ((binMax - binMin) / 2) + binMin;
      }

      binMax = binMid;
      int start = Math.max(1, (loc - binMid) + 1);
      int finish = Math.min(loc + binMid, text.length()) + pattern.length();

      int[] rd = new int[finish + 2];
      rd[finish + 1] = (1 << d) - 1;
      for (int j = finish; j >= start; j--) {
        int charMatch;
        if ((text.length() <= (j - 1)) || !s.containsKey(text.charAt(j - 1))) {

          charMatch = 0;
        } else {
          charMatch = s.get(text.charAt(j - 1));
        }
        if (d == 0) {

          rd[j] = ((rd[j + 1] << 1) | 1) & charMatch;
        } else {

          rd[j] = (((rd[j + 1] << 1) | 1) & charMatch) | ((lastRd[j + 1] | lastRd[j]) << 1) | 1 | lastRd[j + 1];
        }
        if ((rd[j] & matchmask) != 0) {
          double score = matchBitapScore(d, j - 1, loc, pattern);

          if (score <= scoreThreshold) {

            scoreThreshold = score;
            bestLoc = j - 1;
            if (bestLoc > loc) {

              start = Math.max(1, (2 * loc) - bestLoc);
            } else {

              break;
            }
          }
        }
      }
      if (matchBitapScore(d + 1, loc, loc, pattern) > scoreThreshold) {

        break;
      }
      lastRd = rd;
    }
    return bestLoc;
  }

  protected void patchAddContext(final Patch patch, final String text) {
    if (text.length() == 0) {
      return;
    }
    String pattern = text.substring(patch.getStart2(), patch.getStart2() + patch.getLength1());
    int padding = 0;

    while ((text.indexOf(pattern) != text.lastIndexOf(pattern)) && (pattern.length() < (matchMaxBits - patchMargin - patchMargin))) {
      padding += patchMargin;
      pattern = text.substring(Math.max(0, patch.getStart2() - padding), Math.min(text.length(), patch.getStart2() + patch.getLength1() + padding));
    }

    padding += patchMargin;

    String prefix = text.substring(Math.max(0, patch.getStart2() - padding), patch.getStart2());
    LinkedList<Diff> diffs = (LinkedList<Diff>) patch.getDiffs();
    if (prefix.length() != 0) {
      diffs.addFirst(new Diff(DiffOperation.EQUAL, prefix));
    }

    String suffix = text.substring(patch.getStart2() + patch.getLength1(), Math.min(text.length(), patch.getStart2() + patch.getLength1() + padding));
    if (suffix.length() != 0) {
      diffs.addLast(new Diff(DiffOperation.EQUAL, suffix));
    }

    patch.setStart1(patch.getStart1() - prefix.length());
    patch.setStart2(patch.getStart2() - prefix.length());

    patch.setLength1(patch.getLength1() + prefix.length() + suffix.length());
    patch.setLength2(patch.getLength2() + prefix.length() + suffix.length());
  }

  private List<Diff> diffBisectSplit(final String text1, final String text2, final int x, final int y, final long deadline) {
    String text1a = text1.substring(0, x);
    String text2a = text2.substring(0, y);
    String text1b = text1.substring(x);
    String text2b = text2.substring(y);

    List<Diff> diffs = diffMain(text1a, text2a, false, deadline);
    List<Diff> diffsb = diffMain(text1b, text2b, false, deadline);

    diffs.addAll(diffsb);
    return diffs;
  }

  private int diffCleanupSemanticScore(final String one, final String two) {
    if ((one.length() == 0) || (two.length() == 0)) {

      return 6;
    }

    char char1 = one.charAt(one.length() - 1);
    char char2 = two.charAt(0);
    boolean nonAlphaNumeric1 = !Character.isLetterOrDigit(char1);
    boolean nonAlphaNumeric2 = !Character.isLetterOrDigit(char2);
    boolean isWordSeparator1 = nonAlphaNumeric1 && (isWordSepartor(Character.toString(char1)) || ('.' == char1));
    boolean isWordSeparator = nonAlphaNumeric2 && (isWordSepartor(Character.toString(char2)) || ('.' == char2));
    boolean lineBreak1 = isWordSeparator1 && (Character.getType(char1) == Character.CONTROL);
    boolean lineBreak2 = isWordSeparator && (Character.getType(char2) == Character.CONTROL);
    boolean blankLine1 = lineBreak1 && BLANK_LINE_END.matcher(one).find();
    boolean blankLine2 = lineBreak2 && BLANK_LINE_START.matcher(two).find();

    if (blankLine1 || blankLine2) {

      return 5;
    } else if (lineBreak1 || lineBreak2) {

      return 4;
    } else if (nonAlphaNumeric1 && !isWordSeparator1 && isWordSeparator) {

      return 3;
    } else if (isWordSeparator1 || isWordSeparator) {

      return 2;
    } else if (nonAlphaNumeric1 || nonAlphaNumeric2) {

      return 1;
    }
    return 0;
  }

  private List<Diff> diffCompute(final String text1, final String text2, final boolean checklines, final long deadline) {
    List<Diff> diffs = new LinkedList<Diff>();

    if (text1.length() == 0) {

      diffs.add(new Diff(DiffOperation.INSERT, text2));
      return diffs;
    }

    if (text2.length() == 0) {

      diffs.add(new Diff(DiffOperation.DELETE, text1));
      return diffs;
    }

    {

      String longtext = text1.length() > text2.length() ? text1 : text2;
      String shorttext = text1.length() > text2.length() ? text2 : text1;
      int i = longtext.indexOf(shorttext);
      if (i != -1) {

        DiffOperation op = text1.length() > text2.length() ? DiffOperation.DELETE : DiffOperation.INSERT;
        diffs.add(new Diff(op, longtext.substring(0, i)));
        diffs.add(new Diff(DiffOperation.EQUAL, shorttext));
        diffs.add(new Diff(op, longtext.substring(i + shorttext.length())));
        return diffs;
      }

      if (shorttext.length() == 1) {

        diffs.add(new Diff(DiffOperation.DELETE, text1));
        diffs.add(new Diff(DiffOperation.INSERT, text2));
        return diffs;
      }
    }

    String[] hm = diffHalfMatch(text1, text2);
    if (hm.length != 0) {

      String text1A = hm[0];
      String text1B = hm[1];
      String text2A = hm[2];
      String text2B = hm[3];
      String midCommon = hm[4];

      List<Diff> diffsA = diffMain(text1A, text2A, checklines, deadline);
      List<Diff> diffsB = diffMain(text1B, text2B, checklines, deadline);

      diffs = diffsA;
      diffs.add(new Diff(DiffOperation.EQUAL, midCommon));
      diffs.addAll(diffsB);
      return diffs;
    }

    if (checklines && (text1.length() > 100) && (text2.length() > 100)) {
      return diffLineMode(text1, text2, deadline);
    }

    return diffBisect(text1, text2, deadline);
  }

  private String[] diffHalfMatchI(final String longtext, final String shorttext, final int i) {

    String seed = longtext.substring(i, i + (longtext.length() / 4));
    int j = -1;
    String bestCommon = "";
    String bestLongtextA = "", bestLongtextB = "";
    String bestShorttextA = "", bestShorttextB = "";
    while ((j = shorttext.indexOf(seed, j + 1)) != -1) {
      int prefixLength = diffCommonPrefix(longtext.substring(i), shorttext.substring(j));
      int suffixLength = diffCommonSuffix(longtext.substring(0, i), shorttext.substring(0, j));
      if (bestCommon.length() < (suffixLength + prefixLength)) {
        bestCommon = shorttext.substring(j - suffixLength, j) + shorttext.substring(j, j + prefixLength);
        bestLongtextA = longtext.substring(0, i - suffixLength);
        bestLongtextB = longtext.substring(i + prefixLength);
        bestShorttextA = shorttext.substring(0, j - suffixLength);
        bestShorttextB = shorttext.substring(j + prefixLength);
      }
    }
    if ((bestCommon.length() * 2) >= longtext.length()) {
      return new String[]{bestLongtextA, bestLongtextB, bestShorttextA, bestShorttextB, bestCommon};
    }
    return new String[0];
  }

  private List<Diff> diffLineMode(final String text1, final String text2, final long deadline) {

    LinesOrWordsToCharsResult b = diffLinesOrWordsToChars(text1, text2);
    String resultText1 = b.chars1;
    String resultText2 = b.chars2;
    List<String> linearray = b.lineArray;

    LinkedList<Diff> diffs = (LinkedList<Diff>) diffMain(resultText1, resultText2, false, deadline);

    diffCharsToLines(diffs, linearray);

    diffCleanupSemantic(diffs);

    diffs.add(new Diff(DiffOperation.EQUAL, ""));
    int countDelete = 0;
    int countInsert = 0;
    String textDelete = "";
    String textInsert = "";
    ListIterator<Diff> pointer = diffs.listIterator();
    Diff thisDiff = pointer.next();
    while (thisDiff != null) {
      switch (thisDiff.getOperation()) {
        case INSERT:
          countInsert++;
          textInsert += thisDiff.getText();
          break;
        case DELETE:
          countDelete++;
          textDelete += thisDiff.getText();
          break;
        case EQUAL:

          if ((countDelete >= 1) && (countInsert >= 1)) {

            pointer.previous();
            for (int j = 0; j < (countDelete + countInsert); j++) {
              pointer.previous();
              pointer.remove();
            }
            for (Diff newDiff : diffMain(textDelete, textInsert, false, deadline)) {
              pointer.add(newDiff);
            }
          }
          countInsert = 0;
          countDelete = 0;
          textDelete = "";
          textInsert = "";
          break;
        default:
          break;
      }
      thisDiff = pointer.hasNext() ? pointer.next() : null;
    }
    diffs.removeLast();

    return diffs;
  }

  private String diffLinesOrWordsToCharsMunge(final String text, final List<String> lineArray, final Map<String, Integer> lineHash) {
    int lineOrWordStart = 0;
    int lineOrWordEnd = -1;
    String lineOrWord;

    StringBuilder chars = new StringBuilder();

    while (lineOrWordEnd < (text.length() - 1)) {
      int separatorLength = 1;
      String separator = "";
      if (diffMode == DiffMode.LINE_LEVEL) {
        lineOrWordEnd = text.indexOf('\n', lineOrWordStart);
      } else {
        Matcher m = WORD_SEPARATOR_PATTERN.matcher(text);
        m.region(lineOrWordStart, text.length());

        if (m.find()) {
          lineOrWordEnd = m.start();
          separator = m.group();
          separatorLength = m.end() - m.start();
        } else {
          lineOrWordEnd = -1;
        }
      }
      if (lineOrWordEnd == -1) {
        lineOrWordEnd = text.length();
      }

      lineOrWord = text.substring(lineOrWordStart, lineOrWordEnd);
      lineOrWordStart = lineOrWordEnd + separatorLength;

      if (lineHash.containsKey(lineOrWord)) {
        chars.append((char) (int) lineHash.get(lineOrWord));
      } else {
        lineArray.add(lineOrWord);
        lineHash.put(lineOrWord, lineArray.size() - 1);
        chars.append((char) (lineArray.size() - 1));
      }

      if (!"".equals(separator)) {
        if (lineHash.containsKey(separator)) {
          chars.append((char) (int) lineHash.get(separator));
        } else {
          lineArray.add(separator);
          lineHash.put(separator, lineArray.size() - 1);
          chars.append((char) (lineArray.size() - 1));
        }
      }

    }
    return chars.toString();
  }

  private List<Diff> diffMain(final String text1, final String text2, final boolean checklines, final long deadline) {

    if ((text1 == null) || (text2 == null)) {
      throw new IllegalArgumentException("Null inputs. (diffMain)");
    }

    LinkedList<Diff> diffs;
    if (text1.equals(text2)) {
      diffs = new LinkedList<Diff>();
      if (text1.length() != 0) {
        diffs.add(new Diff(DiffOperation.EQUAL, text1));
      }
      return diffs;
    }

    int commonlength = diffCommonPrefix(text1, text2);
    String commonprefix = text1.substring(0, commonlength);
    String resultText1 = text1.substring(commonlength);
    String resultText2 = text2.substring(commonlength);

    commonlength = diffCommonSuffix(resultText1, resultText2);
    String commonsuffix = resultText1.substring(resultText1.length() - commonlength);
    resultText1 = resultText1.substring(0, resultText1.length() - commonlength);
    resultText2 = resultText2.substring(0, resultText2.length() - commonlength);

    diffs = (LinkedList<Diff>) diffCompute(resultText1, resultText2, checklines, deadline);

    if (commonprefix.length() != 0) {
      diffs.addFirst(new Diff(DiffOperation.EQUAL, commonprefix));
    }
    if (commonsuffix.length() != 0) {
      diffs.addLast(new Diff(DiffOperation.EQUAL, commonsuffix));
    }

    diffCleanupMerge(diffs);
    return diffs;
  }

  private void findOverlapsBetweenDeletionAndInsertion(final List<Diff> diffs) {
    ListIterator<Diff> pointer;
    Diff thisDiff;
    pointer = diffs.listIterator();
    Diff prevDiff = null;
    thisDiff = null;
    if (pointer.hasNext()) {
      prevDiff = pointer.next();
      if (pointer.hasNext()) {
        thisDiff = pointer.next();
      }
    }
    while (thisDiff != null) {
      if ((prevDiff.getOperation() == DiffOperation.DELETE) && (thisDiff.getOperation() == DiffOperation.INSERT)) {
        String deletion = prevDiff.getText();
        String insertion = thisDiff.getText();
        int overlapLength1 = diffCommonOverlap(deletion, insertion);
        int overlapLength2 = diffCommonOverlap(insertion, deletion);
        if (overlapLength1 >= overlapLength2) {
          if ((overlapLength1 >= (deletion.length() / 2.0)) || (overlapLength1 >= (insertion.length() / 2.0))) {

            pointer.previous();
            pointer.add(new Diff(DiffOperation.EQUAL, insertion.substring(0, overlapLength1)));
            prevDiff.setText(deletion.substring(0, deletion.length() - overlapLength1));
            thisDiff.setText(insertion.substring(overlapLength1));

          }
        } else if ((overlapLength2 >= (deletion.length() / 2.0)) || (overlapLength2 >= (insertion.length() / 2.0))) {

          pointer.previous();
          pointer.add(new Diff(DiffOperation.EQUAL, deletion.substring(0, overlapLength2)));
          prevDiff.setOperation(DiffOperation.INSERT);
          prevDiff.setText(insertion.substring(0, insertion.length() - overlapLength2));
          thisDiff.setOperation(DiffOperation.DELETE);
          thisDiff.setText(deletion.substring(overlapLength2));

        }
        thisDiff = pointer.hasNext() ? pointer.next() : null;
      }
      prevDiff = thisDiff;
      thisDiff = pointer.hasNext() ? pointer.next() : null;
    }
  }

  private boolean lookForSingleEditsOnBothSides(final List<Diff> diffs) {
    ListIterator<Diff> pointer;
    Diff thisDiff;
    boolean changes = false;

    pointer = diffs.listIterator();
    Diff prevDiff = pointer.hasNext() ? pointer.next() : null;
    thisDiff = pointer.hasNext() ? pointer.next() : null;
    Diff nextDiff = pointer.hasNext() ? pointer.next() : null;

    while (nextDiff != null) {
      if ((prevDiff.getOperation() == DiffOperation.EQUAL) && (nextDiff.getOperation() == DiffOperation.EQUAL)) {

        if (thisDiff.getText().endsWith(prevDiff.getText())) {

          thisDiff.setText(prevDiff.getText() + thisDiff.getText().substring(0, thisDiff.getText().length() - prevDiff.getText().length()));
          nextDiff.setText(prevDiff.getText() + nextDiff.getText());
          pointer.previous();
          pointer.previous();
          pointer.previous();
          pointer.remove();
          pointer.next();
          thisDiff = pointer.next();
          nextDiff = pointer.hasNext() ? pointer.next() : null;
          changes = true;
        } else if (thisDiff.getText().startsWith(nextDiff.getText())) {

          prevDiff.setText(prevDiff.getText() + nextDiff.getText());
          thisDiff.setText(thisDiff.getText().substring(nextDiff.getText().length()) + nextDiff.getText());
          pointer.remove();
          nextDiff = pointer.hasNext() ? pointer.next() : null;
          changes = true;
        }
      }
      prevDiff = thisDiff;
      thisDiff = nextDiff;
      nextDiff = pointer.hasNext() ? pointer.next() : null;
    }
    return changes;
  }

  private double matchBitapScore(final int e, final int x, final int loc, final String pattern) {
    float accuracy = (float) e / pattern.length();
    int proximity = Math.abs(loc - x);
    if (matchDistance == 0) {

      return proximity == 0 ? accuracy : 1.0;
    }
    return accuracy + (proximity / (float) matchDistance);
  }

  private void normalizeDiff(final List<Diff> diffs, final boolean changes) {
    if (changes) {
      diffCleanupMerge(diffs);
    }
    diffCleanupSemanticLossless(diffs);
  }

  protected static class Diff {

    private DiffOperation operation;

    private String text;

    public Diff(final DiffOperation operation, final String text) {

      this.operation = operation;
      this.text = text;
    }

    public DiffOperation getOperation() {
      return operation;
    }

    public void setOperation(final DiffOperation operation) {
      this.operation = operation;
    }

    public String getText() {
      return text;
    }

    public void setText(final String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      String prettyText = text.replace('\n', '\u00b6');
      return "Diff(" + operation + ",\"" + prettyText + "\")";
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      Diff other = (Diff) obj;
      if (operation != other.operation) {
        return false;
      }
      if (text == null) {
        return other.text == null;
      } else {
        return text.equals(other.text);
      }
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = operation == null ? 0 : operation.hashCode();
      result += prime * (text == null ? 0 : text.hashCode());
      return result;
    }

  }

  protected static class LinesOrWordsToCharsResult {

    private String chars1;

    private String chars2;

    private List<String> lineArray;

    protected LinesOrWordsToCharsResult(final String chars1, final String chars2, final List<String> lineArray) {
      this.chars1 = chars1;
      this.chars2 = chars2;
      this.lineArray = lineArray;
    }

    public String getChars1() {
      return chars1;
    }

    public void setChars1(final String chars1) {
      this.chars1 = chars1;
    }

    public String getChars2() {
      return chars2;
    }

    public void setChars2(final String chars2) {
      this.chars2 = chars2;
    }

    public List<String> getLineArray() {
      return lineArray;
    }

    public void setLineArray(final List<String> lineArray) {
      this.lineArray = lineArray;
    }

  }

  protected static class Patch {

    private List<Diff> diffs;

    private int length1;

    private int length2;

    private int start1;

    private int start2;

    public Patch() {
      diffs = new LinkedList<Diff>();
    }

    public List<Diff> getDiffs() {
      return diffs;
    }

    public void setDiffs(final List<Diff> diffs) {
      this.diffs = diffs;
    }

    public int getLength1() {
      return length1;
    }

    public void setLength1(final int length1) {
      this.length1 = length1;
    }

    public int getLength2() {
      return length2;
    }

    public void setLength2(final int length2) {
      this.length2 = length2;
    }

    public int getStart1() {
      return start1;
    }

    public void setStart1(final int start1) {
      this.start1 = start1;
    }

    public int getStart2() {
      return start2;
    }

    public void setStart2(final int start2) {
      this.start2 = start2;
    }

    @Override
    public String toString() {
      String coords1, coords2;
      if (length1 == 0) {
        coords1 = start1 + ",0";
      } else if (length1 == 1) {
        coords1 = Integer.toString(start1 + 1);
      } else {
        coords1 = "" + start1 + 1 + ',' + length1;
      }
      if (length2 == 0) {
        coords2 = start2 + ",0";
      } else if (length2 == 1) {
        coords2 = Integer.toString(start2 + 1);
      } else {
        coords2 = "" + start2 + 1 + ',' + length2;
      }
      StringBuilder text = new StringBuilder();
      text.append("@@ -").append(coords1).append(" +").append(coords2).append(" @@\n");

      for (Diff aDiff : diffs) {
        switch (aDiff.getOperation()) {
          case INSERT:
            text.append('+');
            break;
          case DELETE:
            text.append('-');
            break;
          case EQUAL:
            text.append(' ');
            break;
          default:
            break;
        }
        try {
          text.append(URLEncoder.encode(aDiff.getText(), UTF_8).replace('+', ' ')).append("\n");
        } catch (UnsupportedEncodingException e) {

        }
      }
      return DiffMatchPatch.unescapeForEncodeUriCompatability(text.toString());
    }

  }

}
