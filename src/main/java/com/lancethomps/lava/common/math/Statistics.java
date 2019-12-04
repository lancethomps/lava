package com.lancethomps.lava.common.math;

import static com.lancethomps.lava.common.math.MathUtil.MC;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.log4j.Logger;

import com.lancethomps.lava.common.logging.Logs;

public class Statistics {

  private static final PearsonsCorrelation CORRELATION = new PearsonsCorrelation();

  private static final Covariance COVARIANCE = new Covariance();

  private static final Logger LOG = Logger.getLogger(Statistics.class);

  public static BigDecimal calculateBattingAverage(double[] portReturns, double[] benchReturns) {
    BigDecimal battingAvg = null;
    if ((portReturns.length > 0) && (portReturns.length == benchReturns.length)) {
      battingAvg = ZERO;
      for (int i = 0; i < portReturns.length; i++) {
        double fundReturn = portReturns[i];
        double betaReturn = benchReturns[i];
        if (fundReturn > betaReturn) {
          battingAvg = battingAvg.add(ONE, MC);
        }
      }
      battingAvg = battingAvg.divide(new BigDecimal(portReturns.length, MC), MC);
    }
    return battingAvg;
  }

  public static BigDecimal calculateBetas(double[] portReturns, double[] benchReturns) {
    if ((portReturns.length > 1) && (benchReturns.length == portReturns.length)) {
      double variance = StatUtils.variance(benchReturns);
      double covariance = COVARIANCE.covariance(benchReturns, portReturns);
      double beta = covariance / variance;
      if (Double.isFinite(beta)) {
        return BigDecimal.valueOf(beta);
      }
      Logs.logWarn(LOG, "Beta was not finite: covariance=%s variance=%s", covariance, variance);
      return null;
    }

    return null;
  }

  public static BigDecimal calculateCorrelation(@Nonnull double[] xArray, @Nonnull double[] yArray) {
    if ((xArray.length <= 1) || (xArray.length != yArray.length)) {
      return null;
    }
    double corr = CORRELATION.correlation(xArray, yArray);
    if (Double.isFinite(corr)) {
      return BigDecimal.valueOf(corr);
    }
    Logs.logWarn(LOG, "Correlation was not finite for values: xArray=%s yArray=%s", xArray, yArray);
    return null;
  }

  public static BigDecimal calculateDownsideCaptureRatio(double[] portReturns, double[] benchmarkReturns) {
    return calculateCaptureRatio(portReturns, benchmarkReturns, false);
  }

  public static BigDecimal calculateUpsideCaptureRatio(double[] portReturns, double[] benchmarkReturns) {
    return calculateCaptureRatio(portReturns, benchmarkReturns, true);
  }

  private static BigDecimal calculateCaptureRatio(double[] portReturns, double[] benchmarkReturns, boolean greaterThan) {
    if ((portReturns.length > 0) && (portReturns.length == benchmarkReturns.length)) {
      BigDecimal benchTotal = BigDecimal.ONE;
      BigDecimal portTotal = BigDecimal.ONE;
      int numPeriods = 0;
      for (int idx = (benchmarkReturns.length - 1); idx >= 0; idx--) {
        boolean include = greaterThan ? (benchmarkReturns[idx] > 0) : benchmarkReturns[idx] < 0;
        if (include) {
          numPeriods++;
          benchTotal = benchTotal.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(benchmarkReturns[idx])), MC);
          portTotal = portTotal.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(portReturns[idx])), MC);
        }
      }
      if (numPeriods == 0) {
        return BigDecimal.ONE;
      }
      BigDecimal powExp = BigDecimal.ONE.divide(new BigDecimal(numPeriods), MC);
      return Numbers.pow(portTotal, powExp).subtract(BigDecimal.ONE).divide(Numbers.pow(benchTotal, powExp).subtract(BigDecimal.ONE), MC);

    }
    return null;
  }

}
