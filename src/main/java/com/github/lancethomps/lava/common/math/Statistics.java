package com.github.lancethomps.lava.common.math;

import static com.github.lancethomps.lava.common.logging.Logs.logWarn;
import static com.github.lancethomps.lava.common.math.MathUtil.MC;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.log4j.Logger;

/**
 * The Class Statistics.
 */
public class Statistics {

	/** The Constant CORRELATION. */
	private static final PearsonsCorrelation CORRELATION = new PearsonsCorrelation();

	/** The Constant COVARIANCE. */
	private static final Covariance COVARIANCE = new Covariance();

	/** The Constant LOG. */
	private static final Logger LOG = Logger.getLogger(Statistics.class);

	/**
	 * Calculate batting average.
	 *
	 * @param portReturns the port returns
	 * @param benchReturns the bench returns
	 * @return the big decimal
	 */
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

	/**
	 * Calculate betas.
	 *
	 * @param portReturns the port returns
	 * @param benchReturns the bench returns
	 * @return the big decimal
	 */
	public static BigDecimal calculateBetas(double[] portReturns, double[] benchReturns) {
		if ((portReturns.length > 1) && (benchReturns.length == portReturns.length)) {
			double variance = StatUtils.variance(benchReturns);
			double covariance = COVARIANCE.covariance(benchReturns, portReturns);
			double beta = covariance / variance;
			if (Double.isFinite(beta)) {
				return BigDecimal.valueOf(beta);
			}
			logWarn(LOG, "Beta was not finite: covariance=%s variance=%s", covariance, variance);
			return null;
		}
		// logWarn(LOG,
		// "Beta calculation failed because timeframe of returns for fund/beta securities is off!");
		return null;
	}

	/**
	 * Calculate correlation.
	 *
	 * @param xArray the x array
	 * @param yArray the y array
	 * @return the big decimal
	 */
	public static BigDecimal calculateCorrelation(@Nonnull double[] xArray, @Nonnull double[] yArray) {
		if (xArray.length <= 1) {
			return null;
		}
		double corr = CORRELATION.correlation(xArray, yArray);
		if (Double.isFinite(corr)) {
			return BigDecimal.valueOf(corr);
		}
		logWarn(LOG, "Correlation was not finite for values: xArray=%s yArray=%s", xArray, yArray);
		return null;
	}

	/**
	 * Calculate downside capture ratio.
	 *
	 * @param portReturns the port returns
	 * @param benchmarkReturns the benchmark returns
	 * @return the big decimal
	 */
	public static BigDecimal calculateDownsideCaptureRatio(double[] portReturns, double[] benchmarkReturns) {
		return calculateCaptureRatio(portReturns, benchmarkReturns, false);
	}

	/**
	 * Calculate upside capture ratio.
	 *
	 * @param portReturns the port returns
	 * @param benchmarkReturns the benchmark returns
	 * @return the big decimal
	 */
	public static BigDecimal calculateUpsideCaptureRatio(double[] portReturns, double[] benchmarkReturns) {
		return calculateCaptureRatio(portReturns, benchmarkReturns, true);
	}

	/**
	 * Calculate capture ratio.
	 *
	 * @param portReturns the port returns
	 * @param benchmarkReturns the benchmark returns
	 * @param greaterThan the greater than
	 * @return the big decimal
	 */
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
			//			BigDecimal benchTotal = BigDecimal.ZERO;
			//			BigDecimal portTotal = BigDecimal.ZERO;
			//			for (int idx = 0; idx < benchmarkReturns.length; idx++) {
			//				boolean include = greaterThan ? (benchmarkReturns[idx] > 0) : benchmarkReturns[idx] < 0;
			//				if (include) {
			//					benchTotal = benchTotal.add(BigDecimal.valueOf(benchmarkReturns[idx]));
			//					portTotal = portTotal.add(BigDecimal.valueOf(portReturns[idx]));
			//				}
			//			}
			//			return MathUtil.equalsZero(benchTotal) ? null : portTotal.divide(benchTotal, MC).movePointRight(2);
		}
		return null;
	}

}
