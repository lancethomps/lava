package com.github.lancethomps.lava.common.metrics;

import static io.dropwizard.metrics5.MetricRegistry.name;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import io.dropwizard.metrics5.Counter;
import io.dropwizard.metrics5.Meter;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.Timer;

public class MetricWebFilter implements Filter {

  public static final String METRIC_REGISTRY = "pageView";
  private final Map<Integer, Meter> metersByStatusCode = new HashMap<>(13);
  private Counter activeRequests;
  private Meter errorsMeter;
  private Meter otherMeter;

  private Timer requestTimer;

  private Meter timeoutsMeter;

  @Override
  public void destroy() {

  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    activeRequests.inc();
    final Timer.Context context = requestTimer.time();
    boolean error = false;
    try {
      chain.doFilter(request, response);
    } catch (IOException | RuntimeException | ServletException e) {
      error = true;
      throw e;
    } finally {
      if (!error && request.isAsyncStarted()) {
        request.getAsyncContext().addListener(new AsyncResultListener(context));
      } else {
        context.stop();
        activeRequests.dec();
        if (error) {
          errorsMeter.mark();
        } else {
          markMeterForStatusCode(((HttpServletResponse) response).getStatus());
        }
      }
    }
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    final MetricRegistry metricsRegistry = StatusMonitor.registry(METRIC_REGISTRY);
    metersByStatusCode.clear();

    Consumer<Integer> addMeter = (statusCode) -> {
      metersByStatusCode.put(statusCode, metricsRegistry.meter(name("responseCodes", statusCode.toString())));
    };
    addMeter.accept(HttpServletResponse.SC_OK);
    addMeter.accept(HttpServletResponse.SC_CREATED);
    addMeter.accept(HttpServletResponse.SC_NO_CONTENT);
    addMeter.accept(HttpServletResponse.SC_MOVED_PERMANENTLY);
    addMeter.accept(HttpServletResponse.SC_MOVED_TEMPORARILY);
    addMeter.accept(HttpServletResponse.SC_TEMPORARY_REDIRECT);
    addMeter.accept(HttpServletResponse.SC_BAD_REQUEST);
    addMeter.accept(HttpServletResponse.SC_UNAUTHORIZED);
    addMeter.accept(HttpServletResponse.SC_FORBIDDEN);
    addMeter.accept(HttpServletResponse.SC_NOT_FOUND);
    addMeter.accept(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    addMeter.accept(HttpServletResponse.SC_BAD_GATEWAY);
    addMeter.accept(HttpServletResponse.SC_GATEWAY_TIMEOUT);

    otherMeter = metricsRegistry.meter(name("responseCodes", "other"));
    timeoutsMeter = metricsRegistry.meter("timeouts");
    errorsMeter = metricsRegistry.meter("errors");
    activeRequests = metricsRegistry.counter("activeRequests");
    requestTimer = metricsRegistry.timer("requests");

  }

  private void markMeterForStatusCode(int status) {
    final Meter metric = metersByStatusCode.get(status);
    if (metric != null) {
      metric.mark();
    } else {
      otherMeter.mark();
    }
  }

  private class AsyncResultListener implements AsyncListener {

    private Timer.Context context;

    private boolean done;

    AsyncResultListener(Timer.Context context) {
      this.context = context;
    }

    @Override
    public void onComplete(AsyncEvent event) throws IOException {
      if (!done) {
        HttpServletResponse suppliedResponse = (HttpServletResponse) event.getSuppliedResponse();
        context.stop();
        activeRequests.dec();
        markMeterForStatusCode(suppliedResponse.getStatus());
      }
    }

    @Override
    public void onError(AsyncEvent event) throws IOException {
      context.stop();
      activeRequests.dec();
      errorsMeter.mark();
      done = true;
    }

    @Override
    public void onStartAsync(AsyncEvent event) throws IOException {

    }

    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
      context.stop();
      activeRequests.dec();
      timeoutsMeter.mark();
      done = true;
    }

  }

}
